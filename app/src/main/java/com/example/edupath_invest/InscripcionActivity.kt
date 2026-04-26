package com.example.edupath_invest

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class InscripcionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "mode"
        const val MODE_ADD = "add"
        const val MODE_REMOVE = "remove"
    }

    private data class ActividadConfig(
        val nombre: String,
        val porcentaje: Double
    )

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recycler: RecyclerView
    private lateinit var tvSubtitulo: TextView
    private lateinit var tvTitulo: TextView
    private lateinit var btnGuardarInscripcion: Button
    private val adapter = InscripcionMateriasAdapter()
    private var modo: String = MODE_ADD
    private var codigosInscritosActuales: Set<String> = emptySet()
    private var planesActividadActuales: List<Map<String, Any>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inscripcion)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        modo = intent.getStringExtra(EXTRA_MODE) ?: MODE_ADD

        tvTitulo = findViewById(R.id.tvTituloInscripcion)
        tvSubtitulo = findViewById(R.id.tvSubtituloInscripcion)
        recycler = findViewById(R.id.recyclerMateriasInscribibles)
        btnGuardarInscripcion = findViewById(R.id.btnGuardarInscripcion)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnGuardarInscripcion.setOnClickListener {
            guardarInscripcion()
        }

        cargarMateriasInscribibles()
    }

    private fun cargarMateriasInscribibles() {
        val userId = auth.currentUser?.uid ?: return

        db.collection(UserAcademicProfile.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                codigosInscritosActuales = UserAcademicProfile.obtenerMateriasInscritas(document)
                planesActividadActuales = (document.get(UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN) as? List<*>)
                    ?.mapNotNull { it as? Map<String, Any> }
                    ?: emptyList()

                val anioPensum = UserAcademicProfile.obtenerAnioPensum(document)
                val materias = UserAcademicProfile.obtenerMateriasPensum(this, anioPensum)
                UserAcademicProfile.aplicarEstadosPensum(document, materias)

                val materiasRender = if (modo == MODE_REMOVE) {
                    materias
                        .filter { it.codigo in codigosInscritosActuales }
                        .map { materia ->
                            val abreDirectas = GrafoHelper.contarDesbloqueos(materia.codigo, materias)
                            val nivel = when {
                                abreDirectas >= 5 -> NivelImportancia.MUY_IMPORTANTE
                                abreDirectas >= 1 -> NivelImportancia.IMPORTANTE
                                else -> NivelImportancia.PUEDE_ESPERAR
                            }

                            InscripcionMateriaUi(
                                codigo = materia.codigo,
                                nombre = materia.nombre,
                                peso = abreDirectas,
                                materiasQueAbre = abreDirectas,
                                importancia = nivel
                            )
                        }
                        .sortedBy { it.nombre }
                } else {
                    GrafoHelper.obtenerMateriasPriorizadas(materias, Int.MAX_VALUE)
                        .filter { it.materia.codigo !in codigosInscritosActuales }
                        .map { prioridad ->
                            val nivel = when {
                                prioridad.dependenciasTotales >= 5 -> NivelImportancia.MUY_IMPORTANTE
                                prioridad.dependenciasTotales >= 1 -> NivelImportancia.IMPORTANTE
                                else -> NivelImportancia.PUEDE_ESPERAR
                            }

                            InscripcionMateriaUi(
                                codigo = prioridad.materia.codigo,
                                nombre = prioridad.materia.nombre,
                                peso = prioridad.peso,
                                materiasQueAbre = prioridad.dependenciasTotales,
                                importancia = nivel
                            )
                        }
                }

                tvTitulo.text = if (modo == MODE_REMOVE) "Eliminar materias inscritas" else "Proceso de inscripcion"
                val cantidad = materiasRender.size
                tvSubtitulo.text = if (cantidad == 0) {
                    if (modo == MODE_REMOVE) "No hay materias inscritas para eliminar."
                    else "No hay materias habilitadas para inscribir por ahora."
                } else {
                    if (modo == MODE_REMOVE) "$cantidad materias disponibles para eliminar"
                    else "$cantidad materias habilitadas ordenadas por importancia"
                }

                adapter.submitList(materiasRender)
            }
    }

    private fun guardarInscripcion() {
        val materiasSeleccionadas = adapter.obtenerMateriasSeleccionadas()

        if (materiasSeleccionadas.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una materia", Toast.LENGTH_SHORT).show()
            return
        }

        if (modo == MODE_REMOVE) {
            persistirEliminacion(materiasSeleccionadas)
            return
        }

        if (!validarRestriccionCuartaMatricula(materiasSeleccionadas)) return

        val todasLasMaterias = adapter.obtenerTodasLasMaterias()
        if (!validarPriorizacionSeleccion(materiasSeleccionadas, todasLasMaterias)) {
            Toast.makeText(this, "Debes priorizar materias Muy importantes o Importantes antes de seleccionar una opcional", Toast.LENGTH_LONG).show()
            return
        }

        solicitarActividadesPorMateria(materiasSeleccionadas, 0, mutableListOf())
    }

    private fun validarPriorizacionSeleccion(seleccionadas: List<InscripcionMateriaUi>, todas: List<InscripcionMateriaUi>): Boolean {
        val seleccionaPuedeEsperar = seleccionadas.any { it.importancia == NivelImportancia.PUEDE_ESPERAR }
        if (!seleccionaPuedeEsperar) return true
        val codigosSeleccionados = seleccionadas.map { it.codigo }.toSet()
        return todas.none { it.codigo !in codigosSeleccionados && (it.importancia == NivelImportancia.MUY_IMPORTANTE || it.importancia == NivelImportancia.IMPORTANTE) }
    }

    private fun solicitarActividadesPorMateria(materias: List<InscripcionMateriaUi>, indice: Int, planes: MutableList<Map<String, Any>>) {
        if (indice >= materias.size) {
            persistirInscripcion(materias, planes)
            return
        }

        val materia = materias[indice]
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_configurar_actividades, null)
        val tvDialogSuma = dialogView.findViewById<TextView>(R.id.tvDialogSumaActividad)
        val btnDialogAgregar = dialogView.findViewById<Button>(R.id.btnDialogAgregarActividad)
        val btnDialogQuitar = dialogView.findViewById<Button>(R.id.btnDialogQuitarActividad)
        val btnDialogCancelar = dialogView.findViewById<Button>(R.id.btnDialogCancelar)
        val btnDialogContinuar = dialogView.findViewById<Button>(R.id.btnDialogContinuar)
        val contenedorFilas = dialogView.findViewById<LinearLayout>(R.id.layoutDialogFilasActividad)

        dialogView.findViewById<TextView>(R.id.tvDialogTituloActividad).text = materia.nombre
        val filasActividad = mutableListOf<Triple<View, EditText, EditText>>()

        fun actualizarChipSuma() {
            val suma = filasActividad.sumOf { it.third.text.toString().trim().toDoubleOrNull() ?: 0.0 }
            tvDialogSuma.text = "Suma actual: ${String.format("%.1f", suma)}%"
        }

        fun agregarFila() {
            val fila = LayoutInflater.from(this).inflate(R.layout.item_dialog_actividad_input, contenedorFilas, false)
            val n = fila.findViewById<EditText>(R.id.etDialogNombreActividad)
            val p = fila.findViewById<EditText>(R.id.etDialogPorcentajeActividad)
            p.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { actualizarChipSuma() }
                override fun afterTextChanged(s: Editable?) {}
            })
            contenedorFilas.addView(fila)
            filasActividad.add(Triple(fila, n, p))
            actualizarChipSuma()
        }

        agregarFila()
        btnDialogAgregar.setOnClickListener { agregarFila() }
        btnDialogQuitar.setOnClickListener {
            if (filasActividad.size > 1) {
                val (fila, _, _) = filasActividad.removeAt(filasActividad.lastIndex)
                contenedorFilas.removeView(fila)
                actualizarChipSuma()
            }
        }

        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        btnDialogCancelar.setOnClickListener { dialog.dismiss() }
        btnDialogContinuar.setOnClickListener {
            val actividades = mutableListOf<Map<String, Any?>>()
            var suma = 0.0
            filasActividad.forEach { (_, n, p) ->
                val nombre = n.text.toString().trim()
                val porcentaje = p.text.toString().toDoubleOrNull() ?: 0.0
                suma += porcentaje
                actividades.add(mapOf("nombre" to nombre, "porcentaje" to porcentaje, "nota" to null, "estado" to "pendiente"))
            }

            if (suma != 100.0) {
                Toast.makeText(this, "La suma debe ser 100%", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            planes.add(mapOf("codigo" to materia.codigo, "nombre" to materia.nombre, "actividades" to actividades))
            dialog.dismiss()
            solicitarActividadesPorMateria(materias, indice + 1, planes)
        }
        dialog.show()
    }

    private fun persistirInscripcion(materiasSeleccionadas: List<InscripcionMateriaUi>, planesActividades: List<Map<String, Any>>) {
        val userId = auth.currentUser?.uid ?: return
        val codigos = (codigosInscritosActuales + materiasSeleccionadas.map { it.codigo }).toList()
        val data = mapOf(UserAcademicProfile.FIELD_ENROLLED_SUBJECTS to codigos, UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN to planesActividadActuales + planesActividades)
        db.collection(UserAcademicProfile.USERS_COLLECTION).document(userId).set(data, SetOptions.merge()).addOnSuccessListener { finish() }
    }

    private fun persistirEliminacion(materiasSeleccionadas: List<InscripcionMateriaUi>) {
        val userId = auth.currentUser?.uid ?: return
        val eliminados = materiasSeleccionadas.map { it.codigo }.toSet()
        val codigos = codigosInscritosActuales.filterNot { it in eliminados }
        val planes = planesActividadActuales.filterNot { (it["codigo"] as? String) in eliminados }
        val data = mapOf(UserAcademicProfile.FIELD_ENROLLED_SUBJECTS to codigos, UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN to planes)
        db.collection(UserAcademicProfile.USERS_COLLECTION).document(userId).set(data, SetOptions.merge()).addOnSuccessListener { finish() }
    }

    private fun validarRestriccionCuartaMatricula(seleccionadas: List<InscripcionMateriaUi>): Boolean {
        val tieneCuarta = seleccionadas.any { it.peso >= 10000 }
        if (tieneCuarta && seleccionadas.size > 1) {
            Toast.makeText(this, "En 4ta matrícula solo puedes llevar esa materia.", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
}
package com.example.edupath_invest

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class CicloActivity : AppCompatActivity() {

    private data class ResultadoMateriaCiclo(
        val codigo: String,
        val nombre: String,
        val promedioFinal: Double,
        val estado: String,
        val completa: Boolean
    )

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var btnIniciarInscripcion: Button
    private lateinit var btnAgregarMateria: Button
    private lateinit var btnEliminarMateria: Button
    private lateinit var layoutAccionesMaterias: LinearLayout
    private lateinit var cardMateria1: LinearLayout
    private lateinit var cardMateria2: LinearLayout
    private lateinit var cardMateria3: LinearLayout
    private lateinit var rowMateria1Actividad1: LinearLayout
    private lateinit var rowMateria1Actividad2: LinearLayout
    private lateinit var rowMateria1Actividad3: LinearLayout
    private lateinit var rowMateria2Actividad1: LinearLayout
    private lateinit var rowMateria2Actividad2: LinearLayout
    private lateinit var rowMateria2Actividad3: LinearLayout
    private lateinit var rowMateria3Actividad1: LinearLayout
    private lateinit var rowMateria3Actividad2: LinearLayout
    private lateinit var tvMateria1Titulo: TextView
    private lateinit var tvMateria2Titulo: TextView
    private lateinit var tvMateria3Titulo: TextView
    private lateinit var tvMateria1Necesita: TextView
    private lateinit var tvMateria2Necesita: TextView
    private lateinit var tvMateria3Necesita: TextView
    private lateinit var tvMateria1Total: TextView
    private lateinit var tvMateria2Total: TextView
    private lateinit var tvMateria3Total: TextView

    private var codigoMateriaCard1: String? = null
    private var codigoMateriaCard2: String? = null
    private var codigoMateriaCard3: String? = null
    private val planesActividad = mutableListOf<MutableMap<String, Any?>>()
    private val filasMateria1 = mutableListOf<LinearLayout>()
    private val filasMateria2 = mutableListOf<LinearLayout>()
    private val filasMateria3 = mutableListOf<LinearLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ciclo)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        btnIniciarInscripcion = findViewById(R.id.btnIniciarInscripcion)
        btnAgregarMateria = findViewById(R.id.btnAgregarMateria)
        btnEliminarMateria = findViewById(R.id.btnEliminarMateria)
        layoutAccionesMaterias = findViewById(R.id.layoutAccionesMaterias)
        cardMateria1 = findViewById(R.id.cardMateria1)
        cardMateria2 = findViewById(R.id.cardMateria2)
        cardMateria3 = findViewById(R.id.cardMateria3)
        rowMateria1Actividad1 = findViewById(R.id.rowMateria1Actividad1)
        rowMateria1Actividad2 = findViewById(R.id.rowMateria1Actividad2)
        rowMateria1Actividad3 = findViewById(R.id.rowMateria1Actividad3)
        rowMateria2Actividad1 = findViewById(R.id.rowMateria2Actividad1)
        rowMateria2Actividad2 = findViewById(R.id.rowMateria2Actividad2)
        rowMateria2Actividad3 = findViewById(R.id.rowMateria2Actividad3)
        rowMateria3Actividad1 = findViewById(R.id.rowMateria3Actividad1)
        rowMateria3Actividad2 = findViewById(R.id.rowMateria3Actividad2)
        filasMateria1.addAll(listOf(rowMateria1Actividad1, rowMateria1Actividad2, rowMateria1Actividad3))
        filasMateria2.addAll(listOf(rowMateria2Actividad1, rowMateria2Actividad2, rowMateria2Actividad3))
        filasMateria3.addAll(listOf(rowMateria3Actividad1, rowMateria3Actividad2))
        tvMateria1Titulo = findViewById(R.id.tvMateria1Titulo)
        tvMateria2Titulo = findViewById(R.id.tvMateria2Titulo)
        tvMateria3Titulo = findViewById(R.id.tvMateria3Titulo)
        tvMateria1Necesita = findViewById(R.id.tvMateria1Necesita)
        tvMateria2Necesita = findViewById(R.id.tvMateria2Necesita)
        tvMateria3Necesita = findViewById(R.id.tvMateria3Necesita)
        tvMateria1Total = findViewById(R.id.tvMateria1Total)
        tvMateria2Total = findViewById(R.id.tvMateria2Total)
        tvMateria3Total = findViewById(R.id.tvMateria3Total)

        btnIniciarInscripcion.setOnClickListener {
            startActivity(Intent(this, InscripcionActivity::class.java))
        }

        btnAgregarMateria.setOnClickListener {
            val intent = Intent(this, InscripcionActivity::class.java)
            intent.putExtra(InscripcionActivity.EXTRA_MODE, InscripcionActivity.MODE_ADD)
            startActivity(intent)
        }

        btnEliminarMateria.setOnClickListener {
            val intent = Intent(this, InscripcionActivity::class.java)
            intent.putExtra(InscripcionActivity.EXTRA_MODE, InscripcionActivity.MODE_REMOVE)
            startActivity(intent)
        }

        configurarVisibilidadBotonInscripcion()

        BottomNavHelper.setup(this, "cycle")
    }

    override fun onResume() {
        super.onResume()
        configurarVisibilidadBotonInscripcion()
    }

    private fun configurarVisibilidadBotonInscripcion() {
        val userId = auth.currentUser?.uid ?: return

        db.collection(UserAcademicProfile.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val existeCampoInscritas = document.contains(UserAcademicProfile.FIELD_ENROLLED_SUBJECTS)

                if (!existeCampoInscritas) {
                    btnIniciarInscripcion.visibility = View.VISIBLE
                    layoutAccionesMaterias.visibility = View.GONE
                    cardMateria1.visibility = View.GONE
                    cardMateria2.visibility = View.GONE
                    cardMateria3.visibility = View.GONE
                    return@addOnSuccessListener
                }

                btnIniciarInscripcion.visibility = View.GONE
                layoutAccionesMaterias.visibility = View.VISIBLE

                val materiasInscritas = (document.get(UserAcademicProfile.FIELD_ENROLLED_SUBJECTS) as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?: UserAcademicProfile.obtenerMateriasInscritas(document).toList()

                val anioPensum = UserAcademicProfile.obtenerAnioPensum(document)
                val materiasPensum = UserAcademicProfile.obtenerMateriasPensum(anioPensum)
                val nombrePorCodigo = materiasPensum.associate { it.codigo to it.nombre }
                cargarPlanesActividades(document)

                codigoMateriaCard1 = materiasInscritas.getOrNull(0)
                codigoMateriaCard2 = materiasInscritas.getOrNull(1)
                codigoMateriaCard3 = materiasInscritas.getOrNull(2)

                pintarTarjeta(
                    card = cardMateria1,
                    titulo = tvMateria1Titulo,
                    codigoMateria = codigoMateriaCard1,
                    nombrePorCodigo = nombrePorCodigo,
                    filasActividad = filasMateria1,
                    tvTotal = tvMateria1Total,
                    tvNecesita = tvMateria1Necesita
                )
                pintarTarjeta(
                    card = cardMateria2,
                    titulo = tvMateria2Titulo,
                    codigoMateria = codigoMateriaCard2,
                    nombrePorCodigo = nombrePorCodigo,
                    filasActividad = filasMateria2,
                    tvTotal = tvMateria2Total,
                    tvNecesita = tvMateria2Necesita
                )
                pintarTarjeta(
                    card = cardMateria3,
                    titulo = tvMateria3Titulo,
                    codigoMateria = codigoMateriaCard3,
                    nombrePorCodigo = nombrePorCodigo,
                    filasActividad = filasMateria3,
                    tvTotal = tvMateria3Total,
                    tvNecesita = tvMateria3Necesita
                )

                finalizarCicloSiCorresponde(
                    document = document,
                    materiasInscritas = materiasInscritas,
                    nombrePorCodigo = nombrePorCodigo,
                    materiasPensum = materiasPensum
                )

                cardMateria1.setOnClickListener {
                    codigoMateriaCard1?.let { codigo ->
                        mostrarDialogoEditarNotas(codigo, tvMateria1Titulo.text.toString())
                    }
                }
                cardMateria2.setOnClickListener {
                    codigoMateriaCard2?.let { codigo ->
                        mostrarDialogoEditarNotas(codigo, tvMateria2Titulo.text.toString())
                    }
                }
                cardMateria3.setOnClickListener {
                    codigoMateriaCard3?.let { codigo ->
                        mostrarDialogoEditarNotas(codigo, tvMateria3Titulo.text.toString())
                    }
                }
            }
            .addOnFailureListener {
                btnIniciarInscripcion.visibility = View.VISIBLE
                layoutAccionesMaterias.visibility = View.GONE
                cardMateria1.visibility = View.GONE
                cardMateria2.visibility = View.GONE
                cardMateria3.visibility = View.GONE
            }
    }

    private fun pintarTarjeta(
        card: LinearLayout,
        titulo: TextView,
        codigoMateria: String?,
        nombrePorCodigo: Map<String, String>,
        filasActividad: MutableList<LinearLayout>,
        tvTotal: TextView,
        tvNecesita: TextView
    ) {
        if (codigoMateria.isNullOrBlank()) {
            card.visibility = View.GONE
            return
        }

        card.visibility = View.VISIBLE
        titulo.text = nombrePorCodigo[codigoMateria] ?: codigoMateria

        val plan = buscarPlanMateria(codigoMateria)
        val actividades = obtenerActividades(plan)
        asegurarFilasActividad(card, filasActividad, actividades.size, tvTotal)
        pintarTablaActividad(actividades, filasActividad, tvTotal, tvNecesita)
    }

    private fun asegurarFilasActividad(
        card: LinearLayout,
        filasActividad: MutableList<LinearLayout>,
        cantidadNecesaria: Int,
        tvTotal: TextView
    ) {
        while (filasActividad.size < cantidadNecesaria) {
            val nuevaFila = crearFilaActividadDinamica()
            val posicionTotal = card.indexOfChild(tvTotal)
            card.addView(nuevaFila, posicionTotal)
            filasActividad.add(nuevaFila)
        }
    }

    private fun crearFilaActividadDinamica(): LinearLayout {
        val fila = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 6.dp()
            }
            orientation = LinearLayout.HORIZONTAL
            weightSum = 4f
        }

        fila.addView(crearCeldaActividad(1.5f, false))
        fila.addView(crearCeldaActividad(1f, true))
        fila.addView(crearCeldaActividad(0.8f, true))
        fila.addView(crearCeldaActividad(0.7f, true))

        return fila
    }

    private fun crearCeldaActividad(peso: Float, centrado: Boolean): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, peso)
            textSize = 12f
            setTextColor(resources.getColor(R.color.text_dark, theme))
            gravity = if (centrado) android.view.Gravity.CENTER else android.view.Gravity.START
        }
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun cargarPlanesActividades(document: com.google.firebase.firestore.DocumentSnapshot) {
        planesActividad.clear()

        val planes = (document.get(UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN) as? List<*>) ?: emptyList<Any>()
        planes.forEach { planRaw ->
            val plan = planRaw as? Map<*, *> ?: return@forEach
            val codigo = plan["codigo"] as? String ?: return@forEach
            val nombre = plan["nombre"] as? String ?: ""

            val actividadesRaw = (plan["actividades"] as? List<*>) ?: emptyList<Any>()
            val actividades = actividadesRaw.mapNotNull { actividadRaw ->
                val actividad = actividadRaw as? Map<*, *> ?: return@mapNotNull null

                mutableMapOf<String, Any?>(
                    "nombre" to (actividad["nombre"] as? String ?: "Actividad"),
                    "porcentaje" to (actividad["porcentaje"] as? Number)?.toDouble(),
                    "nota" to (actividad["nota"] as? Number)?.toDouble(),
                    "estado" to (actividad["estado"] as? String ?: "pendiente")
                )
            }.toMutableList()

            planesActividad.add(
                mutableMapOf(
                    "codigo" to codigo,
                    "nombre" to nombre,
                    "actividades" to actividades
                )
            )
        }
    }

    private fun buscarPlanMateria(codigoMateria: String): MutableMap<String, Any?>? {
        return planesActividad.firstOrNull { it["codigo"] == codigoMateria }
    }

    @Suppress("UNCHECKED_CAST")
    private fun obtenerActividades(plan: MutableMap<String, Any?>?): MutableList<MutableMap<String, Any?>> {
        if (plan == null) {
            return mutableListOf()
        }

        return (plan["actividades"] as? MutableList<MutableMap<String, Any?>>) ?: mutableListOf()
    }

    private fun pintarTablaActividad(
        actividades: List<MutableMap<String, Any?>>,
        filasActividad: List<LinearLayout>,
        tvTotal: TextView,
        tvNecesita: TextView
    ) {
        var totalGanado = 0.0

        filasActividad.forEachIndexed { indice, row ->
            val actividad = actividades.getOrNull(indice)
            if (actividad == null) {
                row.visibility = View.GONE
                return@forEachIndexed
            }

            row.visibility = View.VISIBLE

            val tvActividad = row.getChildAt(0) as TextView
            val tvNota = row.getChildAt(1) as TextView
            val tvValor = row.getChildAt(2) as TextView
            val tvEstado = row.getChildAt(3) as TextView

            val nombre = actividad["nombre"] as? String ?: "Actividad"
            val porcentaje = (actividad["porcentaje"] as? Number)?.toDouble() ?: 0.0
            val nota = (actividad["nota"] as? Number)?.toDouble()
            val ganado = if (nota == null) null else (nota * porcentaje) / 100.0

            tvActividad.text = nombre
            tvValor.text = "${String.format("%.1f", porcentaje)}%"
            tvNota.text = nota?.let { String.format("%.1f", it) } ?: ""
            tvEstado.text = ganado?.let { String.format("%.2f", it) } ?: ""

            if (ganado != null) {
                totalGanado += ganado
            }
        }

        tvTotal.text = "Total: ${String.format("%.2f", totalGanado)}"

        val hayNotaRegistrada = actividades.any { (it["nota"] as? Number) != null }
        if (!hayNotaRegistrada) {
            tvNecesita.visibility = View.GONE
            return
        }

        val (texto, visible) = calcularTextoNecesita(actividades)
        tvNecesita.visibility = if (visible) View.VISIBLE else View.GONE
        tvNecesita.text = texto
    }

    private fun calcularTextoNecesita(actividades: List<MutableMap<String, Any?>>): Pair<String, Boolean> {
        var puntosActuales = 0.0
        var porcentajePendiente = 0.0

        actividades.forEach { actividad ->
            val porcentaje = (actividad["porcentaje"] as? Number)?.toDouble() ?: 0.0
            val nota = (actividad["nota"] as? Number)?.toDouble()

            if (nota == null) {
                porcentajePendiente += porcentaje
            } else {
                puntosActuales += (nota * porcentaje) / 100.0
            }
        }

        if (porcentajePendiente <= 0.0) {
            val estado = if (puntosActuales >= 6.0) {
                "Aprobada"
            } else {
                "Reprobada"
            }
            return "$estado - promedio final: ${String.format("%.2f", puntosActuales)}" to true
        }

        val notaNecesaria = (6.0 - puntosActuales) / (porcentajePendiente / 100.0)
        return when {
            notaNecesaria <= 0.0 -> "Ya aseguras aprobacion" to true
            notaNecesaria > 10.0 -> "Necesitas sacar mas de 10" to true
            else -> "Necesitas sacar: ${String.format("%.1f", notaNecesaria)}" to true
        }
    }

    private fun finalizarCicloSiCorresponde(
        document: DocumentSnapshot,
        materiasInscritas: List<String>,
        nombrePorCodigo: Map<String, String>,
        materiasPensum: List<MateriaPensum>
    ) {
        if (materiasInscritas.isEmpty()) {
            return
        }

        val resultados = materiasInscritas.mapNotNull { codigo ->
            val plan = buscarPlanMateria(codigo)
            val actividades = obtenerActividades(plan)
            evaluarResultadoMateria(codigo, nombrePorCodigo[codigo] ?: codigo, actividades)
        }

        val todasCompletas =
            resultados.size == materiasInscritas.size &&
                resultados.all { it.completa }

        if (!todasCompletas) {
            return
        }

        persistirCierreCiclo(document, resultados, materiasPensum)
    }

    private fun evaluarResultadoMateria(
        codigo: String,
        nombre: String,
        actividades: List<MutableMap<String, Any?>>
    ): ResultadoMateriaCiclo {
        var promedioFinal = 0.0
        var completa = actividades.isNotEmpty()

        actividades.forEach { actividad ->
            val porcentaje = (actividad["porcentaje"] as? Number)?.toDouble() ?: 0.0
            val nota = (actividad["nota"] as? Number)?.toDouble()

            if (nota == null) {
                completa = false
            } else {
                promedioFinal += (nota * porcentaje) / 100.0
            }
        }

        val estado = if (promedioFinal >= 6.0) "aprobada" else "reprobada"

        return ResultadoMateriaCiclo(
            codigo = codigo,
            nombre = nombre,
            promedioFinal = promedioFinal,
            estado = estado,
            completa = completa
        )
    }

    private fun persistirCierreCiclo(
        document: DocumentSnapshot,
        resultados: List<ResultadoMateriaCiclo>,
        materiasPensum: List<MateriaPensum>
    ) {
        val userId = auth.currentUser?.uid ?: return
        val codigosFinalizados = resultados.map { it.codigo }.toSet()

        val historialExistente = (document.get(UserAcademicProfile.FIELD_ACADEMIC_HISTORY) as? List<*>)
            ?.mapNotNull { it as? Map<String, Any> }
            ?.filterNot { (it["codigo"] as? String) in codigosFinalizados }
            ?: emptyList()

        val historialFinal = historialExistente + resultados.map { resultado ->
            mapOf<String, Any>(
                "codigo" to resultado.codigo,
                "nombre" to resultado.nombre,
                "promedioFinal" to resultado.promedioFinal,
                "estado" to resultado.estado
            )
        }

        val materiasAprobadasActuales = UserAcademicProfile.obtenerMateriasAprobadas(document).toMutableSet()
        materiasAprobadasActuales.addAll(
            resultados
                .filter { it.estado == "aprobada" }
                .map { it.codigo }
        )

        val planesRestantes = planesActividad.filterNot { plan ->
            val codigo = plan["codigo"] as? String
            codigo in codigosFinalizados
        }

        val codigosCursados = historialFinal
            .mapNotNull { it["codigo"] as? String }
            .toSet()
        val cicloActual = UserAcademicProfile.calcularCicloActual(
            pensum = materiasPensum,
            codigosCursados = codigosCursados
        )

        val payload = mapOf(
            UserAcademicProfile.FIELD_ACADEMIC_HISTORY to historialFinal,
            UserAcademicProfile.FIELD_APPROVED_SUBJECTS to materiasAprobadasActuales.toList(),
            UserAcademicProfile.FIELD_ENROLLED_SUBJECTS to emptyList<String>(),
            UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN to planesRestantes,
            UserAcademicProfile.FIELD_CURRENT_CYCLE to cicloActual
        )

        db.collection(UserAcademicProfile.USERS_COLLECTION)
            .document(userId)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Ciclo culminado: se actualizaron estados finales", Toast.LENGTH_LONG).show()
                configurarVisibilidadBotonInscripcion()
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo cerrar el ciclo automaticamente", Toast.LENGTH_LONG).show()
            }
    }

    private fun mostrarDialogoEditarNotas(codigoMateria: String, nombreMateria: String) {
        val plan = buscarPlanMateria(codigoMateria)
        if (plan == null) {
            Toast.makeText(this, "No hay plan de actividades para esta materia", Toast.LENGTH_SHORT).show()
            return
        }

        val actividades = obtenerActividades(plan)
        if (actividades.isEmpty()) {
            Toast.makeText(this, "No hay actividades configuradas para esta materia", Toast.LENGTH_SHORT).show()
            return
        }

        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val campos = mutableListOf<Pair<MutableMap<String, Any?>, EditText>>()

        actividades.forEach { actividad ->
            val nombre = actividad["nombre"] as? String ?: "Actividad"
            val porcentaje = (actividad["porcentaje"] as? Number)?.toDouble() ?: 0.0
            val notaActual = (actividad["nota"] as? Number)?.toDouble()

            val label = TextView(this).apply {
                text = "$nombre (${String.format("%.1f", porcentaje)}%)"
            }

            val campoNota = EditText(this).apply {
                hint = "Nota (0-10)"
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(notaActual?.let { String.format("%.1f", it) } ?: "")
            }

            contenedor.addView(label)
            contenedor.addView(campoNota)
            campos.add(actividad to campoNota)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("$nombreMateria\nEditar notas")
            .setView(contenedor)
            .setCancelable(false)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                campos.forEach { (actividad, campoNota) ->
                    val valor = campoNota.text.toString().trim()
                    val nota = if (valor.isBlank()) null else valor.toDoubleOrNull()

                    if (nota != null && (nota < 0.0 || nota > 10.0)) {
                        Toast.makeText(this, "Las notas deben estar entre 0 y 10", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    actividad["nota"] = nota
                    actividad["estado"] = if (nota == null) "pendiente" else "registrada"
                }

                guardarPlanActividadesActualizado()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun guardarPlanActividadesActualizado() {
        val userId = auth.currentUser?.uid ?: return
        val payload = mapOf(
            UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN to planesActividad
        )

        db.collection(UserAcademicProfile.USERS_COLLECTION)
            .document(userId)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener {
                configurarVisibilidadBotonInscripcion()
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo guardar la nota", Toast.LENGTH_LONG).show()
            }
    }
}
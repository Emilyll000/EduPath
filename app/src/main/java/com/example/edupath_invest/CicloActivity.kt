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

    // Referencia a cada card por código de materia
    private data class MateriaCardRefs(
        val card: LinearLayout,
        val titulo: TextView,
        val filasActividad: MutableList<LinearLayout>,
        val tvTotal: TextView,
        val tvNecesita: TextView
    )

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var btnIniciarInscripcion: Button
    private lateinit var btnAgregarMateria: Button
    private lateinit var btnEliminarMateria: Button
    private lateinit var layoutAccionesMaterias: LinearLayout
    private lateinit var llMateriasContainer: LinearLayout

    private val planesActividad = mutableListOf<MutableMap<String, Any?>>()
    // Mapa de código de materia -> sus refs de UI
    private val cardsPorCodigo = mutableMapOf<String, MateriaCardRefs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ciclo)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        btnIniciarInscripcion = findViewById(R.id.btnIniciarInscripcion)
        btnAgregarMateria = findViewById(R.id.btnAgregarMateria)
        btnEliminarMateria = findViewById(R.id.btnEliminarMateria)
        layoutAccionesMaterias = findViewById(R.id.layoutAccionesMaterias)
        llMateriasContainer = findViewById(R.id.llMateriasContainer)

        btnIniciarInscripcion.setOnClickListener {
            startActivity(Intent(this, InscripcionActivity::class.java))
        }
        btnAgregarMateria.setOnClickListener {
            startActivity(Intent(this, InscripcionActivity::class.java).apply {
                putExtra(InscripcionActivity.EXTRA_MODE, InscripcionActivity.MODE_ADD)
            })
        }
        btnEliminarMateria.setOnClickListener {
            startActivity(Intent(this, InscripcionActivity::class.java).apply {
                putExtra(InscripcionActivity.EXTRA_MODE, InscripcionActivity.MODE_REMOVE)
            })
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
                    llMateriasContainer.visibility = View.GONE
                    return@addOnSuccessListener
                }

                btnIniciarInscripcion.visibility = View.GONE
                layoutAccionesMaterias.visibility = View.VISIBLE
                llMateriasContainer.visibility = View.VISIBLE

                val materiasInscritas = (document.get(UserAcademicProfile.FIELD_ENROLLED_SUBJECTS) as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?: UserAcademicProfile.obtenerMateriasInscritas(document).toList()

                val anioPensum = UserAcademicProfile.obtenerAnioPensum(document)
                val materiasPensum = UserAcademicProfile.obtenerMateriasPensum(anioPensum)
                val nombrePorCodigo = materiasPensum.associate { it.codigo to it.nombre }

                cargarPlanesActividades(document)

                // Reconstruir todas las cards dinámicamente
                construirCardsMateriasInscritas(materiasInscritas, nombrePorCodigo)

                finalizarCicloSiCorresponde(
                    document = document,
                    materiasInscritas = materiasInscritas,
                    nombrePorCodigo = nombrePorCodigo,
                    materiasPensum = materiasPensum
                )
            }
            .addOnFailureListener {
                btnIniciarInscripcion.visibility = View.VISIBLE
                layoutAccionesMaterias.visibility = View.GONE
                llMateriasContainer.visibility = View.GONE
            }
    }

    /**
     * Limpia el contenedor y genera una card por cada materia inscrita,
     * sin importar cuántas sean.
     */
    private fun construirCardsMateriasInscritas(
        materiasInscritas: List<String>,
        nombrePorCodigo: Map<String, String>
    ) {
        llMateriasContainer.removeAllViews()
        cardsPorCodigo.clear()

        materiasInscritas.forEachIndexed { index, codigo ->
            val esAzul = index % 2 == 0   // alterna colores igual que el diseño original
            val cardRefs = crearCardMateria(codigo, nombrePorCodigo, esAzul)
            cardsPorCodigo[codigo] = cardRefs

            // Margen superior: más grande para la primera card
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = if (index == 0) 24.dp() else 16.dp()
            }
            cardRefs.card.layoutParams = lp
            llMateriasContainer.addView(cardRefs.card)

            // Pintar datos de actividades
            val plan = buscarPlanMateria(codigo)
            val actividades = obtenerActividades(plan)
            asegurarFilasActividad(cardRefs.card, cardRefs.filasActividad, actividades.size, cardRefs.tvTotal)
            pintarTablaActividad(actividades, cardRefs.filasActividad, cardRefs.tvTotal, cardRefs.tvNecesita)

            // Click para editar notas
            cardRefs.card.setOnClickListener {
                mostrarDialogoEditarNotas(codigo, cardRefs.titulo.text.toString())
            }
        }
    }

    /**
     * Construye programáticamente una card idéntica a las del XML original.
     */
    private fun crearCardMateria(
        codigo: String,
        nombrePorCodigo: Map<String, String>,
        esAzul: Boolean
    ): MateriaCardRefs {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14.dp(), 14.dp(), 14.dp(), 14.dp())
            elevation = 2f
            background = resources.getDrawable(
                if (esAzul) R.drawable.bg_input_blue else R.drawable.bg_subject_card_yellow,
                theme
            )
        }

        // Título de la materia
        val tvTitulo = TextView(this).apply {
            text = nombrePorCodigo[codigo] ?: codigo
            textSize = 15f
            setTextColor(resources.getColor(R.color.text_dark, theme))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        card.addView(tvTitulo)

        // Fila encabezado (Actividad | Nota | Valor | Ganado)
        val encabezado = crearFilaEncabezado()
        card.addView(encabezado)

        // Divisor
        val divisor = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1.dp()
            ).apply { topMargin = 6.dp() }
            setBackgroundColor(
                if (esAzul) 0x8888A9C7.toInt() else 0xFFD5B861.toInt()
            )
        }
        card.addView(divisor)

        // Total
        val tvTotal = TextView(this).apply {
            text = "Total: 0.00"
            textSize = 12f
            setTextColor(resources.getColor(R.color.text_dark, theme))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 10.dp() }
        }

        // Necesita
        val tvNecesita = TextView(this).apply {
            text = ""
            textSize = 12f
            setTextColor(resources.getColor(R.color.text_dark, theme))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.END
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12.dp() }
        }

        // tvTotal y tvNecesita se añaden al final, DESPUÉS de las filas de actividad
        card.addView(tvTotal)
        card.addView(tvNecesita)

        return MateriaCardRefs(
            card = card,
            titulo = tvTitulo,
            filasActividad = mutableListOf(),
            tvTotal = tvTotal,
            tvNecesita = tvNecesita
        )
    }

    private fun crearFilaEncabezado(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 4f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 10.dp() }

            addView(crearCeldaTexto("Actividad", 1.5f, android.view.Gravity.CENTER, true))
            addView(crearCeldaTexto("Nota",      1f,   android.view.Gravity.CENTER, true))
            addView(crearCeldaTexto("Valor",     0.8f, android.view.Gravity.CENTER, true))
            addView(crearCeldaTexto("Ganado",    0.7f, android.view.Gravity.CENTER, true))
        }
    }

    private fun asegurarFilasActividad(
        card: LinearLayout,
        filasActividad: MutableList<LinearLayout>,
        cantidadNecesaria: Int,
        tvTotal: TextView
    ) {
        while (filasActividad.size < cantidadNecesaria) {
            val nuevaFila = crearFilaActividadDinamica()
            // Insertar antes de tvTotal
            val posicionTotal = card.indexOfChild(tvTotal)
            card.addView(nuevaFila, posicionTotal)
            filasActividad.add(nuevaFila)
        }
    }

    private fun crearFilaActividadDinamica(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 4f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 6.dp() }

            addView(crearCeldaTexto("", 1.5f, android.view.Gravity.START,  false))
            addView(crearCeldaTexto("", 1f,   android.view.Gravity.CENTER, false))
            addView(crearCeldaTexto("", 0.8f, android.view.Gravity.CENTER, false))
            addView(crearCeldaTexto("", 0.7f, android.view.Gravity.CENTER, false))
        }
    }

    private fun crearCeldaTexto(texto: String, peso: Float, gravedad: Int, negrita: Boolean): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 12f
            setTextColor(resources.getColor(R.color.text_dark, theme))
            gravity = gravedad
            if (negrita) typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, peso)
        }
    }

    private fun crearCeldaActividad(peso: Float, centrado: Boolean) =
        crearCeldaTexto("", peso, if (centrado) android.view.Gravity.CENTER else android.view.Gravity.START, false)

    private fun Int.dp() = (this * resources.displayMetrics.density).toInt()

    // ── Firestore / lógica de negocio (sin cambios) ──────────────────────────

    private fun cargarPlanesActividades(document: DocumentSnapshot) {
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
                    "nombre"     to (actividad["nombre"]     as? String ?: "Actividad"),
                    "porcentaje" to (actividad["porcentaje"] as? Number)?.toDouble(),
                    "nota"       to (actividad["nota"]       as? Number)?.toDouble(),
                    "estado"     to (actividad["estado"]     as? String ?: "pendiente")
                )
            }.toMutableList()
            planesActividad.add(mutableMapOf("codigo" to codigo, "nombre" to nombre, "actividades" to actividades))
        }
    }

    private fun buscarPlanMateria(codigoMateria: String) =
        planesActividad.firstOrNull { it["codigo"] == codigoMateria }

    @Suppress("UNCHECKED_CAST")
    private fun obtenerActividades(plan: MutableMap<String, Any?>?) =
        (plan?.get("actividades") as? MutableList<MutableMap<String, Any?>>) ?: mutableListOf()

    private fun pintarTablaActividad(
        actividades: List<MutableMap<String, Any?>>,
        filasActividad: List<LinearLayout>,
        tvTotal: TextView,
        tvNecesita: TextView
    ) {
        var totalGanado = 0.0
        filasActividad.forEachIndexed { indice, row ->
            val actividad = actividades.getOrNull(indice)
            if (actividad == null) { row.visibility = View.GONE; return@forEachIndexed }
            row.visibility = View.VISIBLE

            val tvActividad = row.getChildAt(0) as TextView
            val tvNota     = row.getChildAt(1) as TextView
            val tvValor    = row.getChildAt(2) as TextView
            val tvEstado   = row.getChildAt(3) as TextView

            val nombre     = actividad["nombre"]     as? String ?: "Actividad"
            val porcentaje = (actividad["porcentaje"] as? Number)?.toDouble() ?: 0.0
            val nota       = (actividad["nota"]       as? Number)?.toDouble()
            val ganado     = nota?.let { (it * porcentaje) / 100.0 }

            tvActividad.text = nombre
            tvValor.text     = "${String.format("%.1f", porcentaje)}%"
            tvNota.text      = nota?.let { String.format("%.1f", it) } ?: ""
            tvEstado.text    = ganado?.let { String.format("%.2f", it) } ?: ""
            if (ganado != null) totalGanado += ganado
        }

        tvTotal.text = "Total: ${String.format("%.2f", totalGanado)}"

        val hayNota = actividades.any { (it["nota"] as? Number) != null }
        if (!hayNota) { tvNecesita.visibility = View.GONE; return }

        val (texto, visible) = calcularTextoNecesita(actividades)
        tvNecesita.visibility = if (visible) View.VISIBLE else View.GONE
        tvNecesita.text = texto
    }

    private fun calcularTextoNecesita(actividades: List<MutableMap<String, Any?>>): Pair<String, Boolean> {
        var puntosActuales    = 0.0
        var porcentajePendiente = 0.0
        actividades.forEach { actividad ->
            val porcentaje = (actividad["porcentaje"] as? Number)?.toDouble() ?: 0.0
            val nota       = (actividad["nota"]       as? Number)?.toDouble()
            if (nota == null) porcentajePendiente += porcentaje
            else              puntosActuales      += (nota * porcentaje) / 100.0
        }
        if (porcentajePendiente <= 0.0) {
            val estado = if (puntosActuales >= 6.0) "Aprobada" else "Reprobada"
            return "$estado - promedio final: ${String.format("%.2f", puntosActuales)}" to true
        }
        val notaNecesaria = (6.0 - puntosActuales) / (porcentajePendiente / 100.0)
        return when {
            notaNecesaria <= 0.0  -> "Ya aseguras aprobacion"                              to true
            notaNecesaria > 10.0  -> "Necesitas sacar mas de 10"                           to true
            else                  -> "Necesitas sacar: ${String.format("%.1f", notaNecesaria)}" to true
        }
    }

    private fun finalizarCicloSiCorresponde(
        document: DocumentSnapshot,
        materiasInscritas: List<String>,
        nombrePorCodigo: Map<String, String>,
        materiasPensum: List<MateriaPensum>
    ) {
        if (materiasInscritas.isEmpty()) return
        val resultados = materiasInscritas.mapNotNull { codigo ->
            val plan = buscarPlanMateria(codigo)
            evaluarResultadoMateria(codigo, nombrePorCodigo[codigo] ?: codigo, obtenerActividades(plan))
        }
        val todasCompletas = resultados.size == materiasInscritas.size && resultados.all { it.completa }
        if (!todasCompletas) return
        persistirCierreCiclo(document, resultados, materiasPensum)
    }

    private fun evaluarResultadoMateria(
        codigo: String, nombre: String, actividades: List<MutableMap<String, Any?>>
    ): ResultadoMateriaCiclo {
        var promedioFinal = 0.0
        var completa = actividades.isNotEmpty()
        actividades.forEach { actividad ->
            val porcentaje = (actividad["porcentaje"] as? Number)?.toDouble() ?: 0.0
            val nota       = (actividad["nota"]       as? Number)?.toDouble()
            if (nota == null) completa = false
            else              promedioFinal += (nota * porcentaje) / 100.0
        }
        return ResultadoMateriaCiclo(
            codigo = codigo, nombre = nombre, promedioFinal = promedioFinal,
            estado = if (promedioFinal >= 6.0) "aprobada" else "reprobada",
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

        val historialFinal = historialExistente + resultados.map { r ->
            mapOf<String, Any>("codigo" to r.codigo, "nombre" to r.nombre,
                "promedioFinal" to r.promedioFinal, "estado" to r.estado)
        }

        val materiasAprobadas = UserAcademicProfile.obtenerMateriasAprobadas(document).toMutableSet().also { set ->
            set.addAll(resultados.filter { it.estado == "aprobada" }.map { it.codigo })
        }

        val planesRestantes = planesActividad.filterNot { plan ->
            (plan["codigo"] as? String) in codigosFinalizados
        }

        val codigosCursados = historialFinal.mapNotNull { it["codigo"] as? String }.toSet()
        val cicloActual = UserAcademicProfile.calcularCicloActual(
            pensum = materiasPensum, codigosCursados = codigosCursados
        )

        val payload = mapOf(
            UserAcademicProfile.FIELD_ACADEMIC_HISTORY        to historialFinal,
            UserAcademicProfile.FIELD_APPROVED_SUBJECTS       to materiasAprobadas.toList(),
            UserAcademicProfile.FIELD_ENROLLED_SUBJECTS       to emptyList<String>(),
            UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN  to planesRestantes,
            UserAcademicProfile.FIELD_CURRENT_CYCLE           to cicloActual
        )

        db.collection(UserAcademicProfile.USERS_COLLECTION).document(userId)
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
            Toast.makeText(this, "No hay actividades configuradas", Toast.LENGTH_SHORT).show()
            return
        }

        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }
        val campos = mutableListOf<Pair<MutableMap<String, Any?>, EditText>>()

        actividades.forEach { actividad ->
            val nombre      = actividad["nombre"]     as? String ?: "Actividad"
            val porcentaje  = (actividad["porcentaje"] as? Number)?.toDouble() ?: 0.0
            val notaActual  = (actividad["nota"]       as? Number)?.toDouble()

            contenedor.addView(TextView(this).apply {
                text = "$nombre (${String.format("%.1f", porcentaje)}%)"
            })
            val campoNota = EditText(this).apply {
                hint = "Nota (0-10)"
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(notaActual?.let { String.format("%.1f", it) } ?: "")
            }
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
                for ((actividad, campoNota) in campos) {
                    val valor = campoNota.text.toString().trim()
                    val nota  = if (valor.isBlank()) null else valor.toDoubleOrNull()
                    if (nota != null && (nota < 0.0 || nota > 10.0)) {
                        Toast.makeText(this, "Las notas deben estar entre 0 y 10", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    actividad["nota"]   = nota
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
        db.collection(UserAcademicProfile.USERS_COLLECTION).document(userId)
            .set(mapOf(UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN to planesActividad), SetOptions.merge())
            .addOnSuccessListener { configurarVisibilidadBotonInscripcion() }
            .addOnFailureListener { Toast.makeText(this, "No se pudo guardar la nota", Toast.LENGTH_LONG).show() }
    }
}
package com.example.edupath_invest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import kotlin.math.roundToInt

class DashboardActivity : AppCompatActivity() {

    private data class RiesgoMateria(
        val codigo: String,
        val nombre: String,
        val promedioActual: Double,
        val notaNecesaria: Double,
        val porcentajePendiente: Double,
        val desbloqueos: Int,
        val esRutaCritica: Boolean
    )

    private class CenterImageSpan(drawable: Drawable) : ImageSpan(drawable) {
        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            val rect = drawable.bounds
            if (fm != null) {
                val fmPaint = paint.fontMetricsInt
                val fontHeight = fmPaint.descent - fmPaint.ascent
                val drHeight = rect.height()
                val centerY = fmPaint.ascent + fontHeight / 2
                fm.ascent = centerY - drHeight / 2
                fm.top = fm.ascent
                fm.descent = centerY + drHeight / 2
                fm.bottom = fm.descent
            }
            return rect.right
        }

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            canvas.save()
            val fmPaint = paint.fontMetricsInt
            val fontHeight = fmPaint.descent - fmPaint.ascent
            val centerY = y + fmPaint.ascent + fontHeight / 2
            val transY = centerY - drawable.bounds.height() / 2
            canvas.translate(x, transY.toFloat())
            drawable.draw(canvas)
            canvas.restore()
        }
    }

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvGreeting: TextView
    private lateinit var progressCircular: ProgressBar
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var tvCurrentCycle: TextView
    private lateinit var tvRecomendadas: TextView
    private lateinit var tvAlertaRiesgoTitulo: TextView
    private lateinit var tvAlertaRiesgoDetalle: TextView

    private val imageGetter = Html.ImageGetter { source ->
        val resId = resources.getIdentifier(source, "drawable", packageName)
        if (resId != 0) {
            val drawable = ContextCompat.getDrawable(this, resId)
            drawable?.apply {
                val size = (14 * resources.displayMetrics.scaledDensity).toInt()
                setBounds(0, 0, size, size)
            }
        } else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvGreeting = findViewById(R.id.tvGreeting)
        progressCircular = findViewById(R.id.progressCircular)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        tvProgressText = findViewById(R.id.tvProgressText)
        tvCurrentCycle = findViewById(R.id.tvCurrentCycle)
        tvRecomendadas = findViewById(R.id.tvRecomendadas)
        tvAlertaRiesgoTitulo = findViewById(R.id.tvAlertaRiesgoTitulo)
        tvAlertaRiesgoDetalle = findViewById(R.id.tvAlertaRiesgoDetalle)

        cargarUsuario()
        BottomNavHelper.setup(this, "home")
    }

    private fun String.formatoNombreCachinbon(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }

    private fun centrarImagenes(spanned: android.text.Spanned): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(spanned)
        val imageSpans = spannable.getSpans(0, spannable.length, ImageSpan::class.java)
        for (span in imageSpans) {
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            spannable.setSpan(CenterImageSpan(span.drawable), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.removeSpan(span)
        }
        return spannable
    }

    private fun obtenerCicloPorFecha(): String {
        val mes = Calendar.getInstance().get(Calendar.MONTH) // Enero es 0, Mayo es 4, etc.
        return when (mes) {
            in Calendar.JANUARY..Calendar.MAY -> "Ciclo 01"
            in Calendar.JUNE..Calendar.NOVEMBER -> "Ciclo 02"
            else -> "Interciclo / Vacaciones"
        }
    }

    private fun cargarUsuario() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val nombre = doc.getString("nombres") ?: ""
                    val apellidos = doc.getString("apellidos") ?: ""
                    val primerNombre = (nombre.split(" ").firstOrNull() ?: "").formatoNombreCachinbon()
                    val primerApellido = (apellidos.split(" ").firstOrNull() ?: "").formatoNombreCachinbon()

                    tvGreeting.text = "$primerNombre $primerApellido"

                    val materiasCursadas = (doc.get(UserAcademicProfile.FIELD_ACADEMIC_HISTORY) as? List<*>)?.size ?: 0
                    val anio = UserAcademicProfile.obtenerAnioPensum(doc)
                    val materias = UserAcademicProfile.obtenerMateriasPensum(anio)
                    val totalMaterias = materias.size

                    val porcentajeCursado = if (totalMaterias == 0) 0 else ((materiasCursadas.toDouble() / totalMaterias) * 100).roundToInt()

                    UserAcademicProfile.aplicarEstadosPensum(doc, materias)

                    progressCircular.progress = porcentajeCursado
                    tvProgressPercent.text = "$porcentajeCursado%"
                    tvProgressText.text = "Has completado el $porcentajeCursado% de tu carrera"

                    // Aquí aplicamos la nueva lógica basada en fecha
                    tvCurrentCycle.text = obtenerCicloPorFecha()

                    val materiasInscritas = UserAcademicProfile.obtenerMateriasInscritas(doc)
                    val materiasPriorizadas = GrafoHelper.obtenerMateriasPriorizadas(materias, Int.MAX_VALUE)
                    val rutaCritica = GrafoHelper.obtenerRutaCritica(materias).map { it.codigo }.toSet()

                    val htmlSugerencias = StringBuilder()
                    if (materiasInscritas.isNotEmpty()) {
                        val enCurso = materias.filter { it.codigo in materiasInscritas }
                        if (enCurso.isEmpty()) {
                            htmlSugerencias.append("No tienes materias inscritas.")
                        } else {
                            enCurso.forEachIndexed { i, m ->
                                htmlSugerencias.append("<b>&#8226; ${m.nombre}</b>")
                                if (i < enCurso.size - 1) htmlSugerencias.append("<br><br>")
                            }
                        }
                    } else {
                        if (materiasPriorizadas.isEmpty()) {
                            htmlSugerencias.append("No hay materias habilitadas.")
                        } else {
                            materiasPriorizadas.take(5).forEachIndexed { i, p ->
                                val m = p.materia
                                val iconName = if (p.dependenciasTotales > 0) "candado" else "trofeo"

                                htmlSugerencias.append("<b>${m.nombre}</b>")
                                if (m.codigo in rutaCritica) htmlSugerencias.append(" <font color='#FFCD5E'>[Prioridad]</font>")

                                val sub = if (p.dependenciasTotales > 0)
                                    "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=\"$iconName\">&nbsp;&nbsp;Habilita ${p.dependenciasTotales} materias"
                                else "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=\"$iconName\">&nbsp;&nbsp;Materia de cierre"
                                htmlSugerencias.append(sub)

                                if (i < 4 && i < materiasPriorizadas.size - 1) htmlSugerencias.append("<br><br>")
                            }
                        }
                    }
                    val spannedSugerencias = HtmlCompat.fromHtml(htmlSugerencias.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, null)
                    tvRecomendadas.text = centrarImagenes(spannedSugerencias)

                    val riesgos = construirAlertasRiesgo(doc, materias, materiasInscritas)
                    pintarCardRiesgo(riesgos, materiasInscritas)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun construirAlertasRiesgo(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        materiasPensum: List<MateriaPensum>,
        materiasInscritas: Set<String>
    ): List<RiesgoMateria> {
        if (materiasInscritas.isEmpty()) return emptyList()

        val rutaCritica = GrafoHelper.obtenerRutaCritica(materiasPensum).map { it.codigo }.toSet()
        val nombrePorCodigo = materiasPensum.associate { it.codigo to it.nombre }
        val planActividades = (doc.get(UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN) as? List<*>) ?: emptyList<Any>()

        return planActividades.mapNotNull { planRaw ->
            val plan = planRaw as? Map<*, *> ?: return@mapNotNull null
            val codigo = plan["codigo"] as? String ?: return@mapNotNull null
            if (codigo !in materiasInscritas) return@mapNotNull null

            val actividades = (plan["actividades"] as? List<*>) ?: emptyList<Any>()
            var puntosActuales = 0.0
            var porcentajePendiente = 0.0

            actividades.forEach { act ->
                val a = act as? Map<*, *> ?: return@forEach
                val porc = (a["porcentaje"] as? Number)?.toDouble() ?: 0.0
                val nota = (a["nota"] as? Number)?.toDouble()
                if (nota == null) porcentajePendiente += porc else puntosActuales += (nota * porc) / 100.0
            }

            val notaNec = if (porcentajePendiente <= 0.0) 0.0 else (6.0 - puntosActuales) / (porcentajePendiente / 100.0)

            if (porcentajePendiente > 0.0 && notaNec > 10.0) {
                RiesgoMateria(codigo, nombrePorCodigo[codigo] ?: codigo, puntosActuales, notaNec, porcentajePendiente, GrafoHelper.contarDesbloqueos(codigo, materiasPensum), codigo in rutaCritica)
            } else null
        }.sortedWith(compareByDescending<RiesgoMateria> { it.esRutaCritica }.thenByDescending { it.desbloqueos })
    }

    private fun pintarCardRiesgo(riesgos: List<RiesgoMateria>, materiasInscritas: Set<String>) {
        if (materiasInscritas.isEmpty()) {
            tvAlertaRiesgoTitulo.text = "Estado Académico"
            tvAlertaRiesgoDetalle.text = "Aún no tienes materias inscritas."
            return
        }

        if (riesgos.isEmpty()) {
            tvAlertaRiesgoTitulo.text = "Estado Académico"
            tvAlertaRiesgoDetalle.text = "¡Todo excelente! No tienes materias en riesgo."
            return
        }

        tvAlertaRiesgoTitulo.text = if (riesgos.size == materiasInscritas.size) "Alerta Crítica" else "Materias en Riesgo"

        val html = StringBuilder()
        riesgos.forEachIndexed { i, r ->
            html.append("<img src=\"alerta\">&nbsp;&nbsp;")
            html.append("<b>${r.nombre}</b>")
            if (r.esRutaCritica) html.append(" <font color='#FFCD5E'>(Ruta Crítica)</font>")
            html.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Necesitas ${String.format("%.1f", r.notaNecesaria)} en lo que falta.")
            if (i < riesgos.size - 1) html.append("<br><br>")
        }
        val spannedRiesgos = HtmlCompat.fromHtml(html.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, null)
        tvAlertaRiesgoDetalle.text = centrarImagenes(spannedRiesgos)
    }
}
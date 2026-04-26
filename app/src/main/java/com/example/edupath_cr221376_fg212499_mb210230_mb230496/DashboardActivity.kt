package com.example.edupath_cr221376_fg212499_mb210230_mb230496

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
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
import android.text.Html

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

    private val imageGetter = Html.ImageGetter { source: String? ->
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
        val mes = Calendar.getInstance().get(Calendar.MONTH)
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

                    val primerNombre = nombre.split(" ").firstOrNull()?.formatoNombreCachinbon() ?: ""
                    val primerApellido = apellidos.split(" ").firstOrNull()?.formatoNombreCachinbon() ?: ""

                    tvGreeting.text = "$primerNombre $primerApellido"

                    val materiasCursadas = (doc.get(UserAcademicProfile.FIELD_ACADEMIC_HISTORY) as? List<*>)?.size ?: 0
                    val anio = UserAcademicProfile.obtenerAnioPensum(doc)
                    val materias = UserAcademicProfile.obtenerMateriasPensum(this, anio)

                    UserAcademicProfile.aplicarEstadosPensum(doc, materias)

                    val porcentaje = if (materias.isEmpty()) 0 else ((materiasCursadas.toDouble() / materias.size) * 100).roundToInt()

                    progressCircular.progress = porcentaje
                    tvProgressPercent.text = "$porcentaje%"
                    tvProgressText.text = "Has completado el $porcentaje% de tu carrera"
                    tvCurrentCycle.text = obtenerCicloPorFecha()

                    val inscritas = UserAcademicProfile.obtenerMateriasInscritas(doc)
                    val priorizadas = GrafoHelper.obtenerMateriasPriorizadas(materias, Int.MAX_VALUE)
                    val rutaCritica = GrafoHelper.obtenerRutaCritica(materias).map { it.codigo }.toSet()

                    val html = StringBuilder()

                    fun agregarMatriculaSiAplica(m: MateriaPensum) {
                        if (m.numMatricula >= 2) {
                            html.append("<br>&nbsp;&nbsp;&nbsp;<img src=\"alerta\"> Matrícula ${m.numMatricula}")
                        }
                    }

                    if (inscritas.isNotEmpty()) {

                        val enCurso = materias.filter { it.codigo in inscritas }

                        enCurso.forEachIndexed { i, m ->
                            html.append("<b>&#8226; ${m.nombre}</b>")
                            agregarMatriculaSiAplica(m)

                            if (i < enCurso.size - 1) html.append("<br><br>")
                        }

                    } else {

                        priorizadas.take(5).forEachIndexed { i, p ->

                            val m = p.materia
                            val icono = if (p.dependenciasTotales > 0) "candado" else "trofeo"

                            html.append("<b>${m.nombre}</b>")

                            agregarMatriculaSiAplica(m)

                            if (m.codigo in rutaCritica) {
                                html.append(" <font color='#FFCD5E'>[Prioridad]</font>")
                            }

                            val sub = if (p.dependenciasTotales > 0)
                                "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=\"$icono\"> Habilita ${p.dependenciasTotales}"
                            else
                                "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src=\"$icono\"> Materia final"

                            html.append(sub)

                            if (i < 4) html.append("<br><br>")
                        }
                    }

                    val spanned = HtmlCompat.fromHtml(html.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, null)
                    tvRecomendadas.text = centrarImagenes(spanned)

                    val riesgos = construirAlertasRiesgo(doc, materias, inscritas)
                    pintarCardRiesgo(riesgos, inscritas)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun construirAlertasRiesgo(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        materias: List<MateriaPensum>,
        inscritas: Set<String>
    ): List<RiesgoMateria> {

        if (inscritas.isEmpty()) return emptyList()

        val rutaCritica = GrafoHelper.obtenerRutaCritica(materias).map { it.codigo }.toSet()
        val nombres = materias.associate { it.codigo to it.nombre }
        val planes = (doc.get(UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN) as? List<*>) ?: emptyList<Any>()

        return planes.mapNotNull {
            val p = it as? Map<*, *> ?: return@mapNotNull null
            val codigo = p["codigo"] as? String ?: return@mapNotNull null
            if (codigo !in inscritas) return@mapNotNull null

            val acts = (p["actividades"] as? List<*>) ?: emptyList<Any>()

            var puntos = 0.0
            var faltante = 0.0

            acts.forEach { aRaw ->
                val a = aRaw as? Map<*, *> ?: return@forEach
                val porc = (a["porcentaje"] as? Number)?.toDouble() ?: 0.0
                val nota = (a["nota"] as? Number)?.toDouble()

                if (nota == null) faltante += porc else puntos += (nota * porc) / 100.0
            }

            val nec = if (faltante == 0.0) 0.0 else (6.0 - puntos) / (faltante / 100.0)

            if (faltante > 0 && nec > 10.0) {
                RiesgoMateria(
                    codigo,
                    nombres[codigo] ?: codigo,
                    puntos,
                    nec,
                    faltante,
                    GrafoHelper.contarDesbloqueos(codigo, materias),
                    codigo in rutaCritica
                )
            } else null

        }.sortedByDescending { it.esRutaCritica }
    }

    private fun pintarCardRiesgo(riesgos: List<RiesgoMateria>, inscritas: Set<String>) {

        if (inscritas.isEmpty()) {
            tvAlertaRiesgoTitulo.text = "Estado Académico"
            tvAlertaRiesgoDetalle.text = "Sin materias inscritas."
            return
        }

        if (riesgos.isEmpty()) {
            tvAlertaRiesgoTitulo.text = "Estado Académico"
            tvAlertaRiesgoDetalle.text = "Todo bien."
            return
        }

        tvAlertaRiesgoTitulo.text = "Materias en Riesgo"

        val html = StringBuilder()

        riesgos.forEachIndexed { i, r ->
            html.append("<img src=\"alerta\"> <b>${r.nombre}</b>")
            html.append("<br>&nbsp;&nbsp;&nbsp; Necesitas ${String.format("%.1f", r.notaNecesaria)}")

            if (i < riesgos.size - 1) html.append("<br><br>")
        }

        val spanned = HtmlCompat.fromHtml(html.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, null)
        tvAlertaRiesgoDetalle.text = centrarImagenes(spanned)
    }
}
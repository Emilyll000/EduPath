package com.example.edupath_invest

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvGreeting: TextView
    private lateinit var progressCircular: ProgressBar
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var tvCurrentCycle: TextView
    private lateinit var tvSubjectsEnrolled: TextView
    private lateinit var tvRecomendadas: TextView
    private lateinit var tvAlertaRiesgoTitulo: TextView
    private lateinit var tvAlertaRiesgoDetalle: TextView

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
        tvSubjectsEnrolled = findViewById(R.id.tvSubjectsEnrolled)
        tvRecomendadas = findViewById(R.id.tvRecomendadas)
        tvAlertaRiesgoTitulo = findViewById(R.id.tvAlertaRiesgoTitulo)
        tvAlertaRiesgoDetalle = findViewById(R.id.tvAlertaRiesgoDetalle)

        cargarUsuario()
        BottomNavHelper.setup(this, "home")
    }

    private fun cargarUsuario() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { doc ->

                val nombre = doc.getString("nombres") ?: ""
                val apellidos = doc.getString("apellidos") ?: ""
                val materiasCursadas = (doc.get(UserAcademicProfile.FIELD_ACADEMIC_HISTORY) as? List<*>)?.size ?: 0
                val cicloActual = doc.getLong(UserAcademicProfile.FIELD_CURRENT_CYCLE)?.toInt() ?: 1

                val nombreCompleto = listOf(nombre, apellidos)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                tvGreeting.text = "Hola, $nombreCompleto"

                val anio = UserAcademicProfile.obtenerAnioPensum(doc)

                val materias = UserAcademicProfile.obtenerMateriasPensum(anio)
                val totalMaterias = materias.size
                val porcentajeCursado = if (totalMaterias == 0) {
                    0
                } else {
                    ((materiasCursadas.toDouble() / totalMaterias) * 100).roundToInt()
                }

                UserAcademicProfile.aplicarEstadosPensum(doc, materias)

                progressCircular.progress = porcentajeCursado
                tvProgressPercent.text = "$porcentajeCursado%"
                tvProgressText.text = "Has completado el $porcentajeCursado% de tu carrera"
                tvCurrentCycle.text = "Ciclo actual: $cicloActual"

                val materiasInscritas = UserAcademicProfile.obtenerMateriasInscritas(doc)
                val materiasPriorizadas = GrafoHelper.obtenerMateriasPriorizadas(
                    materias = materias,
                    maximo = Int.MAX_VALUE
                )
                val materiasEnCurso = materias.filter { it.codigo in materiasInscritas }
                val rutaCritica = GrafoHelper.obtenerRutaCritica(materias).map { it.codigo }.toSet()

                val mostrarInscritas = materiasInscritas.isNotEmpty()
                val cantidadMostrada = if (mostrarInscritas) materiasEnCurso.size else materiasPriorizadas.size
                val etiquetaCantidad = if (mostrarInscritas) {
                    if (cantidadMostrada == 1) "1 materia cursando" else "$cantidadMostrada materias cursando"
                } else {
                    if (cantidadMostrada == 1) {
                        "1 materia habilitada (ordenada por peso)"
                    } else {
                        "$cantidadMostrada materias habilitadas (ordenadas por peso)"
                    }
                }

                val texto = if (mostrarInscritas) {
                    if (materiasEnCurso.isEmpty()) {
                        "No hay materias inscritas registradas por ahora."
                    } else {
                        materiasEnCurso.joinToString("\n") { "• ${it.nombre}" }
                    }
                } else {
                    if (materiasPriorizadas.isEmpty()) {
                        "No hay materias habilitadas para recomendar por ahora."
                    } else {
                        materiasPriorizadas.joinToString("\n") { prioridad ->
                            val materia = prioridad.materia
                            val marcaRutaCritica = if (materia.codigo in rutaCritica) " [Ruta critica]" else ""

                            "• ${materia.nombre}$marcaRutaCritica\n" +
                                "  Peso ${prioridad.peso} | desbloquea ${prioridad.dependenciasTotales} materias | profundidad ${prioridad.longitudRutaCritica}"
                        }
                    }
                }

                tvSubjectsEnrolled.text = etiquetaCantidad
                tvRecomendadas.text = texto

                val riesgos = construirAlertasRiesgo(doc, materias, materiasInscritas)
                pintarCardRiesgo(riesgos, materiasInscritas)
            }
    }

    private fun construirAlertasRiesgo(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        materiasPensum: List<MateriaPensum>,
        materiasInscritas: Set<String>
    ): List<RiesgoMateria> {
        if (materiasInscritas.isEmpty()) {
            return emptyList()
        }

        val rutaCritica = GrafoHelper.obtenerRutaCritica(materiasPensum).map { it.codigo }.toSet()
        val nombrePorCodigo = materiasPensum.associate { it.codigo to it.nombre }
        val planActividades = (doc.get(UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN) as? List<*>) ?: emptyList<Any>()

        return planActividades.mapNotNull { planRaw ->
            val plan = planRaw as? Map<*, *> ?: return@mapNotNull null
            val codigo = plan["codigo"] as? String ?: return@mapNotNull null
            if (codigo !in materiasInscritas) {
                return@mapNotNull null
            }

            val actividades = (plan["actividades"] as? List<*>) ?: emptyList<Any>()

            var puntosActuales = 0.0
            var porcentajePendiente = 0.0

            actividades.forEach { actividadRaw ->
                val actividad = actividadRaw as? Map<*, *> ?: return@forEach
                val porcentaje = (actividad["porcentaje"] as? Number)?.toDouble() ?: 0.0
                val nota = (actividad["nota"] as? Number)?.toDouble()

                if (nota == null) {
                    porcentajePendiente += porcentaje
                } else {
                    puntosActuales += (nota * porcentaje) / 100.0
                }
            }

            val notaNecesaria = if (porcentajePendiente <= 0.0) {
                0.0
            } else {
                (6.0 - puntosActuales) / (porcentajePendiente / 100.0)
            }

            val promedioActual = puntosActuales
            val tienePendientesSinNota = porcentajePendiente > 0.0
            val enRiesgo = tienePendientesSinNota && notaNecesaria > 10.0
            if (!enRiesgo) {
                return@mapNotNull null
            }

            RiesgoMateria(
                codigo = codigo,
                nombre = nombrePorCodigo[codigo] ?: codigo,
                promedioActual = promedioActual,
                notaNecesaria = notaNecesaria,
                porcentajePendiente = porcentajePendiente,
                desbloqueos = GrafoHelper.contarDesbloqueos(codigo, materiasPensum),
                esRutaCritica = codigo in rutaCritica
            )
        }.sortedWith(
            compareByDescending<RiesgoMateria> { it.esRutaCritica }
                .thenByDescending { it.desbloqueos }
                .thenByDescending { it.notaNecesaria }
                .thenBy { it.nombre }
        )
    }

    private fun pintarCardRiesgo(
        riesgos: List<RiesgoMateria>,
        materiasInscritas: Set<String>
    ) {
        if (materiasInscritas.isEmpty()) {
            tvAlertaRiesgoTitulo.text = "Alerta academica"
            tvAlertaRiesgoDetalle.text = "No hay materias inscritas para evaluar riesgo."
            return
        }

        if (riesgos.isEmpty()) {
            tvAlertaRiesgoTitulo.text = "Alerta academica"
            tvAlertaRiesgoDetalle.text = "Sin alertas: no hay materias inscritas con actividades pendientes en riesgo de reprobacion."
            return
        }

        val todasBajas = riesgos.size == materiasInscritas.size
        tvAlertaRiesgoTitulo.text = if (todasBajas) {
            "Alerta critica"
        } else {
            "Materias en riesgo"
        }

        val cabecera = if (todasBajas) {
            "Las siguientes materias inscritas estan en riesgo de reprobacion, prioriza estas:"
        } else {
            "Las siguientes materias inscritas estan en riesgo de reprobacion, prioriza estas:"
        }

        val detalle = riesgos.joinToString("\n") { riesgo ->
            val ruta = if (riesgo.esRutaCritica) " [Ruta critica]" else ""
            val necesita = "Necesitas sacar ${String.format("%.1f", riesgo.notaNecesaria)}"

            "• ${riesgo.nombre}$ruta\n  Promedio ${String.format("%.2f", riesgo.promedioActual)} | ${necesita} | pendiente ${String.format("%.1f", riesgo.porcentajePendiente)}%"
        }

        tvAlertaRiesgoDetalle.text = "$cabecera\n$detalle"
    }
}
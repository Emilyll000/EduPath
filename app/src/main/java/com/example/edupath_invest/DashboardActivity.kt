package com.example.edupath_invest

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class DashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvGreeting: TextView
    private lateinit var progressCircular: ProgressBar
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var tvCurrentCycle: TextView
    private lateinit var tvSubjectsEnrolled: TextView
    private lateinit var tvRecomendadas: TextView

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
                val materiasDisponibles = materias.filter { it.estado == EstadoMateria.HABILITADA }
                val materiasEnCurso = materias.filter { it.codigo in materiasInscritas }

                val mostrarInscritas = materiasInscritas.isNotEmpty()
                val cantidadMostrada = if (mostrarInscritas) materiasEnCurso.size else materiasDisponibles.size
                val etiquetaCantidad = if (mostrarInscritas) {
                    if (cantidadMostrada == 1) "1 materia cursando" else "$cantidadMostrada materias cursando"
                } else {
                    if (cantidadMostrada == 1) "1 materia disponible" else "$cantidadMostrada materias disponibles"
                }

                val texto = if (mostrarInscritas) {
                    if (materiasEnCurso.isEmpty()) {
                        "No hay materias inscritas registradas por ahora."
                    } else {
                        materiasEnCurso.joinToString("\n") { "• ${it.nombre}" }
                    }
                } else {
                    if (materiasDisponibles.isEmpty()) {
                        "No hay materias disponibles con prerrequisitos completos por ahora."
                    } else {
                        materiasDisponibles.joinToString("\n") { "• ${it.nombre}" }
                    }
                }

                tvSubjectsEnrolled.text = etiquetaCantidad
                tvRecomendadas.text = texto
            }
    }
}
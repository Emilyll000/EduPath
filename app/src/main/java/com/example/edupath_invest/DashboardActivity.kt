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
                val materiasAprobadas = UserAcademicProfile.obtenerMateriasAprobadas(doc)
                val materiasCursadas = (doc.get(UserAcademicProfile.FIELD_ACADEMIC_HISTORY) as? List<*>)?.size ?: 0
                val cicloActual = doc.getLong(UserAcademicProfile.FIELD_CURRENT_CYCLE)?.toInt() ?: 1

                val nombreCompleto = listOf(nombre, apellidos)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                tvGreeting.text = "Hola, $nombreCompleto"

                val anio = UserAcademicProfile.obtenerAnioPensum(doc)

                val materias = PensumRepository.obtenerPensum(anio)
                val totalMaterias = materias.size
                val porcentajeCursado = if (totalMaterias == 0) {
                    0
                } else {
                    ((materiasCursadas.toDouble() / totalMaterias) * 100).roundToInt()
                }

                materias.forEach { materia ->
                    if (materia.codigo in materiasAprobadas) {
                        materia.estado = EstadoMateria.APROBADA
                    }
                }
                GrafoHelper.actualizarEstados(materias)

                progressCircular.progress = porcentajeCursado
                tvProgressPercent.text = "$porcentajeCursado%"
                tvProgressText.text = "Has completado el $porcentajeCursado% de tu carrera"
                tvCurrentCycle.text = "Ciclo actual: $cicloActual"
                tvSubjectsEnrolled.text = "$materiasCursadas materias cursadas"

                val recomendadas = GrafoHelper.obtenerRecomendadas(materias)

                val texto = if (recomendadas.isEmpty()) {
                    "No hay recomendaciones por ahora."
                } else {
                    recomendadas.joinToString("\n") { "• ${it.nombre}" }
                }

                tvRecomendadas.text = texto
            }
    }
}
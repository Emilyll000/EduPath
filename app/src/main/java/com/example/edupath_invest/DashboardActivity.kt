package com.example.edupath_invest

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvGreeting: TextView
    private lateinit var tvRecomendadas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvGreeting = findViewById(R.id.tvGreeting)
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
                val plan = doc.getString("plan") ?: "Pénsum 2023"

                tvGreeting.text = "Hola, $nombre"

                val anio = if (plan.contains("2016")) 2016 else 2023

                val materias = PensumRepository.obtenerPensum(anio)
                GrafoHelper.actualizarEstados(materias)

                val recomendadas = GrafoHelper.obtenerRecomendadas(materias)

                val texto = recomendadas.joinToString("\n") { "• ${it.nombre}" }

                tvRecomendadas.text = texto
            }
    }
}
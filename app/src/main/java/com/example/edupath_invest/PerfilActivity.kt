package com.example.edupath_invest

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class PerfilActivity : AppCompatActivity() {

    private lateinit var btnSalir: ImageButton
    private lateinit var btnEditar: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        btnSalir = findViewById(R.id.btnSalir)
        btnEditar = findViewById(R.id.btnEditar)

        configurarEventos()
        BottomNavHelper.setup(this, "profile")
    }

    private fun configurarEventos() {
        btnSalir.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        btnEditar.setOnClickListener {
            // Aquí luego se puedes abrir una pantalla para editar perfil
        }
    }
}
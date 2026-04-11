package com.example.edupath_invest

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etCarnet: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvRecover: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etCarnet = findViewById(R.id.etCarnet)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        tvRecover = findViewById(R.id.tvRecover)

        btnLogin.setOnClickListener {
            iniciarSesion()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvRecover.setOnClickListener {
            Toast.makeText(this, "Función de recuperación pendiente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun iniciarSesion() {
        val usuario = etCarnet.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
            autenticarConCorreo(usuario, password)
            return
        }

        db.collection("usuarios")
            .whereEqualTo("carnet", usuario)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                val correo = documents.documents.firstOrNull()?.getString("correo")

                if (correo.isNullOrBlank()) {
                    Toast.makeText(this, "No existe una cuenta asociada a ese carnet", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                autenticarConCorreo(correo, password)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error buscando el usuario: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun autenticarConCorreo(correo: String, password: String) {
        auth.signInWithEmailAndPassword(correo, password)
            .addOnSuccessListener {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.toFirebaseAuthMessage(), Toast.LENGTH_LONG).show()
            }
    }
}
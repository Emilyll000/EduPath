package com.example.edupath_invest

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etNombres: EditText
    private lateinit var etApellidos: EditText
    private lateinit var etCarnet: EditText
    private lateinit var etCorreo: EditText
    private lateinit var spPlan: Spinner
    private lateinit var etPassword: EditText
    private lateinit var etRepeatPassword: EditText
    private lateinit var btnRegisterArrow: ImageButton
    private lateinit var tvRegistrar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNombres = findViewById(R.id.etNombres)
        etApellidos = findViewById(R.id.etApellidos)
        etCarnet = findViewById(R.id.etCarnet)
        etCorreo = findViewById(R.id.etCorreo)
        spPlan = findViewById(R.id.spPlan)
        etPassword = findViewById(R.id.etPassword)
        etRepeatPassword = findViewById(R.id.etRepeatPassword)
        btnRegisterArrow = findViewById(R.id.btnRegisterArrow)
        tvRegistrar = findViewById(R.id.tvRegistrar)

        configurarSpinner()

        btnRegisterArrow.setOnClickListener {
            registrarUsuario()
        }

        tvRegistrar.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun configurarSpinner() {
        val planes = listOf(
            "Selecciona tu plan",
            "Pénsum 2016",
            "Pénsum 2023"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            planes
        )

        spPlan.adapter = adapter
    }

    private fun registrarUsuario() {
        val nombres = etNombres.text.toString().trim()
        val apellidos = etApellidos.text.toString().trim()
        val carnet = etCarnet.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val plan = spPlan.selectedItem.toString()
        val password = etPassword.text.toString().trim()
        val repeatPassword = etRepeatPassword.text.toString().trim()

        if (nombres.isEmpty() ||
            apellidos.isEmpty() ||
            carnet.isEmpty() ||
            correo.isEmpty() ||
            password.isEmpty() ||
            repeatPassword.isEmpty()
        ) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (plan == "Selecciona tu plan") {
            Toast.makeText(this, "Selecciona un plan", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != repeatPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(correo, password)
            .addOnSuccessListener {
                val userId = auth.currentUser?.uid ?: return@addOnSuccessListener

                val usuario = hashMapOf(
                    "nombres" to nombres,
                    "apellidos" to apellidos,
                    "carnet" to carnet,
                    "correo" to correo,
                    "plan" to plan
                )

                db.collection("usuarios")
                    .document(userId)
                    .set(usuario)
                    .addOnSuccessListener {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error guardando datos: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
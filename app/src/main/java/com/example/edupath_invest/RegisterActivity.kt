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

private data class PlanSpinnerItem(
    val id: Int?,
    val nombre: String
) {
    override fun toString(): String = nombre
}

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
    private var planesDisponibles: List<PlanSpinnerItem> = emptyList()

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
        planesDisponibles = listOf(
            PlanSpinnerItem(
                id = null,
                nombre = "Selecciona tu plan"
            )
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            planesDisponibles
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spPlan.adapter = adapter

        cargarPlanes()
    }

    private fun cargarPlanes() {
        db.collection("planes")
            .orderBy("id")
            .get()
            .addOnSuccessListener { documents ->
                val planes = documents.mapNotNull { document ->
                    val id = document.getLong("id")?.toInt() ?: return@mapNotNull null
                    val nombre = document.getString("nombre") ?: return@mapNotNull null

                    PlanSpinnerItem(id = id, nombre = nombre)
                }

                planesDisponibles = listOf(
                    PlanSpinnerItem(id = null, nombre = "Selecciona tu plan")
                ) + planes

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    planesDisponibles
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spPlan.adapter = adapter
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "No se pudieron cargar los planes: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun registrarUsuario() {
        val nombres = etNombres.text.toString().trim()
        val apellidos = etApellidos.text.toString().trim()
        val carnet = etCarnet.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val planSeleccionado = spPlan.selectedItem as? PlanSpinnerItem
        val planId = planSeleccionado?.id
        val anioPensum = UserAcademicProfile.obtenerAnioPensum(planId)
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

        if (planId == null) {
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
                    UserAcademicProfile.FIELD_PLAN_ID to planId,
                    UserAcademicProfile.FIELD_PLAN_YEAR to anioPensum,
                    UserAcademicProfile.FIELD_FIRST_LOGIN to true,
                    UserAcademicProfile.FIELD_ENTRY_TYPE to ""
                )

                db.collection(UserAcademicProfile.USERS_COLLECTION)
                    .document(userId)
                    .set(usuario)
                    .addOnSuccessListener {
                        startActivity(Intent(this, FirstLoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error guardando datos: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.toFirebaseAuthMessage(), Toast.LENGTH_LONG).show()
            }
    }
}
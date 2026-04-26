package com.example.edupath_cr221376_fg212499_mb210230_mb230496

import android.graphics.BitmapFactory
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URL

class PerfilActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var imgAvatar: ImageView
    private lateinit var tvAvatarIniciales: TextView
    private lateinit var tvNombrePerfil: TextView
    private lateinit var tvApellidoPerfil: TextView
    private lateinit var tvCarreraPerfil: TextView
    private lateinit var tvCorreoPerfil: TextView
    private lateinit var tvCarnetPerfil: TextView
    private lateinit var tvPlanPerfil: TextView
    private lateinit var btnSalir: Button
    private lateinit var btnEditar: Button

    private var modoEdicion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        imgAvatar = findViewById(R.id.imgAvatar)
        tvAvatarIniciales = findViewById(R.id.tvAvatarIniciales)
        tvNombrePerfil = findViewById(R.id.tvNombrePerfil)
        tvApellidoPerfil = findViewById(R.id.tvApellidoPerfil)
        tvCarreraPerfil = findViewById(R.id.tvCarreraPerfil)
        tvCorreoPerfil = findViewById(R.id.tvCorreoPerfil)
        tvCarnetPerfil = findViewById(R.id.tvCarnetPerfil)
        tvPlanPerfil = findViewById(R.id.tvPlanPerfil)
        btnSalir = findViewById(R.id.btnSalir)
        btnEditar = findViewById(R.id.btnEditar)

        configurarEventos()
        cargarPerfil()
        BottomNavHelper.setup(this, "profile")
    }

    private fun cargarPerfil() {
        val userId = auth.currentUser?.uid ?: return

        db.collection(UserAcademicProfile.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val nombres = doc.getString("nombres").orEmpty().trim()
                val apellidos = doc.getString("apellidos").orEmpty().trim()

                val correo = doc.getString("correo").orEmpty().ifBlank { auth.currentUser?.email.orEmpty() }
                val carnet = doc.getString("carnet").orEmpty()
                val anioPensum = UserAcademicProfile.obtenerAnioPensum(doc)
                val textoPlan = "Plan $anioPensum"

                tvNombrePerfil.setText(nombres.ifBlank { "-" })
                tvApellidoPerfil.setText(apellidos.ifBlank { "-" })
                tvCarreraPerfil.text = "Ingenieria en Ciencias de la Computacion"
                tvCorreoPerfil.text = correo.ifBlank { "-" }
                tvCarnetPerfil.setText(carnet.ifBlank { "-" })
                tvPlanPerfil.text = textoPlan

                val iniciales = construirIniciales(nombres, apellidos)
                val avatarUrl = doc.getString("avatarUrl")
                    ?: doc.getString("avatar")
                    ?: auth.currentUser?.photoUrl?.toString()

                if (avatarUrl.isNullOrBlank()) {
                    mostrarIniciales(iniciales)
                } else {
                    cargarAvatarDesdeUrl(avatarUrl, iniciales)
                }
            }
            .addOnFailureListener {
                mostrarIniciales("?")
                Toast.makeText(this, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun activarModoEdicion() {
        modoEdicion = true
        btnEditar.text = "Guardar"

        tvNombrePerfil.setFocusable(true)
        tvNombrePerfil.isFocusableInTouchMode = true
        tvNombrePerfil.isCursorVisible = true
        tvNombrePerfil.requestFocus()

        tvApellidoPerfil.setFocusable(true)
        tvApellidoPerfil.isFocusableInTouchMode = true
        tvApellidoPerfil.isCursorVisible = true

        tvCarnetPerfil.setFocusable(true)
        tvCarnetPerfil.isFocusableInTouchMode = true
        tvCarnetPerfil.isCursorVisible = true
    }

    private fun desactivarModoEdicion() {
        modoEdicion = false
        btnEditar.text = "Editar perfil"

        tvNombrePerfil.setFocusable(false)
        tvNombrePerfil.isFocusableInTouchMode = false
        tvNombrePerfil.isCursorVisible = false

        tvApellidoPerfil.setFocusable(false)
        tvApellidoPerfil.isFocusableInTouchMode = false
        tvApellidoPerfil.isCursorVisible = false

        tvCarnetPerfil.setFocusable(false)
        tvCarnetPerfil.isFocusableInTouchMode = false
        tvCarnetPerfil.isCursorVisible = false
    }

    private fun guardarCambios() {
        val nombres = tvNombrePerfil.text.toString().trim()
        val apellidos = tvApellidoPerfil.text.toString().trim()
        val carnet = tvCarnetPerfil.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        if (nombres.isEmpty() || apellidos.isEmpty() || carnet.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection(UserAcademicProfile.USERS_COLLECTION)
            .document(userId)
            .update(
                "nombres", nombres,
                "apellidos", apellidos,
                "carnet", carnet
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                desactivarModoEdicion()
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configurarEventos() {
        btnSalir.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        btnEditar.setOnClickListener {
            if (!modoEdicion) activarModoEdicion()
            else guardarCambios()
        }
    }

    private fun construirIniciales(nombres: String, apellidos: String): String {
        val inicialNombre = nombres.firstOrNull { !it.isWhitespace() }?.uppercaseChar()
        val inicialApellido = apellidos.firstOrNull { !it.isWhitespace() }?.uppercaseChar()
        return buildString {
            if (inicialNombre != null) append(inicialNombre)
            if (inicialApellido != null) append(inicialApellido)
        }.ifBlank { "?" }
    }

    private fun mostrarIniciales(iniciales: String) {
        tvAvatarIniciales.text = iniciales
        tvAvatarIniciales.visibility = View.VISIBLE
        imgAvatar.visibility = View.GONE
    }

    private fun cargarAvatarDesdeUrl(url: String, iniciales: String) {
        Thread {
            try {
                val connection = URL(url).openConnection().apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                connection.getInputStream().use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    runOnUiThread {
                        if (bitmap != null) {
                            imgAvatar.setImageBitmap(bitmap)
                            imgAvatar.visibility = View.VISIBLE
                            tvAvatarIniciales.visibility = View.GONE
                        } else {
                            mostrarIniciales(iniciales)
                        }
                    }
                }
            } catch (_: Exception) {
                runOnUiThread { mostrarIniciales(iniciales) }
            }
        }.start()
    }
}
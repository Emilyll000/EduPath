package com.example.edupath_invest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirstLoginActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var tvSubtitle: TextView
    private lateinit var btnNuevoIngreso: Button
    private lateinit var btnAntiguoIngreso: Button
    private lateinit var layoutAntiguoIngreso: LinearLayout
    private lateinit var recyclerMaterias: RecyclerView
    private lateinit var btnGuardarHistorial: Button

    private val adapter = FirstLoginSubjectAdapter()
    private var userId: String? = null
    private var perfilUsuario: DocumentSnapshot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_login)

        userId = auth.currentUser?.uid
        if (userId == null) {
            volverAlLogin()
            return
        }

        tvSubtitle = findViewById(R.id.tvFirstLoginSubtitle)
        btnNuevoIngreso = findViewById(R.id.btnNuevoIngreso)
        btnAntiguoIngreso = findViewById(R.id.btnAntiguoIngreso)
        layoutAntiguoIngreso = findViewById(R.id.layoutAntiguoIngreso)
        recyclerMaterias = findViewById(R.id.recyclerFirstLoginMaterias)
        btnGuardarHistorial = findViewById(R.id.btnGuardarHistorial)

        recyclerMaterias.layoutManager = LinearLayoutManager(this)
        recyclerMaterias.adapter = adapter
        recyclerMaterias.setHasFixedSize(false)

        btnNuevoIngreso.setOnClickListener {
            registrarNuevoIngreso()
        }

        btnAntiguoIngreso.setOnClickListener {
            mostrarMateriasSegunPensum()
            layoutAntiguoIngreso.visibility = View.VISIBLE
        }

        btnGuardarHistorial.setOnClickListener {
            registrarAntiguoIngreso()
        }

        cargarEstadoPrimerIngreso()
    }

    private fun cargarEstadoPrimerIngreso() {
        val currentUserId = userId ?: return

        db.collection(UserAcademicProfile.USERS_COLLECTION)
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    auth.signOut()
                    Toast.makeText(this, "No se encontro tu perfil registrado", Toast.LENGTH_LONG).show()
                    volverAlLogin()
                    return@addOnSuccessListener
                }

                perfilUsuario = document

                if (document.getBoolean(UserAcademicProfile.FIELD_FIRST_LOGIN) == false) {
                    navegarAlDashboard()
                    return@addOnSuccessListener
                }

                val anioPensum = UserAcademicProfile.obtenerAnioPensum(document)
                adapter.submitPensum(UserAcademicProfile.obtenerMateriasPrimerIngreso(anioPensum))
                tvSubtitle.text = "Indica si eres de nuevo ingreso o si ya cursaste materias del pensum $anioPensum."
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se pudo cargar tu perfil: ${e.message}", Toast.LENGTH_LONG).show()
                volverAlLogin()
            }
    }

    private fun mostrarMateriasSegunPensum() {
        val documento = perfilUsuario ?: return
        val anioPensum = UserAcademicProfile.obtenerAnioPensum(documento)

        adapter.submitPensum(UserAcademicProfile.obtenerMateriasPrimerIngreso(anioPensum))
        tvSubtitle.text = "Selecciona las materias que ya cursaste del pensum $anioPensum e indica su nota final."
    }

    private fun registrarNuevoIngreso() {
        guardarPerfilPrimerIngreso(
            mapOf(
                UserAcademicProfile.FIELD_FIRST_LOGIN to false,
                UserAcademicProfile.FIELD_ENTRY_TYPE to "nuevo",
                UserAcademicProfile.FIELD_CURRENT_CYCLE to 1,
                UserAcademicProfile.FIELD_APPROVED_SUBJECTS to emptyList<String>(),
                UserAcademicProfile.FIELD_ENROLLED_SUBJECTS to emptyList<String>(),
                UserAcademicProfile.FIELD_ACADEMIC_HISTORY to emptyList<Map<String, Any>>()
            )
        )
    }

    private fun registrarAntiguoIngreso() {
        val materiasSeleccionadas = adapter.obtenerSeleccionadas()

        if (materiasSeleccionadas.isEmpty()) {
            Toast.makeText(this, "Marca al menos una materia cursada", Toast.LENGTH_SHORT).show()
            return
        }

        val documento = perfilUsuario ?: return
        val anioPensum = UserAcademicProfile.obtenerAnioPensum(documento)
        val pensum = UserAcademicProfile.obtenerMateriasPrimerIngreso(anioPensum)
        val historial = mutableListOf<Map<String, Any>>()
        val materiasAprobadas = mutableListOf<String>()

        materiasSeleccionadas.forEach { materia ->
            val promedio = materia.promedioFinal.toDoubleOrNull()

            if (promedio == null || promedio < 0 || promedio > 10) {
                Toast.makeText(
                    this,
                    "Ingresa un promedio valido entre 0 y 10 para ${materia.nombre}",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            val estado = if (promedio >= 6) "aprobada" else "reprobada"

            historial.add(
                mapOf(
                    "codigo" to materia.codigo,
                    "nombre" to materia.nombre,
                    "promedioFinal" to promedio,
                    "estado" to estado
                )
            )

            if (promedio >= 6) {
                materiasAprobadas.add(materia.codigo)
            }
        }

        val cicloActual = UserAcademicProfile.calcularCicloActual(
            pensum = pensum,
            codigosCursados = materiasSeleccionadas.map { it.codigo }.toSet()
        )

        guardarPerfilPrimerIngreso(
            mapOf(
                UserAcademicProfile.FIELD_FIRST_LOGIN to false,
                UserAcademicProfile.FIELD_ENTRY_TYPE to "antiguo",
                UserAcademicProfile.FIELD_CURRENT_CYCLE to cicloActual,
                UserAcademicProfile.FIELD_APPROVED_SUBJECTS to materiasAprobadas,
                UserAcademicProfile.FIELD_ENROLLED_SUBJECTS to emptyList<String>(),
                UserAcademicProfile.FIELD_ACADEMIC_HISTORY to historial
            )
        )
    }

    private fun guardarPerfilPrimerIngreso(data: Map<String, Any>) {
        val currentUserId = userId ?: return

        db.collection(UserAcademicProfile.USERS_COLLECTION)
            .document(currentUserId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                navegarAlDashboard()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se pudo guardar tu informacion: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun navegarAlDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    private fun volverAlLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
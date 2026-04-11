package com.example.edupath_invest

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PensumActivity : AppCompatActivity() {

    private lateinit var tvTituloPensum: TextView
    private lateinit var recyclerPensum: RecyclerView
    private lateinit var btnModificar: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pensum)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvTituloPensum = findViewById(R.id.tvTituloPensum)
        recyclerPensum = findViewById(R.id.recyclerPensum)
        btnModificar = findViewById(R.id.btnModificar)

        recyclerPensum.layoutManager = GridLayoutManager(this, 2)
        cargarPensumUsuario()

        btnModificar.setOnClickListener {
            // luego aquí abriremos la pantalla para cambiar estados
        }

        BottomNavHelper.setup(this, "pensum")
    }

    private fun cargarPensumUsuario() {
        val userId = auth.currentUser?.uid ?: return

        db.collection(UserAcademicProfile.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val anioPensum = UserAcademicProfile.obtenerAnioPensum(document)
                val materias = PensumRepository.obtenerPensum(anioPensum)
                val materiasAprobadas = UserAcademicProfile.obtenerMateriasAprobadas(document)

                materias.forEach { materia ->
                    if (materia.codigo in materiasAprobadas) {
                        materia.estado = EstadoMateria.APROBADA
                    }
                }

                GrafoHelper.actualizarEstados(materias)
                tvTituloPensum.text = "Pénsum $anioPensum"
                recyclerPensum.adapter = PensumAdapter(materias)
            }
    }
}
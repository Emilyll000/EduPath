package com.example.edupath_invest

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PensumActivity : AppCompatActivity() {

    private lateinit var tvTituloPensum: TextView
    private lateinit var recyclerPensum: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pensum)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvTituloPensum = findViewById(R.id.tvTituloPensum)
        recyclerPensum = findViewById(R.id.recyclerPensum)

        recyclerPensum.layoutManager = object : GridLayoutManager(this, 2) {
            override fun isAutoMeasureEnabled(): Boolean = true
        }

        cargarPensumUsuario()

        // El botón btnModificar fue eliminado del XML, así que quitamos su referencia aquí

        BottomNavHelper.setup(this, "pensum")
    }

    private fun cargarPensumUsuario() {
        val userId = auth.currentUser?.uid ?: return

        db.collection(UserAcademicProfile.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val anioPensum = UserAcademicProfile.obtenerAnioPensum(document)
                val materias = UserAcademicProfile.obtenerMateriasPensum(anioPensum)
                UserAcademicProfile.aplicarEstadosPensum(document, materias)

                val codigosRecomendados = GrafoHelper
                    .obtenerRecomendadas(materias, maximo = 5)
                    .map { it.codigo }
                    .toSet()

                materias.forEach { materia ->
                    if (materia.estado == EstadoMateria.HABILITADA && materia.codigo in codigosRecomendados) {
                        materia.estado = EstadoMateria.RECOMENDADA
                    }
                }

                val adapter = PensumAdapter(materias)
                tvTituloPensum.text = "Pénsum $anioPensum"
                recyclerPensum.adapter = adapter

                (recyclerPensum.layoutManager as? GridLayoutManager)?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (adapter.isHeaderPosition(position)) 2 else 1
                    }
                }
            }
    }
}
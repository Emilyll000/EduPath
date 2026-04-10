package com.example.edupath_invest

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PensumActivity : AppCompatActivity() {

    private lateinit var tvTituloPensum: TextView
    private lateinit var recyclerPensum: RecyclerView
    private lateinit var btnModificar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pensum)

        tvTituloPensum = findViewById(R.id.tvTituloPensum)
        recyclerPensum = findViewById(R.id.recyclerPensum)
        btnModificar = findViewById(R.id.btnModificar)

        val anioPensumSeleccionado = 2023
        val materias = PensumRepository.obtenerPensum(anioPensumSeleccionado)

        marcarEjemplo(materias)
        GrafoHelper.actualizarEstados(materias)

        tvTituloPensum.text = "Pénsum $anioPensumSeleccionado"

        recyclerPensum.layoutManager = GridLayoutManager(this, 2)
        recyclerPensum.adapter = PensumAdapter(materias)

        btnModificar.setOnClickListener {
            // luego aquí abriremos la pantalla para cambiar estados
        }

        BottomNavHelper.setup(this, "pensum")
    }

    private fun marcarEjemplo(materias: MutableList<MateriaPensum>) {
        materias.find { it.codigo == "MAT101" }?.estado = EstadoMateria.APROBADA
        materias.find { it.codigo == "PRG101" }?.estado = EstadoMateria.APROBADA
        materias.find { it.codigo == "MAT102" }?.estado = EstadoMateria.INSCRITA
    }
}
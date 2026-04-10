package com.example.edupath_invest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PensumAdapter(
    private val materias: List<MateriaPensum>
) : RecyclerView.Adapter<PensumAdapter.PensumViewHolder>() {

    class PensumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCodigo: TextView = itemView.findViewById(R.id.txtCodigo)
        val txtMateria: TextView = itemView.findViewById(R.id.txtMateria)
        val txtEstado: TextView = itemView.findViewById(R.id.txtEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PensumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_materia, parent, false)
        return PensumViewHolder(view)
    }

    override fun onBindViewHolder(holder: PensumViewHolder, position: Int) {
        val materia = materias[position]

        holder.txtCodigo.text = materia.codigo
        holder.txtMateria.text = materia.nombre
        holder.txtEstado.text = when (materia.estado) {
            EstadoMateria.APROBADA -> "Aprobada"
            EstadoMateria.INSCRITA -> "Inscrita"
            EstadoMateria.HABILITADA -> "Habilitada"
            EstadoMateria.PENDIENTE -> "Pendiente"
        }
    }

    override fun getItemCount(): Int {
        return materias.size
    }
}
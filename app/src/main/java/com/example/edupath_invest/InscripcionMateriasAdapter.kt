// InscripcionMateriasAdapter.kt
package com.example.edupath_invest

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InscripcionMateriasAdapter : RecyclerView.Adapter<InscripcionMateriasAdapter.InscripcionViewHolder>() {

    private val items = mutableListOf<InscripcionMateriaUi>()
    private val codigosSeleccionados = linkedSetOf<String>()

    class InscripcionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreMateriaInscribible)
        val tvCodigo: TextView = itemView.findViewById(R.id.tvCodigoMateriaInscribible)
        val tvSeleccion: TextView = itemView.findViewById(R.id.tvSeleccionMateria)
        val tvImportancia: TextView = itemView.findViewById(R.id.tvImportanciaMateria)
        val tvAbre: TextView = itemView.findViewById(R.id.tvAbreMateria)
        val tvMatricula: TextView = itemView.findViewById(R.id.tvMatriculaMateria)
        val tvPeso: TextView = itemView.findViewById(R.id.tvPesoMateria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InscripcionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inscripcion_materia, parent, false)
        return InscripcionViewHolder(view)
    }

    override fun onBindViewHolder(holder: InscripcionViewHolder, position: Int) {
        val item = items[position]
        holder.tvNombre.text = item.nombre
        holder.tvCodigo.text = item.codigo
        holder.tvAbre.text = "Abre ${item.materiasQueAbre} mat."

        if (item.matricula > 1) {
            holder.tvMatricula.visibility = View.VISIBLE
            holder.tvMatricula.text = "• ${item.matricula}a Matrícula"
        } else {
            holder.tvMatricula.visibility = View.GONE
        }

        val (texto, fondo) = when (item.importancia) {
            NivelImportancia.MUY_IMPORTANTE -> "PRIORIDAD" to R.drawable.bg_input_blue
            NivelImportancia.IMPORTANTE -> "IMPORTANTE" to R.drawable.bg_input_yellow
            else -> "OPCIONAL" to R.drawable.bg_button_round
        }

        holder.tvImportancia.text = texto
        holder.tvImportancia.setBackgroundResource(fondo)

        val seleccionada = item.codigo in codigosSeleccionados
        if (seleccionada) {
            holder.tvSeleccion.text = "QUITAR"
            holder.tvSeleccion.setTextColor(Color.RED)
        } else {
            holder.tvSeleccion.text = "AÑADIR"
            holder.tvSeleccion.setTextColor(Color.parseColor("#062451"))
        }

        holder.itemView.setOnClickListener {
            if (item.codigo in codigosSeleccionados) codigosSeleccionados.remove(item.codigo)
            else codigosSeleccionados.add(item.codigo)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(materias: List<InscripcionMateriaUi>) {
        items.clear()
        items.addAll(materias)
        notifyDataSetChanged()
    }

    fun obtenerCodigosSeleccionados(): List<String> = codigosSeleccionados.toList()
    fun obtenerMateriasSeleccionadas(): List<InscripcionMateriaUi> = items.filter { it.codigo in codigosSeleccionados }
    fun obtenerTodasLasMaterias(): List<InscripcionMateriaUi> = items.toList()
}
package com.example.edupath_invest

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
        val tvPeso: TextView = itemView.findViewById(R.id.tvPesoMateria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InscripcionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inscripcion_materia, parent, false)
        return InscripcionViewHolder(view)
    }

    override fun onBindViewHolder(holder: InscripcionViewHolder, position: Int) {
        val item = items[position]
        holder.tvNombre.text = item.nombre
        holder.tvCodigo.text = item.codigo

        val (textoImportancia, fondo) = when (item.importancia) {
            NivelImportancia.MUY_IMPORTANTE -> "Muy importante" to R.drawable.bg_input_blue
            NivelImportancia.IMPORTANTE -> "Importante" to R.drawable.bg_input_yellow
            NivelImportancia.PUEDE_ESPERAR -> "Puede esperar" to R.drawable.bg_button_round
        }

        holder.itemView.setBackgroundResource(fondo)
        holder.tvImportancia.text = textoImportancia

        val textoAbre = if (item.materiasQueAbre == 1) {
            "Abre 1 materia"
        } else {
            "Abre ${item.materiasQueAbre} materias"
        }

        holder.tvAbre.text = textoAbre
        holder.tvPeso.text = "Peso ${item.peso}"

        val estaSeleccionada = item.codigo in codigosSeleccionados
        holder.tvSeleccion.text = if (estaSeleccionada) "Seleccionada" else "Seleccionar"

        holder.itemView.alpha = if (estaSeleccionada) 1.0f else 0.92f
        holder.itemView.setOnClickListener {
            if (item.codigo in codigosSeleccionados) {
                codigosSeleccionados.remove(item.codigo)
            } else {
                codigosSeleccionados.add(item.codigo)
            }

            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(materias: List<InscripcionMateriaUi>) {
        items.clear()
        items.addAll(materias)
        codigosSeleccionados.clear()
        notifyDataSetChanged()
    }

    fun obtenerCodigosSeleccionados(): List<String> {
        return codigosSeleccionados.toList()
    }

    fun obtenerMateriasSeleccionadas(): List<InscripcionMateriaUi> {
        return items.filter { it.codigo in codigosSeleccionados }
    }

    fun obtenerTodasLasMaterias(): List<InscripcionMateriaUi> {
        return items.toList()
    }
}

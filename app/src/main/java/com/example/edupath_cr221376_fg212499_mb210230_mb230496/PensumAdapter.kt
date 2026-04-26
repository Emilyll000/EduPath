package com.example.edupath_cr221376_fg212499_mb210230_mb230496

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PensumAdapter(
    private val materias: List<MateriaPensum>
) : RecyclerView.Adapter<PensumAdapter.PensumViewHolder>() {

    private val items = buildListItems(materias)

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_SUBJECT = 1
    }

    sealed class PensumListItem {
        data class Header(
            val anioEtiqueta: String,
            val cicloEtiqueta: String
        ) : PensumListItem()

        data class Subject(val materia: MateriaPensum) : PensumListItem()
    }

    open class PensumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class HeaderViewHolder(itemView: View) : PensumViewHolder(itemView) {
        val txtAnio: TextView = itemView.findViewById(R.id.txtAnio)
        val txtCiclo: TextView = itemView.findViewById(R.id.txtCiclo)
    }

    class SubjectViewHolder(itemView: View) : PensumViewHolder(itemView) {
        val txtCorrelativo: TextView = itemView.findViewById(R.id.txtCorrelativo)
        val txtCodigo: TextView = itemView.findViewById(R.id.txtCodigo)
        val txtMateria: TextView = itemView.findViewById(R.id.txtMateria)
        val txtPrerequisito: TextView = itemView.findViewById(R.id.txtPrerequisito)
        val txtUv: TextView = itemView.findViewById(R.id.txtUv)
        val txtEstado: TextView = itemView.findViewById(R.id.txtEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PensumViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_HEADER) {
            R.layout.item_pensum_header
        } else {
            R.layout.item_materia
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(view)
        } else {
            SubjectViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: PensumViewHolder, position: Int) {
        when (val item = items[position]) {
            is PensumListItem.Header -> {
                val headerHolder = holder as HeaderViewHolder
                headerHolder.txtAnio.text = item.anioEtiqueta
                headerHolder.txtCiclo.text = item.cicloEtiqueta
            }

            is PensumListItem.Subject -> {
                val subjectHolder = holder as SubjectViewHolder
                val materia = item.materia

                subjectHolder.txtCorrelativo.text = materia.correlativo.toString()
                subjectHolder.txtCodigo.text = materia.codigo
                subjectHolder.txtMateria.text = materia.nombre
                subjectHolder.txtPrerequisito.text = materia.prerequisitoEtiqueta
                subjectHolder.txtUv.text = materia.unidadesValorativas.toString()
                val (textoEstado, fondoEstado) = when (materia.estado) {
                    EstadoMateria.APROBADA -> "Aprobada" to R.drawable.bg_input_blue
                    EstadoMateria.REPROBADA -> "Reprobada" to R.drawable.bg_button_round
                    EstadoMateria.INSCRITA -> "Inscrita" to R.drawable.bg_card_cycle
                    EstadoMateria.RECOMENDADA -> "Recomendada" to R.drawable.bg_input_recommendation
                    EstadoMateria.HABILITADA -> "Habilitada" to R.drawable.bg_input_yellow
                    EstadoMateria.PENDIENTE -> "Pendiente" to R.drawable.bg_button_round
                }

                subjectHolder.txtEstado.text = textoEstado
                subjectHolder.itemView.setBackgroundResource(fondoEstado)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is PensumListItem.Header -> VIEW_TYPE_HEADER
            is PensumListItem.Subject -> VIEW_TYPE_SUBJECT
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun isHeaderPosition(position: Int): Boolean {
        return items.getOrNull(position) is PensumListItem.Header
    }

    private fun buildListItems(materias: List<MateriaPensum>): List<PensumListItem> {
        val items = mutableListOf<PensumListItem>()
        var ultimoBloque: Pair<String, String>? = null

        materias.forEach { materia ->
            val anioEtiqueta = materia.anioEtiqueta.ifBlank { "AÑO ${materia.ciclo}" }
            val cicloEtiqueta = materia.cicloEtiqueta.ifBlank { "CICLO ${materia.ciclo}" }
            val bloqueActual = anioEtiqueta to cicloEtiqueta

            if (bloqueActual != ultimoBloque) {
                items.add(PensumListItem.Header(anioEtiqueta, cicloEtiqueta))
                ultimoBloque = bloqueActual
            }

            items.add(PensumListItem.Subject(materia))
        }

        return items
    }
}
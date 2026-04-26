package com.example.edupath_cr221376_fg212499_mb210230_mb230496

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class FirstLoginSubjectItem(
    val codigo: String,
    val nombre: String,
    val ciclo: Int,
    var seleccionada: Boolean = false,
    var promedioFinal: String = "",
    var numMatricula: Int = 1
)

class FirstLoginSubjectAdapter : RecyclerView.Adapter<FirstLoginSubjectAdapter.SubjectViewHolder>() {

    private val materias = mutableListOf<FirstLoginSubjectItem>()

    class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkMateria: CheckBox = itemView.findViewById(R.id.checkMateria)
        val tvCodigo: TextView = itemView.findViewById(R.id.tvCodigoMateria)
        val etPromedio: EditText = itemView.findViewById(R.id.etPromedioFinal)
        var textWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_first_login_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val materia = materias[position]

        holder.checkMateria.setOnCheckedChangeListener(null)
        holder.etPromedio.removeTextChangedListener(holder.textWatcher)

        holder.checkMateria.text = materia.nombre
        holder.tvCodigo.text = materia.codigo
        holder.checkMateria.isChecked = materia.seleccionada
        holder.etPromedio.setText(materia.promedioFinal)
        holder.etPromedio.isEnabled = materia.seleccionada
        holder.etPromedio.alpha = if (materia.seleccionada) 1f else 0.45f

        holder.checkMateria.setOnCheckedChangeListener { _, isChecked ->
            materia.seleccionada = isChecked

            if (!isChecked) {
                materia.promedioFinal = ""
                holder.etPromedio.setText("")
            }

            holder.etPromedio.isEnabled = isChecked
            holder.etPromedio.alpha = if (isChecked) 1f else 0.45f
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                materia.promedioFinal = s?.toString().orEmpty()
            }
        }

        holder.textWatcher = watcher
        holder.etPromedio.addTextChangedListener(watcher)
    }

    override fun getItemCount(): Int = materias.size

    fun submitPensum(materiasPensum: List<MateriaPensum>) {
        materias.clear()
        materias.addAll(
            materiasPensum.map {
                FirstLoginSubjectItem(codigo = it.codigo, nombre = it.nombre, ciclo = it.ciclo)
            }
        )
        notifyDataSetChanged()
    }

    fun obtenerSeleccionadas(): List<FirstLoginSubjectItem> {
        return materias.filter { it.seleccionada }
    }
}
package com.example.edupath_invest

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class CicloActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var btnIniciarInscripcion: Button
    private lateinit var btnAgregarMateria: Button
    private lateinit var btnEliminarMateria: Button
    private lateinit var layoutAccionesMaterias: LinearLayout
    private lateinit var llMateriasContainer: LinearLayout

    private val planesActividad = mutableListOf<MutableMap<String, Any?>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ciclo)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        btnIniciarInscripcion = findViewById(R.id.btnIniciarInscripcion)
        btnAgregarMateria = findViewById(R.id.btnAgregarMateria)
        btnEliminarMateria = findViewById(R.id.btnEliminarMateria)
        layoutAccionesMaterias = findViewById(R.id.layoutAccionesMaterias)
        llMateriasContainer = findViewById(R.id.llMateriasContainer)

        btnIniciarInscripcion.setOnClickListener {
            startActivity(Intent(this, InscripcionActivity::class.java))
        }

        btnAgregarMateria.setOnClickListener {
            startActivity(Intent(this, InscripcionActivity::class.java).apply {
                putExtra("EXTRA_MODE", "add")
            })
        }

        btnEliminarMateria.setOnClickListener {
            startActivity(Intent(this, InscripcionActivity::class.java).apply {
                putExtra("EXTRA_MODE", "remove")
            })
        }

        configurarVisibilidadBotonInscripcion()
        BottomNavHelper.setup(this, "cycle")
    }

    override fun onResume() {
        super.onResume()
        configurarVisibilidadBotonInscripcion()
    }

    private fun configurarVisibilidadBotonInscripcion() {
        val userId = auth.currentUser?.uid ?: return
        db.collection(UserAcademicProfile.USERS_COLLECTION).document(userId).get()
            .addOnSuccessListener { document ->
                if (!document.contains(UserAcademicProfile.FIELD_ENROLLED_SUBJECTS)) {
                    btnIniciarInscripcion.visibility = View.VISIBLE
                    layoutAccionesMaterias.visibility = View.GONE
                    llMateriasContainer.visibility = View.GONE
                    return@addOnSuccessListener
                }

                btnIniciarInscripcion.visibility = View.GONE
                layoutAccionesMaterias.visibility = View.VISIBLE
                llMateriasContainer.visibility = View.VISIBLE

                val materias = (document.get(UserAcademicProfile.FIELD_ENROLLED_SUBJECTS) as? List<*>)
                    ?.mapNotNull { it as? String } ?: emptyList()

                cargarPlanesActividades(document)
                construirTablasMaterias(materias)
            }
    }

    private fun construirTablasMaterias(materias: List<String>) {
        llMateriasContainer.removeAllViews()
        materias.forEachIndexed { index, codigo ->
            val esAzul = index % 2 == 0
            val viewMateria = LayoutInflater.from(this).inflate(R.layout.item_materia_tabla, llMateriasContainer, false)

            val plan = planesActividad.firstOrNull { it["codigo"] == codigo }
            val nombreMateria = plan?.get("nombre") as? String ?: codigo
            val actividades = (plan?.get("actividades") as? List<MutableMap<String, Any?>>) ?: emptyList()

            viewMateria.findViewById<TextView>(R.id.tvNombreMateriaTabla).text = nombreMateria
            val containerFilas = viewMateria.findViewById<LinearLayout>(R.id.llFilasActividades)
            var totalGanado = 0.0

            actividades.forEach { act ->
                val fila = LayoutInflater.from(this).inflate(R.layout.item_fila_actividad, containerFilas, false)
                val porcentaje = (act["porcentaje"] as? Number)?.toDouble() ?: 0.0
                val nota = (act["nota"] as? Number)?.toDouble()
                val ganado = nota?.let { (it * porcentaje) / 100.0 } ?: 0.0
                totalGanado += ganado

                fila.findViewById<TextView>(R.id.tvNombreAct).text = act["nombre"].toString()
                fila.findViewById<TextView>(R.id.tvNotaAct).text = nota?.let { String.format("%.1f", it) } ?: "-"
                fila.findViewById<TextView>(R.id.tvValorAct).text = "${porcentaje.toInt()}%"
                fila.findViewById<TextView>(R.id.tvGanadoAct).text = String.format("%.2f", ganado)

                configurarColoresFila(fila, esAzul)
                containerFilas.addView(fila)
            }

            viewMateria.findViewById<TextView>(R.id.tvTotalMateria).text = String.format("%.2f", totalGanado)

            val tvNecesita = viewMateria.findViewById<TextView>(R.id.tvNecesitasSacar)
            val (texto, visible) = calcularEstadoMateria(actividades)
            tvNecesita.text = texto
            tvNecesita.visibility = if (visible) View.VISIBLE else View.GONE

            viewMateria.findViewById<TextView>(R.id.tvModificarNotas).setOnClickListener {
                mostrarDialogoEditar(codigo, nombreMateria)
            }

            configurarColoresTabla(viewMateria, esAzul)
            llMateriasContainer.addView(viewMateria)
        }
    }

    private fun mostrarDialogoEditar(codigo: String, nombreMateria: String) {
        val plan = planesActividad.firstOrNull { it["codigo"] == codigo } ?: return
        val actividades = (plan["actividades"] as? MutableList<MutableMap<String, Any?>>) ?: return

        val dialogView = layoutInflater.inflate(R.layout.layout_dialog_editar_notas, null)
        val llCampos = dialogView.findViewById<LinearLayout>(R.id.llCamposNotasContainer)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        tvTitle.text = nombreMateria

        val inputs = mutableListOf<Pair<MutableMap<String, Any?>, EditText>>()
        val customFont = ResourcesCompat.getFont(this, R.font.finland_rounded_bold)

        actividades.forEach { act ->
            val label = TextView(this)
            label.text = act["nombre"].toString()
            label.setTextColor(Color.parseColor("#1A1A1A"))
            label.textSize = 17f // Tamaño más legible
            if (customFont != null) label.typeface = customFont
            label.setPadding(0, 25, 0, 8)

            // Input de la nota
            val et = EditText(this)
            et.setText(act["nota"]?.toString() ?: "")
            et.hint = "0.0"
            et.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            et.setBackgroundResource(R.drawable.bg_input_blue)

            et.setPadding(35, 20, 35, 20)
            et.textSize = 16f

            llCampos.addView(label)
            llCampos.addView(et)
            inputs.add(act to et)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        val roundedBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 45f
            setColor(Color.WHITE)
        }
        dialog.window?.setBackgroundDrawable(roundedBg)

        dialog.setOnShowListener {
            val btnGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnCancelar = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            btnGuardar.setTextColor(Color.parseColor("#062451"))
            btnGuardar.textSize = 20f
            if (customFont != null) btnGuardar.typeface = customFont

            btnCancelar.setTextColor(Color.GRAY)

            btnGuardar.setOnClickListener {
                var hayError = false
                inputs.forEach { (act, et) ->
                    val nota = et.text.toString().toDoubleOrNull()
                    if (nota != null && (nota < 0 || nota > 10)) {
                        et.error = "0-10"
                        hayError = true
                    } else {
                        act["nota"] = nota
                        act["estado"] = if (nota != null) "registrada" else "pendiente"
                    }
                }
                if (!hayError) {
                    guardarCambios()
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun calcularEstadoMateria(actividades: List<MutableMap<String, Any?>>): Pair<String, Boolean> {
        var puntos = 0.0
        var faltante = 0.0
        actividades.forEach {
            val p = (it["porcentaje"] as? Number)?.toDouble() ?: 0.0
            val n = (it["nota"] as? Number)?.toDouble()
            if (n == null) faltante += p else puntos += (n * p) / 100.0
        }
        if (faltante == 0.0) return (if (puntos >= 6.0) "APROBADA" else "REPROBADA") to true
        val nec = (6.0 - puntos) / (faltante / 100.0)
        return (if (nec <= 0) "¡Ya pasaste!" else "Necesitas: ${String.format("%.1f", nec)}") to true
    }

    private fun configurarColoresTabla(view: View, esAzul: Boolean) {
        val colorBorde = Color.parseColor(if (esAzul) "#4476A1" else "#DEAF47")
        val colorOscuro = Color.parseColor(if (esAzul) "#B8D1E9" else "#FFD985")
        val colorFondo = Color.parseColor(if (esAzul) "#EEF1F8" else "#FFF7E6")

        view.findViewById<MaterialCardView>(R.id.cardContenedor).strokeColor = colorBorde
        view.findViewById<LinearLayout>(R.id.llFondoTabla).setBackgroundColor(colorFondo)
        view.findViewById<TextView>(R.id.tvNombreMateriaTabla).setBackgroundColor(colorOscuro)
        view.findViewById<LinearLayout>(R.id.llHeaderColumnas).setBackgroundColor(colorOscuro)
        view.findViewById<LinearLayout>(R.id.llFooterTabla).setBackgroundColor(colorOscuro)

        val divisores = listOf(R.id.divH1, R.id.divH2, R.id.divH3, R.id.divV1, R.id.divV2, R.id.divV3, R.id.divV4, R.id.divV5)
        divisores.forEach { view.findViewById<View>(it).setBackgroundColor(colorBorde) }
    }

    private fun configurarColoresFila(view: View, esAzul: Boolean) {
        val colorBorde = Color.parseColor(if (esAzul) "#4476A1" else "#DEAF47")
        val divisores = listOf(R.id.v1, R.id.v2, R.id.v3, R.id.vH)
        divisores.forEach { view.findViewById<View>(it).setBackgroundColor(colorBorde) }
    }

    private fun cargarPlanesActividades(doc: DocumentSnapshot) {
        planesActividad.clear()
        val lista = (doc.get(UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN) as? List<Map<String, Any?>>) ?: emptyList()
        lista.forEach { planesActividad.add(it.toMutableMap()) }
    }

    private fun guardarCambios() {
        val userId = auth.currentUser?.uid ?: return
        db.collection(UserAcademicProfile.USERS_COLLECTION).document(userId)
            .set(mapOf(UserAcademicProfile.FIELD_ENROLLED_ACTIVITY_PLAN to planesActividad), SetOptions.merge())
            .addOnSuccessListener { configurarVisibilidadBotonInscripcion() }
    }
}
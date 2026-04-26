// InscripcionMateriaUi.kt
package com.example.edupath_cr221376_fg212499_mb210230_mb230496

enum class NivelImportancia {
    MUY_IMPORTANTE,
    IMPORTANTE,
    PUEDE_ESPERAR
}

data class InscripcionMateriaUi(
    val codigo: String,
    val nombre: String,
    val peso: Int,
    val materiasQueAbre: Int,
    val importancia: NivelImportancia,
    val matricula: Int
)
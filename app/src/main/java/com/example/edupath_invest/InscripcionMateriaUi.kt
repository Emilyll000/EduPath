package com.example.edupath_invest

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
    val importancia: NivelImportancia
)

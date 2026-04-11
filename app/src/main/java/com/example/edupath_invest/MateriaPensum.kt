package com.example.edupath_invest

data class MateriaPensum(
    val correlativo: Int = 0,
    val codigo: String,
    val nombre: String,
    val ciclo: Int,
    val anioPensum: Int,
    val anioEtiqueta: String = "",
    val cicloEtiqueta: String = "",
    val prerequisitoEtiqueta: String = "",
    val unidadesValorativas: Int = 0,
    val prerequisitos: List<String> = emptyList(),
    var estado: EstadoMateria = EstadoMateria.PENDIENTE
)
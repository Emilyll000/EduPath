package com.example.edupath_invest

data class MateriaPensum(
    val codigo: String,
    val nombre: String,
    val ciclo: Int,
    val anioPensum: Int,
    val prerequisitos: List<String> = emptyList(),
    var estado: EstadoMateria = EstadoMateria.PENDIENTE
)
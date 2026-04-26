// Archivo: MateriaPensum.kt
package com.example.edupath_cr221376_fg212499_mb210230_mb230496

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
    var estado: EstadoMateria = EstadoMateria.PENDIENTE,

    var numMatricula: Int = 1
) {
    val esCienciaBasica: Boolean
        get() = codigo.contains("501")

    val uvEfectivas: Int
        get() = if (numMatricula == 3) unidadesValorativas * 3 else unidadesValorativas
}
package com.example.edupath_invest

object PensumRepository {

    fun obtenerPensum(anio: Int): MutableList<MateriaPensum> {
        return when (anio) {
            2016 -> obtenerPensum2016().toMutableList()
            2023 -> obtenerPensum2023().toMutableList()
            else -> obtenerPensum2023().toMutableList()
        }
    }

    private fun obtenerPensum2016(): List<MateriaPensum> {
        return listOf(
            MateriaPensum("MAT101", "Matemática I", 1, 2016),
            MateriaPensum("PRG101", "Programación I", 1, 2016),
            MateriaPensum("LOG101", "Lógica de Sistemas", 1, 2016),
            MateriaPensum("MAT102", "Matemática II", 2, 2016, listOf("MAT101")),
            MateriaPensum("PRG102", "Programación II", 2, 2016, listOf("PRG101")),
            MateriaPensum("BDD201", "Base de Datos", 3, 2016, listOf("PRG102")),
            MateriaPensum("EDA201", "Estructuras de Datos", 3, 2016, listOf("PRG102")),
            MateriaPensum("SO301", "Sistemas Operativos", 4, 2016, listOf("EDA201")),
            MateriaPensum("RED301", "Redes", 4, 2016, listOf("EDA201"))
        )
    }

    private fun obtenerPensum2023(): List<MateriaPensum> {
        return listOf(
            MateriaPensum("MAT101", "Matemática I", 1, 2023),
            MateriaPensum("PRG101", "Programación I", 1, 2023),
            MateriaPensum("FDE101", "Fundamentos de Electrónica", 1, 2023),
            MateriaPensum("MAT102", "Matemática II", 2, 2023, listOf("MAT101")),
            MateriaPensum("PRG102", "Programación II", 2, 2023, listOf("PRG101")),
            MateriaPensum("POO201", "Programación Orientada a Objetos", 3, 2023, listOf("PRG102")),
            MateriaPensum("BDD201", "Base de Datos", 3, 2023, listOf("PRG102")),
            MateriaPensum("EDA201", "Estructuras de Datos", 3, 2023, listOf("PRG102")),
            MateriaPensum("SO301", "Sistemas Operativos", 4, 2023, listOf("EDA201")),
            MateriaPensum("RED301", "Redes de Computadoras", 4, 2023, listOf("EDA201"))
        )
    }
}
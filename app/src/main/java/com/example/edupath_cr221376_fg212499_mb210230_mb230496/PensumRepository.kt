package com.example.edupath_cr221376_fg212499_mb210230_mb230496

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
            MateriaPensum(codigo = "MAT101", nombre = "Matemática I", ciclo = 1, anioPensum = 2016),
            MateriaPensum(codigo = "PRG101", nombre = "Programación I", ciclo = 1, anioPensum = 2016),
            MateriaPensum(codigo = "LOG101", nombre = "Lógica de Sistemas", ciclo = 1, anioPensum = 2016),
            MateriaPensum(codigo = "MAT102", nombre = "Matemática II", ciclo = 2, anioPensum = 2016, prerequisitos = listOf("MAT101")),
            MateriaPensum(codigo = "PRG102", nombre = "Programación II", ciclo = 2, anioPensum = 2016, prerequisitos = listOf("PRG101")),
            MateriaPensum(codigo = "BDD201", nombre = "Base de Datos", ciclo = 3, anioPensum = 2016, prerequisitos = listOf("PRG102")),
            MateriaPensum(codigo = "EDA201", nombre = "Estructuras de Datos", ciclo = 3, anioPensum = 2016, prerequisitos = listOf("PRG102")),
            MateriaPensum(codigo = "SO301", nombre = "Sistemas Operativos", ciclo = 4, anioPensum = 2016, prerequisitos = listOf("EDA201")),
            MateriaPensum(codigo = "RED301", nombre = "Redes", ciclo = 4, anioPensum = 2016, prerequisitos = listOf("EDA201"))
        )
    }

    private fun obtenerPensum2023(): List<MateriaPensum> {
        return listOf(
            MateriaPensum(codigo = "MAT101", nombre = "Matemática I", ciclo = 1, anioPensum = 2023),
            MateriaPensum(codigo = "PRG101", nombre = "Programación I", ciclo = 1, anioPensum = 2023),
            MateriaPensum(codigo = "FDE101", nombre = "Fundamentos de Electrónica", ciclo = 1, anioPensum = 2023),
            MateriaPensum(codigo = "MAT102", nombre = "Matemática II", ciclo = 2, anioPensum = 2023, prerequisitos = listOf("MAT101")),
            MateriaPensum(codigo = "PRG102", nombre = "Programación II", ciclo = 2, anioPensum = 2023, prerequisitos = listOf("PRG101")),
            MateriaPensum(codigo = "POO201", nombre = "Programación Orientada a Objetos", ciclo = 3, anioPensum = 2023, prerequisitos = listOf("PRG102")),
            MateriaPensum(codigo = "BDD201", nombre = "Base de Datos", ciclo = 3, anioPensum = 2023, prerequisitos = listOf("PRG102")),
            MateriaPensum(codigo = "EDA201", nombre = "Estructuras de Datos", ciclo = 3, anioPensum = 2023, prerequisitos = listOf("PRG102")),
            MateriaPensum(codigo = "SO301", nombre = "Sistemas Operativos", ciclo = 4, anioPensum = 2023, prerequisitos = listOf("EDA201")),
            MateriaPensum(codigo = "RED301", nombre = "Redes de Computadoras", ciclo = 4, anioPensum = 2023, prerequisitos = listOf("EDA201"))
        )
    }
}
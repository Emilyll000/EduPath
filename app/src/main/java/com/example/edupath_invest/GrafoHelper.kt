package com.example.edupath_invest

object GrafoHelper {

    fun construirGrafo(materias: List<MateriaPensum>): Map<String, List<String>> {
        val grafo = mutableMapOf<String, MutableList<String>>()

        materias.forEach { materia ->
            grafo.putIfAbsent(materia.codigo, mutableListOf())

            materia.prerequisitos.forEach { prereq ->
                grafo.putIfAbsent(prereq, mutableListOf())
                grafo[prereq]?.add(materia.codigo)
            }
        }

        return grafo
    }

    fun actualizarEstados(materias: MutableList<MateriaPensum>) {
        val aprobadasOInscritas = materias
            .filter {
                it.estado == EstadoMateria.APROBADA || it.estado == EstadoMateria.INSCRITA
            }
            .map { it.codigo }
            .toSet()

        materias.forEach { materia ->
            if (materia.estado == EstadoMateria.PENDIENTE) {
                val habilitada = materia.prerequisitos.all { it in aprobadasOInscritas }
                if (habilitada) {
                    materia.estado = EstadoMateria.HABILITADA
                }
            }
        }
    }

    fun contarDesbloqueos(codigo: String, materias: List<MateriaPensum>): Int {
        return materias.count { codigo in it.prerequisitos }
    }

    fun obtenerRecomendadas(
        materias: List<MateriaPensum>,
        maximo: Int = 5
    ): List<MateriaPensum> {
        return materias
            .filter { it.estado == EstadoMateria.HABILITADA }
            .sortedByDescending { contarDesbloqueos(it.codigo, materias) }
            .take(maximo)
    }

    fun obtenerRutaCritica(materias: List<MateriaPensum>): List<MateriaPensum> {
        return materias
            .sortedByDescending { contarDesbloqueos(it.codigo, materias) }
            .take(5)
    }
}
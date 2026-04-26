package com.example.edupath_cr221376_fg212499_mb210230_mb230496

object GrafoHelper {

    data class MateriaPriorizada(
        val materia: MateriaPensum,
        val peso: Int,
        val desbloqueosDirectos: Int,
        val dependenciasTotales: Int,
        val longitudRutaCritica: Int
    )

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

    fun actualizarEstados(materias: MutableList<MateriaPensum>, cicloProximo: String) {
        val aprobadasOInscritas = materias
            .filter { it.estado == EstadoMateria.APROBADA || it.estado == EstadoMateria.INSCRITA }
            .map { it.codigo }.toSet()

        materias.forEach { materia ->
            if (materia.estado == EstadoMateria.PENDIENTE) {
                val requisitosCumplidos = materia.prerequisitos.all { it in aprobadasOInscritas }

                val esCicloParMateria = (materia.ciclo % 2 == 0)
                val correspondeCiclo = if (cicloProximo == "par") esCicloParMateria else !esCicloParMateria

                val esCienciaBasica = materia.codigo.contains("501")

                if (requisitosCumplidos && (correspondeCiclo || esCienciaBasica)) {
                    materia.estado = EstadoMateria.HABILITADA
                }
            }
        }
    }

    fun contarDesbloqueos(codigo: String, materias: List<MateriaPensum>): Int {
        return materias.count { codigo in it.prerequisitos }
    }

    fun obtenerMateriasPriorizadas(
        materias: List<MateriaPensum>,
        maximo: Int = 5
    ): List<MateriaPriorizada> {
        val grafo = construirGrafo(materias)
        val memoDependencias = mutableMapOf<String, Int>()
        val memoRuta = mutableMapOf<String, Int>()

        return materias
            .filter { it.estado == EstadoMateria.HABILITADA || it.estado == EstadoMateria.REPROBADA }
            .map { materia ->
                val desbloqueosDirectos = contarDesbloqueos(materia.codigo, materias)
                val dependenciasTotales = contarDependenciasTotales(materia.codigo, grafo, memoDependencias)
                val longitudRutaCritica = calcularLongitudRutaCritica(materia.codigo, grafo, memoRuta)

                val multiplicador = materia.numMatricula.coerceAtMost(4)
                val uvPenalizadas = materia.unidadesValorativas * multiplicador

                val penalizacionRiesgo = when (materia.numMatricula) {
                    2 -> 200
                    3 -> 1000
                    4 -> 10000
                    else -> 0
                }

                val peso = (dependenciasTotales * 100) +
                        (longitudRutaCritica * 10) +
                        desbloqueosDirectos +
                        uvPenalizadas +
                        penalizacionRiesgo

                MateriaPriorizada(
                    materia = materia,
                    peso = peso,
                    desbloqueosDirectos = desbloqueosDirectos,
                    dependenciasTotales = dependenciasTotales,
                    longitudRutaCritica = longitudRutaCritica
                )
            }
            .sortedWith(
                compareByDescending<MateriaPriorizada> { it.peso }
                    .thenByDescending { it.materia.numMatricula }
                    .thenByDescending { it.longitudRutaCritica }
            )
            .take(maximo)
    }

    fun obtenerRecomendadas(
        materias: List<MateriaPensum>,
        maximo: Int = 5
    ): List<MateriaPensum> {
        return obtenerMateriasPriorizadas(materias, maximo).map { it.materia }
    }

    fun obtenerRutaCritica(materias: List<MateriaPensum>): List<MateriaPensum> {
        val grafo = construirGrafo(materias)
        val memoRuta = mutableMapOf<String, Int>()
        val memoDependencias = mutableMapOf<String, Int>()

        return materias
            .sortedWith(
                compareByDescending<MateriaPensum> {
                    calcularLongitudRutaCritica(it.codigo, grafo, memoRuta)
                }.thenByDescending {
                    contarDependenciasTotales(it.codigo, grafo, memoDependencias)
                }.thenByDescending {
                    contarDesbloqueos(it.codigo, materias)
                }
            )
            .take(5)
    }

    private fun contarDependenciasTotales(
        codigo: String,
        grafo: Map<String, List<String>>,
        memo: MutableMap<String, Int>
    ): Int {
        memo[codigo]?.let { return it }

        val visitados = mutableSetOf<String>()

        fun dfs(actual: String) {
            grafo[actual].orEmpty().forEach { dependiente ->
                if (visitados.add(dependiente)) {
                    dfs(dependiente)
                }
            }
        }

        dfs(codigo)
        return visitados.size.also { memo[codigo] = it }
    }

    private fun calcularLongitudRutaCritica(
        codigo: String,
        grafo: Map<String, List<String>>,
        memo: MutableMap<String, Int>
    ): Int {
        memo[codigo]?.let { return it }

        val dependientes = grafo[codigo].orEmpty()
        val longitud = if (dependientes.isEmpty()) {
            0
        } else {
            1 + dependientes.maxOf { dependiente ->
                calcularLongitudRutaCritica(dependiente, grafo, memo)
            }
        }

        memo[codigo] = longitud
        return longitud
    }
}
package com.example.edupath_invest

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

    fun obtenerMateriasPriorizadas(
        materias: List<MateriaPensum>,
        maximo: Int = 5
    ): List<MateriaPriorizada> {
        val grafo = construirGrafo(materias)
        val memoDependencias = mutableMapOf<String, Int>()
        val memoRuta = mutableMapOf<String, Int>()

        return materias
            .filter { it.estado == EstadoMateria.HABILITADA }
            .map { materia ->
                val desbloqueosDirectos = contarDesbloqueos(materia.codigo, materias)
                val dependenciasTotales = contarDependenciasTotales(
                    codigo = materia.codigo,
                    grafo = grafo,
                    memo = memoDependencias
                )
                val longitudRutaCritica = calcularLongitudRutaCritica(
                    codigo = materia.codigo,
                    grafo = grafo,
                    memo = memoRuta
                )
                val peso =
                    (dependenciasTotales * 100) +
                        (longitudRutaCritica * 10) +
                        desbloqueosDirectos +
                        materia.unidadesValorativas

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
                    .thenByDescending { it.longitudRutaCritica }
                    .thenByDescending { it.dependenciasTotales }
                    .thenByDescending { it.desbloqueosDirectos }
                    .thenBy { it.materia.ciclo }
                    .thenBy { it.materia.nombre }
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
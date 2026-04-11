package com.example.edupath_invest

import com.google.firebase.firestore.DocumentSnapshot

object UserAcademicProfile {
    const val USERS_COLLECTION = "usuarios"
    const val FIELD_FIRST_LOGIN = "firstLogin"
    const val FIELD_ENTRY_TYPE = "tipoIngreso"
    const val FIELD_APPROVED_SUBJECTS = "materiasAprobadas"
    const val FIELD_ACADEMIC_HISTORY = "historialAcademico"
    const val FIELD_CURRENT_CYCLE = "cicloActual"
    const val FIELD_PLAN_ID = "planId"
    const val FIELD_PLAN_YEAR = "anioPensum"

    fun obtenerAnioPensum(plan: String?): Int {
        return extraerAnioPlan(plan)
    }

    fun obtenerAnioPensum(planId: Int?): Int {
        if (planId == null) {
            return 2023
        }

        val nombrePlan = PensumSeeds.planes
            .firstOrNull { it.id == planId }
            ?.nombre

        return extraerAnioPlan(nombrePlan)
    }

    fun obtenerAnioPensum(document: DocumentSnapshot): Int {
        val anioGuardado = document.getLong(FIELD_PLAN_YEAR)?.toInt()
        if (anioGuardado != null) {
            return anioGuardado
        }

        val planIdGuardado = document.getLong(FIELD_PLAN_ID)?.toInt()
        if (planIdGuardado != null) {
            return obtenerAnioPensum(planIdGuardado)
        }

        return obtenerAnioPensum(document.getString("plan"))
    }

    fun obtenerPlanId(anioPensum: Int): Int {
        return PensumSeeds.planes
            .firstOrNull { extraerAnioPlan(it.nombre) == anioPensum }
            ?.id
            ?: 3
    }

    fun obtenerMateriasPensum(anioPensum: Int): MutableList<MateriaPensum> {
        val planId = obtenerPlanId(anioPensum)
        val materiasDelPlan = PensumSeeds.materias.filter { it.planId == planId }
        val materiasPorCorrelativo = materiasDelPlan.associateBy { it.correlativo }

        return materiasDelPlan
            .map { materia ->
                MateriaPensum(
                    correlativo = materia.correlativo,
                    codigo = materia.codigo,
                    nombre = materia.nombre,
                    ciclo = extraerNumeroCiclo(materia.ciclo),
                    anioPensum = anioPensum,
                    anioEtiqueta = materia.anio,
                    cicloEtiqueta = materia.ciclo,
                    prerequisitoEtiqueta = materia.prerequisito,
                    unidadesValorativas = materia.uv,
                    prerequisitos = extraerPrerequisitos(materia.prerequisito, materiasPorCorrelativo)
                )
            }
            .toMutableList()
    }

    fun obtenerMateriasPrimerIngreso(anioPensum: Int): List<MateriaPensum> {
        return obtenerMateriasPensum(anioPensum)
    }

    fun aplicarEstadosPensum(document: DocumentSnapshot, materias: MutableList<MateriaPensum>) {
        val materiasAprobadas = obtenerMateriasAprobadas(document)
        val cicloActual = document.getLong(FIELD_CURRENT_CYCLE)?.toInt() ?: 1

        materias.forEach { materia ->
            val requisitosCompletos = materia.prerequisitos.all { it in materiasAprobadas }

            materia.estado = when {
                materia.codigo in materiasAprobadas -> EstadoMateria.APROBADA
                materia.ciclo == cicloActual && requisitosCompletos -> EstadoMateria.INSCRITA
                requisitosCompletos -> EstadoMateria.HABILITADA
                else -> EstadoMateria.PENDIENTE
            }
        }
    }

    private fun extraerNumeroCiclo(ciclo: String): Int {
        val digitos = ciclo.filter { it.isDigit() }.toIntOrNull()
        if (digitos != null) {
            return digitos
        }

        val romano = ciclo
            .substringAfterLast(' ')
            .uppercase()

        return when (romano) {
            "I" -> 1
            "II" -> 2
            "III" -> 3
            "IV" -> 4
            "V" -> 5
            "VI" -> 6
            "VII" -> 7
            "VIII" -> 8
            "IX" -> 9
            "X" -> 10
            else -> 1
        }
    }

    private fun extraerPrerequisitos(
        prerequisito: String,
        materiasPorCorrelativo: Map<Int, MateriaSeed>
    ): List<String> {
        return prerequisito
            .split(',')
            .mapNotNull { valor ->
                valor
                    .trim()
                    .toIntOrNull()
                    ?.let { correlativo -> materiasPorCorrelativo[correlativo]?.codigo }
            }
            .distinct()
    }

    private fun extraerAnioPlan(plan: String?): Int {
        return plan
            ?.filter { it.isDigit() }
            ?.takeLast(4)
            ?.toIntOrNull()
            ?: 2023
    }

    fun obtenerMateriasAprobadas(document: DocumentSnapshot): Set<String> {
        val materiasGuardadas = (document.get(FIELD_APPROVED_SUBJECTS) as? List<*>)
            ?.mapNotNull { it as? String }
            ?.toSet()
            ?: emptySet()

        val materiasHistorial = (document.get(FIELD_ACADEMIC_HISTORY) as? List<*>)
            ?.mapNotNull { registro ->
                val materia = registro as? Map<*, *> ?: return@mapNotNull null
                val codigo = materia["codigo"] as? String ?: return@mapNotNull null
                val estado = (materia["estado"] as? String)?.lowercase()
                val promedio = (materia["promedioFinal"] as? Number)?.toDouble()

                if (estado == "aprobada" || (promedio != null && promedio >= 6.0)) {
                    codigo
                } else {
                    null
                }
            }
            ?.toSet()
            ?: emptySet()

        return materiasGuardadas + materiasHistorial
    }

    fun calcularCicloActual(
        pensum: List<MateriaPensum>,
        codigosCursados: Set<String>
    ): Int {
        if (codigosCursados.isEmpty()) {
            return 1
        }

        val cicloMaximoPensum = pensum.maxOfOrNull { it.ciclo } ?: 1
        val cicloMaximoCursado = pensum
            .filter { it.codigo in codigosCursados }
            .maxOfOrNull { it.ciclo }
            ?: 1

        return (cicloMaximoCursado + 1).coerceAtMost(cicloMaximoPensum)
    }
}
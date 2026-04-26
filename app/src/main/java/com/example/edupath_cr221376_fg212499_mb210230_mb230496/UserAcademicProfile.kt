package com.example.edupath_cr221376_fg212499_mb210230_mb230496

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot

object UserAcademicProfile {
    const val USERS_COLLECTION = "usuarios"
    const val FIELD_FIRST_LOGIN = "firstLogin"
    const val FIELD_ENTRY_TYPE = "tipoIngreso"
    const val FIELD_APPROVED_SUBJECTS = "materiasAprobadas"
    const val FIELD_ENROLLED_SUBJECTS = "materiasInscritas"
    const val FIELD_ENROLLED_ACTIVITY_PLAN = "planActividadesInscripcion"
    const val FIELD_ACADEMIC_HISTORY = "historialAcademico"
    const val FIELD_CURRENT_CYCLE = "cicloActual"
    const val FIELD_PLAN_ID = "planId"
    const val FIELD_PLAN_YEAR = "anioPensum"

    // --- MANEJO DE AÑO Y PLAN ---

    fun obtenerAnioPensum(plan: String?): Int = extraerAnioPlan(plan)

    fun obtenerAnioPensum(planId: Int?): Int {
        if (planId == null) return 2023
        val nombrePlan = PensumSeeds.planes.firstOrNull { it.id == planId }?.nombre
        return extraerAnioPlan(nombrePlan)
    }

    fun obtenerAnioPensum(document: DocumentSnapshot): Int {
        val anioGuardado = document.getLong(FIELD_PLAN_YEAR)?.toInt()
        if (anioGuardado != null) return anioGuardado

        val planIdGuardado = document.getLong(FIELD_PLAN_ID)?.toInt()
        if (planIdGuardado != null) return obtenerAnioPensum(planIdGuardado)

        return obtenerAnioPensum(document.getString("plan"))
    }

    fun obtenerPlanId(anioPensum: Int): Int {
        return PensumSeeds.planes
            .firstOrNull { extraerAnioPlan(it.nombre) == anioPensum }
            ?.id ?: 3
    }

    // --- CARGA DE MATERIAS (CON PESO POR MATRÍCULA) ---

    fun obtenerMateriasPensum(context: Context, anioPensum: Int): MutableList<MateriaPensum> {
        val planId = obtenerPlanId(anioPensum)
        val materiasDelPlan = PensumSeeds.materias.filter { it.planId == planId }
        val materiasPorCorrelativo = materiasDelPlan.associateBy { it.correlativo }

        return materiasDelPlan.map { materia ->
            val intentosLocales = LocalAcademicManager.obtenerIntentos(context, materia.codigo)
            val matricula = (intentosLocales + 1).coerceAtMost(4)

            MateriaPensum(
                correlativo = materia.correlativo,
                codigo = materia.codigo,
                nombre = materia.nombre,
                ciclo = extraerNumeroCiclo(materia.ciclo),
                anioPensum = anioPensum,
                anioEtiqueta = materia.anio,
                cicloEtiqueta = materia.ciclo,
                prerequisitoEtiqueta = materia.prerequisito,
                // CORRECCIÓN: La UV de la materia ahora refleja el peso de la matrícula
                unidadesValorativas = materia.uv * matricula,
                prerequisitos = extraerPrerequisitos(materia.prerequisito, materiasPorCorrelativo),
                numMatricula = matricula
            )
        }.toMutableList()
    }

    fun obtenerMateriasPrimerIngreso(context: Context, anioPensum: Int): List<MateriaPensum> {
        return obtenerMateriasPensum(context, anioPensum)
    }

    fun aplicarEstadosPensum(document: DocumentSnapshot, materias: MutableList<MateriaPensum>) {
        val aprobadas = obtenerMateriasAprobadas(document)
        val inscritas = obtenerMateriasInscritas(document)

        materias.forEach { materia ->
            val requisitosCompletos = materia.prerequisitos.all { it in aprobadas }
            materia.estado = when {
                materia.codigo in aprobadas -> EstadoMateria.APROBADA
                materia.codigo in inscritas -> EstadoMateria.INSCRITA
                requisitosCompletos -> EstadoMateria.HABILITADA
                else -> EstadoMateria.PENDIENTE
            }
        }
    }

    // --- LÓGICA ACADÉMICA Y CUM (PONDERADO) ---

    fun calcularCicloActual(pensum: List<MateriaPensum>, codigosCursados: Set<String>): Int {
        if (codigosCursados.isEmpty()) return 1
        val cicloMaximoPensum = pensum.maxOfOrNull { it.ciclo } ?: 1
        val cicloMaximoCursado = pensum
            .filter { it.codigo in codigosCursados }
            .maxOfOrNull { it.ciclo } ?: 1
        return (cicloMaximoCursado + 1).coerceAtMost(cicloMaximoPensum)
    }

    fun calcularCUM(document: DocumentSnapshot): Double {
        val historial = document.get(FIELD_ACADEMIC_HISTORY) as? List<Map<String, Any>> ?: return 0.0
        var puntosAcumulados = 0.0
        var totalUVCursadas = 0

        historial.forEach { registro ->
            val notaRaw = registro["promedioFinal"]
            val nota = when (notaRaw) {
                is Number -> notaRaw.toDouble()
                is String -> notaRaw.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }

            val codigo = registro["codigo"] as? String ?: ""
            // Buscamos si el registro del historial ya tiene guardado en qué matrícula se cursó
            val nMatricula = (registro["numMatricula"] ?: registro["matricula"])?.let {
                when(it) {
                    is Number -> it.toInt()
                    is String -> it.toIntOrNull()
                    else -> 1
                }
            } ?: 1

            val uvBase = obtenerUVBase(codigo, document)
            val uvPonderada = uvBase * nMatricula

            if (uvPonderada > 0) {
                puntosAcumulados += (nota * uvPonderada)
                totalUVCursadas += uvPonderada
            }
        }
        return if (totalUVCursadas > 0) puntosAcumulados / totalUVCursadas else 0.0
    }

    private fun obtenerUVBase(codigo: String, document: DocumentSnapshot): Int {
        val anioPensum = obtenerAnioPensum(document)
        val planId = obtenerPlanId(anioPensum)
        return PensumSeeds.materias
            .filter { it.planId == planId }
            .find { it.codigo.trim().equals(codigo.trim(), ignoreCase = true) }
            ?.uv ?: 0
    }

    fun obtenerLimiteUV(cum: Double): Int {
        return when {
            cum >= 7.5 -> 32
            cum >= 7.0 -> 24
            cum >= 6.0 -> 20
            else -> 16
        }
    }

    fun determinarProximoCiclo(cicloActual: Int): String {
        return if (cicloActual % 2 == 0) "impar" else "par"
    }

    // --- GESTIÓN DE MATERIAS POR ESTADO ---

    fun obtenerMateriasAprobadas(document: DocumentSnapshot): Set<String> {
        val directas = (document.get(FIELD_APPROVED_SUBJECTS) as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
        val delHistorial = (document.get(FIELD_ACADEMIC_HISTORY) as? List<*>)?.mapNotNull {
            val m = it as? Map<*, *> ?: return@mapNotNull null
            val cod = m["codigo"] as? String ?: return@mapNotNull null
            val nota = when(val n = m["promedioFinal"]) {
                is Number -> n.toDouble()
                is String -> n.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
            val estado = (m["estado"] as? String)?.lowercase()
            if (estado == "aprobada" || nota >= 6.0) cod else null
        }?.toSet() ?: emptySet()
        return directas + delHistorial
    }

    fun obtenerMateriasInscritas(document: DocumentSnapshot): Set<String> {
        val guardadas = (document.get(FIELD_ENROLLED_SUBJECTS) as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
        val delHistorial = (document.get(FIELD_ACADEMIC_HISTORY) as? List<*>)?.mapNotNull {
            val m = it as? Map<*, *> ?: return@mapNotNull null
            if ((m["estado"] as? String)?.lowercase() == "inscrita") m["codigo"] as? String else null
        }?.toSet() ?: emptySet()
        return (guardadas + delHistorial) - obtenerMateriasAprobadas(document) - obtenerMateriasReprobadas(document)
    }

    fun obtenerMateriasReprobadas(document: DocumentSnapshot): Set<String> {
        return (document.get(FIELD_ACADEMIC_HISTORY) as? List<*>)?.mapNotNull {
            val m = it as? Map<*, *> ?: return@mapNotNull null
            val cod = m["codigo"] as? String ?: return@mapNotNull null
            val nota = (m["promedioFinal"] as? Number)?.toDouble() ?: 0.0
            val estado = (m["estado"] as? String)?.lowercase()
            if (estado == "reprobada" || nota < 6.0) cod else null
        }?.toSet() ?: emptySet()
    }

    // --- PARSERS INTERNOS ---

    private fun extraerNumeroCiclo(ciclo: String): Int {
        val digitos = ciclo.filter { it.isDigit() }.toIntOrNull()
        if (digitos != null) return digitos
        val romano = ciclo.substringAfterLast(' ').uppercase()
        return when (romano) {
            "I" -> 1 "II" -> 2 "III" -> 3 "IV" -> 4 "V" -> 5
            "VI" -> 6 "VII" -> 7 "VIII" -> 8 "IX" -> 9 "X" -> 10
            else -> 1
        }
    }

    private fun extraerPrerequisitos(prereq: String, map: Map<Int, MateriaSeed>): List<String> {
        return prereq.split(',').mapNotNull { it.trim().toIntOrNull()?.let { c -> map[c]?.codigo } }.distinct()
    }

    private fun extraerAnioPlan(plan: String?): Int {
        return plan?.filter { it.isDigit() }?.takeLast(4)?.toIntOrNull() ?: 2023
    }
}
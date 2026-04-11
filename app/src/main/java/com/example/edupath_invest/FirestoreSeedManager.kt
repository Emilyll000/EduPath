package com.example.edupath_invest

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object FirestoreSeedManager {

    private const val PLANES_COLLECTION = "planes"
    private const val MATERIAS_COLLECTION = "materias"
    private const val SEED_METADATA_COLLECTION = "seed_metadata"
    private const val PENSUM_METADATA_ID = "pensum"

    fun ensurePensumSeed(db: FirebaseFirestore, onComplete: (Result<Unit>) -> Unit) {
        db.collection(SEED_METADATA_COLLECTION).document(PENSUM_METADATA_ID).get()
            .addOnSuccessListener { metadataDoc ->
                val expectedPlanCount = PensumSeeds.planes.size.toLong()
                val expectedMateriaCount = PensumSeeds.materias.size.toLong()

                val storedPlanCount = metadataDoc.getLong("planCount")
                val storedMateriaCount = metadataDoc.getLong("materiaCount")

                if (metadataDoc.exists() &&
                    storedPlanCount == expectedPlanCount &&
                    storedMateriaCount == expectedMateriaCount
                ) {
                    onComplete(Result.success(Unit))
                    return@addOnSuccessListener
                }

                registrarSeed(db, onComplete)
            }
            .addOnFailureListener { error ->
                onComplete(Result.failure(error))
            }
    }

    private fun registrarSeed(db: FirebaseFirestore, onComplete: (Result<Unit>) -> Unit) {
        val batch = db.batch()

        PensumSeeds.planes.forEach { plan ->
            val planRef = db.collection(PLANES_COLLECTION).document("plan_${plan.id}")
            batch.set(
                planRef,
                mapOf(
                    "id" to plan.id,
                    "nombre" to plan.nombre
                ),
                SetOptions.merge()
            )
        }

        PensumSeeds.materias.forEach { materia ->
            val materiaRef = db.collection(MATERIAS_COLLECTION).document("materia_${materia.id}")
            batch.set(
                materiaRef,
                mapOf(
                    "id" to materia.id,
                    "codigo" to materia.codigo,
                    "nombre" to materia.nombre,
                    "prerequisito" to materia.prerequisito,
                    "uv" to materia.uv,
                    "anio" to materia.anio,
                    "ciclo" to materia.ciclo,
                    "planId" to materia.planId
                ),
                SetOptions.merge()
            )
        }

        val metadataRef = db.collection(SEED_METADATA_COLLECTION).document(PENSUM_METADATA_ID)
        batch.set(
            metadataRef,
            mapOf(
                "planCount" to PensumSeeds.planes.size,
                "materiaCount" to PensumSeeds.materias.size
            ),
            SetOptions.merge()
        )

        batch.commit()
            .addOnSuccessListener {
                onComplete(Result.success(Unit))
            }
            .addOnFailureListener { error ->
                onComplete(Result.failure(error))
            }
    }
}
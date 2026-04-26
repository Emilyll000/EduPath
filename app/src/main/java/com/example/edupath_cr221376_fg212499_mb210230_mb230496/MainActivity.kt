package com.example.edupath_cr221376_fg212499_mb210230_mb230496

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirestoreSeedManager.ensurePensumSeed(db) { result ->
            result.exceptionOrNull()?.let { error ->
                Log.e("MainActivity", "No se pudo verificar o registrar la seed en Firestore", error)
            }

            navegarSegunSesion()
        }
    }

    private fun navegarSegunSesion() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        db.collection(UserAcademicProfile.USERS_COLLECTION)
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val destino = when {
                    !document.exists() -> {
                        auth.signOut()
                        LoginActivity::class.java
                    }

                    document.getBoolean(UserAcademicProfile.FIELD_FIRST_LOGIN) == false -> DashboardActivity::class.java
                    else -> FirstLoginActivity::class.java
                }

                startActivity(Intent(this, destino))
                finish()
            }
            .addOnFailureListener {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
    }
}
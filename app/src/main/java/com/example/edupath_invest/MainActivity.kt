package com.example.edupath_invest

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

        if (currentUser != null) {
            startActivity(Intent(this, FirstLoginActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
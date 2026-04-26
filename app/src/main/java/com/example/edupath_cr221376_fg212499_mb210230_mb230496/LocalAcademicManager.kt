package com.example.edupath_cr221376_fg212499_mb210230_mb230496

import android.content.Context
import android.content.SharedPreferences

object LocalAcademicManager {
    private const val PREFS_NAME = "academic_local_prefs"
    private const val KEY_INTENTOS = "intentos_fallidos_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun guardarIntentoFallido(context: Context, codigoMateria: String) {
        val prefs = getPrefs(context)
        val actual = obtenerIntentos(context, codigoMateria)
        prefs.edit().putInt(KEY_INTENTOS + codigoMateria, actual + 1).apply()
    }

    fun obtenerIntentos(context: Context, codigoMateria: String): Int {
        return getPrefs(context).getInt(KEY_INTENTOS + codigoMateria, 0)
    }

    fun borrarIntento(context: Context, codigoMateria: String) {
        getPrefs(context).edit().remove(KEY_INTENTOS + codigoMateria).apply()
    }
}
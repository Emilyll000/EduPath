package com.example.edupath_invest

import com.google.firebase.auth.FirebaseAuthException

fun Throwable.toFirebaseAuthMessage(): String {
    val authException = this as? FirebaseAuthException
    val errorCode = authException?.errorCode

    return when {
        errorCode == "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato valido."
        errorCode == "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con ese correo."
        errorCode == "ERROR_WRONG_PASSWORD" -> "La contrasena es incorrecta."
        errorCode == "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese correo ya esta registrado."
        errorCode == "ERROR_WEAK_PASSWORD" -> "La contrasena debe tener al menos 6 caracteres."
        errorCode == "ERROR_NETWORK_REQUEST_FAILED" -> "No se pudo conectar con Firebase. Verifica tu conexion a Internet."
        message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true ->
            "Firebase Auth no encuentra la configuracion del proyecto. Verifica en Firebase Console que Authentication este habilitado y que la app Android com.example.edupath_invest exista en el proyecto edutech-c23bd."
        !errorCode.isNullOrBlank() -> "Firebase Auth devolvio $errorCode. ${message.orEmpty()}"
        !message.isNullOrBlank() -> message.orEmpty()
        else -> "Ocurrio un error de autenticacion no identificado."
    }
}
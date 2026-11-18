package com.example.practica15

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ServicioFirebase : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("FCM", "Token nuevo: $token")
    }

    override fun onMessageReceived(mensaje: RemoteMessage) {
        val titulo = mensaje.notification?.title ?: mensaje.data["titulo"] ?: "Mensaje nuevo"
        val texto = mensaje.notification?.body ?: mensaje.data["texto"] ?: ""
        val destino = mensaje.data["destino"] ?: "mensaje"
        val dato = mensaje.data["dato"] ?: ""

        crearCanales(this)
        mostrarNotificacion(this, titulo, texto, CANAL_ALTA, destino, dato)
    }
}
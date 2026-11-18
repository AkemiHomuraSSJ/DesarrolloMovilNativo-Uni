package com.example.practica15

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

const val CANAL_ALTA = "canal_alta"
const val CANAL_BAJA = "canal_baja"

fun crearCanales(contexto: Context) {
    val manager = contexto.getSystemService(NotificationManager::class.java)
    val canalAlta = NotificationChannel(CANAL_ALTA, "Notificaciones importantes", NotificationManager.IMPORTANCE_HIGH)
    val canalBaja = NotificationChannel(CANAL_BAJA, "Notificaciones normales", NotificationManager.IMPORTANCE_LOW)
    manager.createNotificationChannel(canalAlta)
    manager.createNotificationChannel(canalBaja)
}

fun mostrarNotificacion(contexto: Context, titulo: String, texto: String, canal: String, destino: String, dato: String) {
    if (Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val intent = Intent(contexto, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    intent.putExtra("destino", destino)
    intent.putExtra("dato", dato)

    val id = System.currentTimeMillis().toInt()
    val pendingIntent = PendingIntent.getActivity(
        contexto,
        id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notificacion = NotificationCompat.Builder(contexto, canal)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(titulo)
        .setContentText(texto)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(contexto).notify(id, notificacion)
}

class MainActivity : ComponentActivity() {

    private var destino by mutableStateOf("inicio")
    private var dato by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crearCanales(this)
        leerIntent(intent)
        setContent {
            MaterialTheme {
                Pantallas(destino, dato) { destino = "inicio" }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        leerIntent(intent)
    }

    private fun leerIntent(intent: Intent) {
        val uri = intent.data
        if (uri != null) {
            destino = uri.host ?: "inicio"
            dato = uri.getQueryParameter("dato") ?: ""
        } else {
            destino = intent.getStringExtra("destino") ?: "inicio"
            dato = intent.getStringExtra("dato") ?: ""
        }
    }
}

@Composable
fun Pantallas(destino: String, dato: String, volver: () -> Unit) {
    BackHandler(enabled = destino != "inicio") { volver() }

    when (destino) {
        "detalle" -> PantallaDetalle(dato, volver)
        "mensaje" -> PantallaMensaje(dato, volver)
        else -> PantallaInicio()
    }
}

@Composable
fun PantallaInicio() {
    val contexto = LocalContext.current
    var token by remember { mutableStateOf("Obteniendo token...") }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { tarea ->
            if (tarea.isSuccessful) {
                token = tarea.result
                Log.d("FCM", "Token: ${tarea.result}")
            } else {
                token = "No se pudo obtener el token"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Practica 15", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                mostrarNotificacion(
                    contexto,
                    "Notificación importante",
                    "Toca para ir al detalle",
                    CANAL_ALTA,
                    "detalle",
                    "Llegaste desde una notificación local importante"
                )
            }
        ) {
            Text("Notificación importante")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                mostrarNotificacion(
                    contexto,
                    "Notificación normal",
                    "Toca para ir al detalle",
                    CANAL_BAJA,
                    "detalle",
                    "Llegaste desde una notificación local normal"
                )
            }
        ) {
            Text("Notificación normal")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Token FCM:")
        SelectionContainer {
            Text(token)
        }
    }
}

@Composable
fun PantallaDetalle(dato: String, volver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pantalla de detalle", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(dato)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = volver) {
            Text("Volver")
        }
    }
}

@Composable
fun PantallaMensaje(dato: String, volver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pantalla de mensaje push", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Dato recibido: $dato")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = volver) {
            Text("Volver")
        }
    }
}
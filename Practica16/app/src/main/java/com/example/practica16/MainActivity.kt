package com.example.practica16

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class DescargaWorker(contexto: android.content.Context, parametros: WorkerParameters) :
    CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        return try {
            delay(15000)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaPrincipal()
                }
            }
        }
    }
}

@Composable
fun PantallaPrincipal() {
    val contexto = LocalContext.current
    val administrador = WorkManager.getInstance(contexto)
    var idTrabajo by remember { mutableStateOf<UUID?>(null) }

    val flujo = remember(idTrabajo) {
        val id = idTrabajo
        if (id == null) flowOf<WorkInfo?>(null) else administrador.getWorkInfoByIdFlow(id)
    }
    val informacion by flujo.collectAsState(initial = null)

    val estado = when (informacion?.state) {
        WorkInfo.State.ENQUEUED -> "ENQUEUED (en cola, esperando restricciones)"
        WorkInfo.State.RUNNING -> "RUNNING (ejecutando)"
        WorkInfo.State.SUCCEEDED -> "SUCCEEDED (terminada con exito)"
        WorkInfo.State.FAILED -> "FAILED (fallo)"
        WorkInfo.State.BLOCKED -> "BLOCKED (bloqueada)"
        WorkInfo.State.CANCELLED -> "CANCELLED (cancelada)"
        null -> "Sin tarea"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Practica 16 - WorkManager")
        Text(text = "Restricciones: bateria no baja y conexion a red")
        Text(text = "Estado: $estado")

        Button(onClick = {
            val restricciones = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val solicitud = OneTimeWorkRequestBuilder<DescargaWorker>()
                .setConstraints(restricciones)
                .build()

            administrador.enqueue(solicitud)
            idTrabajo = solicitud.id
        }) {
            Text(text = "Iniciar tarea")
        }
    }
}
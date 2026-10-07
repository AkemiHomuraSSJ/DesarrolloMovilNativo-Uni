package com.example.practica08

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TareaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = BaseDeDatos.obtener(application).tareaDao()

    val tareas = dao.obtenerTodas().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun agregar(titulo: String) {
        viewModelScope.launch {
            dao.insertar(Tarea(titulo = titulo))
        }
    }

    fun cambiarEstado(tarea: Tarea) {
        viewModelScope.launch {
            dao.actualizar(tarea.copy(hecha = !tarea.hecha))
        }
    }

    fun eliminar(tarea: Tarea) {
        viewModelScope.launch {
            dao.eliminar(tarea)
        }
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel: TareaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaTareas(viewModel)
                }
            }
        }
    }
}

@Composable
fun PantallaTareas(viewModel: TareaViewModel) {
    val tareas by viewModel.tareas.collectAsState()
    var texto by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(text = "Lista de tareas", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Nueva tarea") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (texto.isNotBlank()) {
                    viewModel.agregar(texto.trim())
                    texto = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (tareas.isEmpty()) {
            Text("No hay tareas guardadas")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(tareas, key = { it.id }) { tarea ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tarea.hecha,
                        onCheckedChange = { viewModel.cambiarEstado(tarea) }
                    )
                    Text(
                        text = tarea.titulo,
                        textDecoration = if (tarea.hecha) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = { viewModel.eliminar(tarea) }) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}
package com.example.administradortareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "tareas")
data class Tarea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val completada: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas")
    fun obtenerTodas(): Flow<List<Tarea>>

    @Insert
    suspend fun insertar(tarea: Tarea)

    @Update
    suspend fun actualizar(tarea: Tarea)

    @Delete
    suspend fun eliminar(tarea: Tarea)

    @Query("DELETE FROM tareas WHERE completada = 1")
    suspend fun borrarCompletadas()
}

@Database(entities = [Tarea::class], version = 1, exportSchema = false)
abstract class BaseDatos : RoomDatabase() {
    abstract fun tareaDao(): TareaDao
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val baseDatos = Room.databaseBuilder(this, BaseDatos::class.java, "tareas.db").build()
        val dao = baseDatos.tareaDao()
        setContent {
            MaterialTheme {
                PantallaTareas(dao)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(dao: TareaDao) {
    val alcance = rememberCoroutineScope()
    val flujo = remember { dao.obtenerTodas() }
    val tareas by flujo.collectAsState(initial = emptyList())

    var menuAbierto by remember { mutableStateOf(false) }
    var filtro by remember { mutableStateOf("Todas") }
    var orden by remember { mutableStateOf("Fecha") }

    var dialogoAbierto by remember { mutableStateOf(false) }
    var tareaEditando by remember { mutableStateOf<Tarea?>(null) }
    var textoTarea by remember { mutableStateOf("") }

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val tareasFiltradas = tareas
        .filter {
            when (filtro) {
                "Pendientes" -> !it.completada
                "Completadas" -> it.completada
                else -> true
            }
        }
        .let { lista ->
            if (orden == "Fecha") {
                lista.sortedByDescending { it.fecha }
            } else {
                lista.sortedWith(compareBy<Tarea> { it.completada }.thenByDescending { it.fecha })
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                actions = {
                    TextButton(onClick = {
                        alcance.launch { dao.borrarCompletadas() }
                    }) {
                        Text("Borrar completadas")
                    }
                    TextButton(onClick = { menuAbierto = true }) {
                        Text("Menu")
                    }
                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ver todas") },
                            onClick = {
                                filtro = "Todas"
                                menuAbierto = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ver pendientes") },
                            onClick = {
                                filtro = "Pendientes"
                                menuAbierto = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ver completadas") },
                            onClick = {
                                filtro = "Completadas"
                                menuAbierto = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ordenar por fecha") },
                            onClick = {
                                orden = "Fecha"
                                menuAbierto = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ordenar por estado") },
                            onClick = {
                                orden = "Estado"
                                menuAbierto = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                tareaEditando = null
                textoTarea = ""
                dialogoAbierto = true
            }) {
                Text("+")
            }
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(16.dp)
        ) {
            Text("Filtro: $filtro - Orden: $orden")

            if (tareasFiltradas.isEmpty()) {
                Text(
                    text = "No hay tareas para mostrar",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(tareasFiltradas, key = { it.id }) { tarea ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = tarea.completada,
                                onCheckedChange = {
                                    alcance.launch {
                                        dao.actualizar(tarea.copy(completada = it))
                                    }
                                }
                            )
                            Column {
                                Text(
                                    text = tarea.titulo,
                                    textDecoration = if (tarea.completada) TextDecoration.LineThrough else null
                                )
                                Text(formatoFecha.format(Date(tarea.fecha)))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = {
                                tareaEditando = tarea
                                textoTarea = tarea.titulo
                                dialogoAbierto = true
                            }) {
                                Text("Editar")
                            }
                            TextButton(onClick = {
                                alcance.launch { dao.eliminar(tarea) }
                            }) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialogoAbierto) {
        AlertDialog(
            onDismissRequest = { dialogoAbierto = false },
            title = { Text(if (tareaEditando == null) "Nueva tarea" else "Editar tarea") },
            text = {
                OutlinedTextField(
                    value = textoTarea,
                    onValueChange = { textoTarea = it },
                    label = { Text("Titulo") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val titulo = textoTarea.trim()
                    val editando = tareaEditando
                    if (titulo.isNotEmpty()) {
                        alcance.launch {
                            if (editando == null) {
                                dao.insertar(Tarea(titulo = titulo))
                            } else {
                                dao.actualizar(editando.copy(titulo = titulo))
                            }
                        }
                        dialogoAbierto = false
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogoAbierto = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
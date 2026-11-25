package com.example.practica17

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EstadoUi {
    object Cargando : EstadoUi
    data class Exito(val datos: List<String>) : EstadoUi
    data class Error(val mensaje: String) : EstadoUi
}

class PantallaViewModel : ViewModel() {

    private val _estado = MutableStateFlow<EstadoUi>(EstadoUi.Cargando)
    val estado: StateFlow<EstadoUi> = _estado.asStateFlow()

    private var intentos = 0

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _estado.value = EstadoUi.Cargando
            delay(2000)
            intentos++
            if (intentos % 2 == 1) {
                _estado.value = EstadoUi.Error("No se pudo cargar la informacion")
            } else {
                _estado.value = EstadoUi.Exito(
                    listOf(
                        "Manzana",
                        "Pera",
                        "Naranja",
                        "Platano",
                        "Uva",
                        "Sandia",
                        "Melon"
                    )
                )
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pantallaViewModel = ViewModelProvider(this)[PantallaViewModel::class.java]
        setContent {
            MaterialTheme {
                Pantalla(pantallaViewModel)
            }
        }
    }
}

@Composable
fun Pantalla(pantallaViewModel: PantallaViewModel) {
    val estado = pantallaViewModel.estado.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (estado) {
            is EstadoUi.Cargando -> {
                CircularProgressIndicator()
                Text(text = "Cargando...", modifier = Modifier.padding(top = 16.dp))
            }

            is EstadoUi.Exito -> {
                Text(text = "Lista de frutas")
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(estado.datos) { dato ->
                        Text(text = dato)
                    }
                }
                Button(onClick = { pantallaViewModel.cargarDatos() }) {
                    Text(text = "Volver a cargar")
                }
            }

            is EstadoUi.Error -> {
                Text(text = estado.mensaje)
                Button(
                    onClick = { pantallaViewModel.cargarDatos() },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Reintentar")
                }
            }
        }
    }
}
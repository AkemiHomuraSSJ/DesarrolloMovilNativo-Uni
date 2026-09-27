package com.example.registroestudiantes

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavegacionApp()
                }
            }
        }
    }
}

@Composable
fun NavegacionApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "formulario") {
        composable("formulario") {
            PantallaFormulario(navController)
        }
        composable(
            route = "confirmacion/{matricula}/{nombre}/{carrera}/{turno}/{estatus}",
            arguments = listOf(
                navArgument("matricula") { type = NavType.StringType },
                navArgument("nombre") { type = NavType.StringType },
                navArgument("carrera") { type = NavType.StringType },
                navArgument("turno") { type = NavType.StringType },
                navArgument("estatus") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            val carrera = backStackEntry.arguments?.getString("carrera") ?: ""
            val turno = backStackEntry.arguments?.getString("turno") ?: ""
            val estatus = backStackEntry.arguments?.getString("estatus") ?: ""
            PantallaConfirmacion(navController, matricula, nombre, carrera, turno, estatus)
        }
    }
}

@Composable
fun PantallaFormulario(navController: NavController) {
    val contexto = LocalContext.current
    val preferencias = contexto.getSharedPreferences("datos_app", Context.MODE_PRIVATE)

    var matricula by remember { mutableStateOf(preferencias.getString("ultima_matricula", "") ?: "") }
    var nombre by remember { mutableStateOf("") }
    var carreraSeleccionada by remember { mutableStateOf("Ingenieria en Sistemas") }
    var menuExpandido by remember { mutableStateOf(false) }
    var turnoSeleccionado by remember { mutableStateOf("Matutino") }
    var estatusActivo by remember { mutableStateOf(true) }

    val listaCarreras = listOf(
        "Ingenieria en Sistemas",
        "Contaduria",
        "Administracion",
        "Derecho",
        "Psicologia"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = "Registro de Estudiantes", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = matricula,
            onValueChange = { matricula = it },
            label = { Text("Matricula") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = carreraSeleccionada,
                onValueChange = {},
                readOnly = true,
                label = { Text("Carrera") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { menuExpandido = true }
            )
            DropdownMenu(
                expanded = menuExpandido,
                onDismissRequest = { menuExpandido = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                listaCarreras.forEach { carrera ->
                    DropdownMenuItem(
                        text = { Text(carrera) },
                        onClick = {
                            carreraSeleccionada = carrera
                            menuExpandido = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Turno")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = turnoSeleccionado == "Matutino",
                onClick = { turnoSeleccionado = "Matutino" }
            )
            Text(text = "Matutino")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = turnoSeleccionado == "Vespertino",
                onClick = { turnoSeleccionado = "Vespertino" }
            )
            Text(text = "Vespertino")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Estatus: " + if (estatusActivo) "Activo" else "Inactivo")
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = estatusActivo,
                onCheckedChange = { estatusActivo = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                preferencias.edit().putString("ultima_matricula", matricula).apply()
                val estatusTexto = if (estatusActivo) "Activo" else "Inactivo"
                navController.navigate(
                    "confirmacion/$matricula/$nombre/$carreraSeleccionada/$turnoSeleccionado/$estatusTexto"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Registrar")
        }
    }
}

@Composable
fun PantallaConfirmacion(
    navController: NavController,
    matricula: String,
    nombre: String,
    carrera: String,
    turno: String,
    estatus: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Confirmacion de Registro", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Matricula: $matricula")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Nombre: $nombre")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Carrera: $carrera")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Turno: $turno")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Estatus: $estatus")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Volver")
        }
    }
}
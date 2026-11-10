package com.example.practica14

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

val fuenteMapa = object : OnlineTileSourceBase(
    "EsriCalles",
    0,
    19,
    256,
    "",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PantallaMapa()
            }
        }
    }
}

fun tienePermiso(contexto: Context): Boolean {
    val fina = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION)
    val aproximada = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fina == PackageManager.PERMISSION_GRANTED || aproximada == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
fun obtenerUbicacion(
    cliente: FusedLocationProviderClient,
    alObtener: (GeoPoint) -> Unit,
    alFallar: () -> Unit
) {
    cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { ubicacion ->
            if (ubicacion != null) {
                alObtener(GeoPoint(ubicacion.latitude, ubicacion.longitude))
            } else {
                alFallar()
            }
        }
        .addOnFailureListener {
            alFallar()
        }
}

@Composable
fun PantallaMapa() {
    val contexto = LocalContext.current
    val actividad = contexto as ComponentActivity
    val cliente = remember { LocationServices.getFusedLocationProviderClient(contexto) }
    val permisos = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    var permisoConcedido by remember { mutableStateOf(tienePermiso(contexto)) }
    var permisoDenegado by remember { mutableStateOf(false) }
    var ubicacion by remember { mutableStateOf<GeoPoint?>(null) }
    var mensajeError by remember { mutableStateOf("") }

    val mapa = remember {
        Configuration.getInstance().load(
            contexto,
            contexto.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = "Practica14GPSMapasEstudianteLuisG/1.0"
        MapView(contexto).apply {
            setTileSource(fuenteMapa)
            setMultiTouchControls(true)
            controller.setZoom(16.0)
        }
    }

    DisposableEffect(Unit) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                mapa.onResume()
            } else if (evento == Lifecycle.Event.ON_PAUSE) {
                mapa.onPause()
            }
        }
        actividad.lifecycle.addObserver(observador)
        onDispose {
            actividad.lifecycle.removeObserver(observador)
            mapa.onDetach()
        }
    }

    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        val concedido = resultados[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                resultados[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        permisoConcedido = concedido
        permisoDenegado = !concedido
    }

    LaunchedEffect(Unit) {
        if (!permisoConcedido) {
            lanzador.launch(permisos)
        }
    }

    LaunchedEffect(permisoConcedido) {
        if (permisoConcedido) {
            obtenerUbicacion(
                cliente,
                alObtener = {
                    ubicacion = it
                    mensajeError = ""
                },
                alFallar = {
                    mensajeError = "No se pudo obtener la ubicación. Revisa que el GPS esté activo."
                }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!permisoConcedido) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                if (permisoDenegado) {
                    Text("Permiso denegado. La aplicación necesita la ubicación para mostrar el mapa.")
                } else {
                    Text("Se necesita el permiso de ubicación.")
                }
                Button(
                    onClick = { lanzador.launch(permisos) },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Pedir permiso")
                }
                Button(
                    onClick = {
                        val intento = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", contexto.packageName, null)
                        )
                        contexto.startActivity(intento)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Abrir ajustes")
                }
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                val posicion = ubicacion
                if (posicion != null) {
                    Text("Latitud: ${posicion.latitude}")
                    Text("Longitud: ${posicion.longitude}")
                } else if (mensajeError.isNotEmpty()) {
                    Text(mensajeError)
                } else {
                    Text("Obteniendo ubicación...")
                }
                Button(
                    onClick = {
                        obtenerUbicacion(
                            cliente,
                            alObtener = {
                                ubicacion = it
                                mensajeError = ""
                            },
                            alFallar = {
                                mensajeError = "No se pudo obtener la ubicación. Revisa que el GPS esté activo."
                            }
                        )
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Actualizar ubicación")
                }
            }
            AndroidView(
                factory = { mapa },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                update = { vista ->
                    val posicion = ubicacion
                    if (posicion != null) {
                        vista.overlays.clear()
                        val marcador = Marker(vista)
                        marcador.position = posicion
                        marcador.title = "Mi ubicación"
                        marcador.snippet = "${posicion.latitude}, ${posicion.longitude}"
                        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        vista.overlays.add(marcador)
                        vista.controller.animateTo(posicion)
                        vista.invalidate()
                    }
                }
            )
        }
    }
}
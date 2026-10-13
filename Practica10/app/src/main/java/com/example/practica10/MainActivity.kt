package com.example.practica10

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PantallaGaleria()
            }
        }
    }
}

@Composable
fun PantallaGaleria() {
    val contexto = LocalContext.current

    val imagenes = remember {
        mutableStateListOf<Any>().apply {
            for (i in 10..39) {
                add("https://picsum.photos/id/$i/400/400")
            }
        }
    }
    var imagenSeleccionada by remember { mutableStateOf<Any?>(null) }
    var mensaje by remember { mutableStateOf("") }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto: Bitmap? ->
        if (foto != null) {
            imagenes.add(0, foto)
            mensaje = "Foto agregada a la galería"
        } else {
            mensaje = "No se tomó ninguna foto"
        }
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) {
            lanzadorCamara.launch(null)
        } else {
            mensaje = "Permiso de cámara denegado"
        }
    }

    fun tomarFoto() {
        val permiso = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            lanzadorCamara.launch(null)
        } else {
            lanzadorPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    val seleccionada = imagenSeleccionada

    if (seleccionada != null) {
        BackHandler { imagenSeleccionada = null }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = seleccionada,
                contentDescription = "Imagen ampliada",
                placeholder = ColorPainter(Color.LightGray),
                error = ColorPainter(Color.Red),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Button(
                onClick = { imagenSeleccionada = null },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver a la galería")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Button(
                onClick = { tomarFoto() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tomar foto")
            }

            if (mensaje.isNotEmpty()) {
                Text(
                    text = mensaje,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(110.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                items(imagenes) { imagen ->
                    AsyncImage(
                        model = imagen,
                        contentDescription = "Imagen de la galería",
                        placeholder = ColorPainter(Color.LightGray),
                        error = ColorPainter(Color.Red),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { imagenSeleccionada = imagen }
                    )
                }
            }
        }
    }
}
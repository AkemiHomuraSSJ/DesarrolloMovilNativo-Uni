package com.example.redsociallocal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Aplicacion()
                }
            }
        }
    }
}

fun formatearFecha(milisegundos: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX"))
    return formato.format(Date(milisegundos))
}

fun crearArchivoFoto(contexto: Context): File {
    val carpeta = File(contexto.filesDir, "fotos")
    carpeta.mkdirs()
    return File(carpeta, "foto_" + System.currentTimeMillis() + ".jpg")
}

fun uriDeArchivo(contexto: Context, archivo: File): Uri {
    return FileProvider.getUriForFile(contexto, contexto.packageName + ".fileprovider", archivo)
}

fun cargarBitmap(contexto: Context, uri: String): Bitmap? {
    val direccion = Uri.parse(uri)
    val opciones = BitmapFactory.Options()
    opciones.inSampleSize = 8
    val bitmap = contexto.contentResolver.openInputStream(direccion)?.use { entrada ->
        BitmapFactory.decodeStream(entrada, null, opciones)
    }
    if (bitmap == null) {
        return null
    }
    val orientacion = contexto.contentResolver.openInputStream(direccion)?.use { entrada ->
        ExifInterface(entrada).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    }
    val grados = when (orientacion) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    if (grados == 0f) {
        return bitmap
    }
    val matriz = Matrix()
    matriz.postRotate(grados)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
}

@Composable
fun FotoPublicacion(uri: String, modifier: Modifier) {
    val contexto = LocalContext.current
    val bitmap = remember(uri) { cargarBitmap(contexto, uri) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun Aplicacion() {
    val viewModel: PublicacionViewModel = viewModel()
    val publicaciones by viewModel.publicaciones.collectAsState()
    var mostrandoFormulario by rememberSaveable { mutableStateOf(false) }

    if (mostrandoFormulario) {
        BackHandler { mostrandoFormulario = false }
        PantallaFormulario(
            onGuardar = { titulo, descripcion, fotoUri, fecha ->
                viewModel.guardar(titulo, descripcion, fotoUri, fecha)
                mostrandoFormulario = false
            },
            onCancelar = { mostrandoFormulario = false }
        )
    } else {
        PantallaLista(
            publicaciones = publicaciones,
            onNueva = { mostrandoFormulario = true }
        )
    }
}

@Composable
fun PantallaLista(publicaciones: List<Publicacion>, onNueva: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Red Social Local", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = onNueva, modifier = Modifier.fillMaxWidth()) {
            Text("Nueva publicación")
        }

        if (publicaciones.isEmpty()) {
            Text("Aún no hay publicaciones")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(publicaciones, key = { it.id }) { publicacion ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (publicacion.fotoUri.isNotEmpty()) {
                                FotoPublicacion(
                                    uri = publicacion.fotoUri,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }
                            Text(publicacion.titulo, style = MaterialTheme.typography.titleMedium)
                            Text(publicacion.descripcion)
                            Text(formatearFecha(publicacion.fecha))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaFormulario(
    onGuardar: (String, String, String, Long) -> Unit,
    onCancelar: () -> Unit
) {
    val contexto = LocalContext.current

    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var fotoUri by rememberSaveable { mutableStateOf("") }
    var uriCamara by rememberSaveable { mutableStateOf("") }
    var mensaje by rememberSaveable { mutableStateOf("") }
    val fecha = rememberSaveable { System.currentTimeMillis() }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            fotoUri = uriCamara
        }
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val archivo = crearArchivoFoto(contexto)
            contexto.contentResolver.openInputStream(uri)?.use { entrada ->
                archivo.outputStream().use { salida ->
                    entrada.copyTo(salida)
                }
            }
            fotoUri = uriDeArchivo(contexto, archivo).toString()
        }
    }

    fun abrirCamara() {
        val archivo = crearArchivoFoto(contexto)
        val uri = uriDeArchivo(contexto, archivo)
        uriCamara = uri.toString()
        lanzadorCamara.launch(uri)
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            mensaje = ""
            abrirCamara()
        } else {
            mensaje = "Se necesita el permiso de la cámara para tomar fotos"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nueva publicación", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Fecha: " + formatearFecha(fecha))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val permiso = contexto.checkSelfPermission(Manifest.permission.CAMERA)
                    if (permiso == PackageManager.PERMISSION_GRANTED) {
                        abrirCamara()
                    } else {
                        lanzadorPermiso.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Tomar foto")
            }

            Button(
                onClick = {
                    lanzadorGaleria.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Galería")
            }
        }

        if (mensaje.isNotEmpty()) {
            Text(mensaje, color = MaterialTheme.colorScheme.error)
        }

        if (fotoUri.isNotEmpty()) {
            FotoPublicacion(
                uri = fotoUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancelar,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = { onGuardar(titulo.trim(), descripcion.trim(), fotoUri, fecha) },
                enabled = titulo.isNotBlank() && descripcion.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }
        }
    }
}
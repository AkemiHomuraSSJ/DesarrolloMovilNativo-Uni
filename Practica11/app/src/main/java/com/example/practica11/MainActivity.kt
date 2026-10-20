package com.example.practica11

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val codigoPermiso = 100
    private val nombreArchivo = "archivo.txt"

    private lateinit var campoTexto: EditText
    private lateinit var radioInterno: RadioButton
    private lateinit var radioSD: RadioButton
    private lateinit var textoResultado: TextView
    private var accionPendiente = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        campoTexto = findViewById(R.id.campoTexto)
        radioInterno = findViewById(R.id.radioInterno)
        radioSD = findViewById(R.id.radioSD)
        textoResultado = findViewById(R.id.textoResultado)
        val botonGuardar = findViewById<Button>(R.id.botonGuardar)
        val botonLeer = findViewById<Button>(R.id.botonLeer)

        botonGuardar.setOnClickListener {
            if (tienePermiso()) {
                guardarArchivo()
            } else {
                accionPendiente = "guardar"
                pedirPermiso()
            }
        }

        botonLeer.setOnClickListener {
            if (tienePermiso()) {
                leerArchivo()
            } else {
                accionPendiente = "leer"
                pedirPermiso()
            }
        }
    }

    private fun tienePermiso(): Boolean {
        if (radioInterno.isChecked) {
            return true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true
        }
        val permisoEscritura = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permisoLectura = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        return permisoEscritura == PackageManager.PERMISSION_GRANTED &&
                permisoLectura == PackageManager.PERMISSION_GRANTED
    }

    private fun pedirPermiso() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            codigoPermiso
        )
    }

    override fun onRequestPermissionsResult(
        codigo: Int,
        permisos: Array<out String>,
        resultados: IntArray
    ) {
        super.onRequestPermissionsResult(codigo, permisos, resultados)
        if (codigo != codigoPermiso) {
            return
        }
        var concedido = resultados.isNotEmpty()
        for (resultado in resultados) {
            if (resultado != PackageManager.PERMISSION_GRANTED) {
                concedido = false
            }
        }
        if (concedido) {
            if (accionPendiente == "guardar") {
                guardarArchivo()
            } else if (accionPendiente == "leer") {
                leerArchivo()
            }
        } else {
            textoResultado.text = "Permiso de almacenamiento denegado"
        }
        accionPendiente = ""
    }

    private fun obtenerArchivo(): File? {
        if (radioInterno.isChecked) {
            return File(filesDir, nombreArchivo)
        }
        val carpetas = getExternalFilesDirs(null)
        var carpeta: File? = carpetas[0]
        if (carpetas.size > 1 && carpetas[1] != null) {
            carpeta = carpetas[1]
        }
        if (carpeta == null) {
            return null
        }
        val estado = Environment.getExternalStorageState(carpeta)
        if (estado != Environment.MEDIA_MOUNTED) {
            return null
        }
        return File(carpeta, nombreArchivo)
    }

    private fun guardarArchivo() {
        val texto = campoTexto.text.toString()
        if (texto.isEmpty()) {
            textoResultado.text = "Escribe un texto antes de guardar"
            return
        }
        val archivo = obtenerArchivo()
        if (archivo == null) {
            textoResultado.text = "La SDCard no esta disponible"
            return
        }
        try {
            val salida = FileOutputStream(archivo)
            salida.write(texto.toByteArray())
            salida.close()
            textoResultado.text = "Archivo guardado en:\n" + archivo.absolutePath
        } catch (e: Exception) {
            textoResultado.text = "Error al guardar: " + e.message
        }
    }

    private fun leerArchivo() {
        val archivo = obtenerArchivo()
        if (archivo == null) {
            textoResultado.text = "La SDCard no esta disponible"
            return
        }
        if (!archivo.exists()) {
            textoResultado.text = "El archivo no existe en esta ubicacion"
            return
        }
        try {
            val lector = FileInputStream(archivo).bufferedReader()
            val contenido = lector.readText().trim()
            lector.close()
            campoTexto.setText(contenido)
            textoResultado.text = "Contenido leido:\n" + contenido
        } catch (e: Exception) {
            textoResultado.text = "Error al leer: " + e.message
        }
    }
}
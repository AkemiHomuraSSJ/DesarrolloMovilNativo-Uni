package com.example.sharedpreferences

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var campoNombre: EditText
    private lateinit var campoEdad: EditText
    private lateinit var textoEstado: TextView
    private lateinit var preferencias: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        campoNombre = findViewById(R.id.campoNombre)
        campoEdad = findViewById(R.id.campoEdad)
        textoEstado = findViewById(R.id.textoEstado)

        preferencias = getSharedPreferences("datos_usuario", MODE_PRIVATE)

        val botonGuardar: Button = findViewById(R.id.botonGuardar)
        val botonCargar: Button = findViewById(R.id.botonCargar)
        val botonBorrar: Button = findViewById(R.id.botonBorrar)

        botonGuardar.setOnClickListener {
            guardarDatos()
        }

        botonCargar.setOnClickListener {
            cargarDatos()
        }

        botonBorrar.setOnClickListener {
            borrarDatos()
        }

        cargarDatos()
    }

    private fun guardarDatos() {
        val nombre = campoNombre.text.toString()
        val edad = campoEdad.text.toString()

        val editor = preferencias.edit()
        editor.putString("nombre", nombre)
        editor.putString("edad", edad)
        editor.apply()

        textoEstado.text = "Datos guardados correctamente"
        Toast.makeText(this, "Datos guardados", Toast.LENGTH_SHORT).show()
    }

    private fun cargarDatos() {
        val nombre = preferencias.getString("nombre", "")
        val edad = preferencias.getString("edad", "")

        campoNombre.setText(nombre)
        campoEdad.setText(edad)

        if (nombre.isNullOrEmpty() && edad.isNullOrEmpty()) {
            textoEstado.text = "No hay datos guardados"
        } else {
            textoEstado.text = "Datos cargados correctamente"
        }
    }

    private fun borrarDatos() {
        val editor = preferencias.edit()
        editor.clear()
        editor.apply()

        campoNombre.setText("")
        campoEdad.setText("")
        textoEstado.text = "Datos borrados"
        Toast.makeText(this, "Datos borrados", Toast.LENGTH_SHORT).show()
    }
}
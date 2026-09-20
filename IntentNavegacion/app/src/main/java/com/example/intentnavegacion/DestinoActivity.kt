package com.example.intentnavegacion

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DestinoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destino)

        val nombre = intent.getStringExtra("nombre") ?: ""
        val edad = intent.getStringExtra("edad") ?: ""
        val correo = intent.getStringExtra("correo") ?: ""

        findViewById<TextView>(R.id.textoNombre).text = "Nombre: $nombre"
        findViewById<TextView>(R.id.textoEdad).text = "Edad: $edad"
        findViewById<TextView>(R.id.textoCorreo).text = "Correo: $correo"

        findViewById<Button>(R.id.botonVolver).setOnClickListener {
            finish()
        }
    }
}
package com.example.intentnavegacion

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val campoNombre = findViewById<EditText>(R.id.campoNombre)
        val campoEdad = findViewById<EditText>(R.id.campoEdad)
        val campoCorreo = findViewById<EditText>(R.id.campoCorreo)
        val botonEnviar = findViewById<Button>(R.id.botonEnviar)

        botonEnviar.setOnClickListener {
            val nombre = campoNombre.text.toString().trim()
            val edad = campoEdad.text.toString().trim()
            val correo = campoCorreo.text.toString().trim()

            if (nombre.isEmpty() || edad.isEmpty() || correo.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DestinoActivity::class.java)
            intent.putExtra("nombre", nombre)
            intent.putExtra("edad", edad)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }
    }
}
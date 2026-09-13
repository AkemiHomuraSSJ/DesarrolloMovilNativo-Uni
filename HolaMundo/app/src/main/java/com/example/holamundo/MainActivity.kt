package com.example.holamundo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val editTextNombre = findViewById<EditText>(R.id.editTextNombre)
        val botonSaludar = findViewById<Button>(R.id.botonSaludar)
        val textViewResultado = findViewById<TextView>(R.id.textViewResultado)

        botonSaludar.setOnClickListener {
            val nombre = editTextNombre.text.toString()
            val saludo = "Hola, " + nombre
            textViewResultado.text = saludo
        }
    }
}
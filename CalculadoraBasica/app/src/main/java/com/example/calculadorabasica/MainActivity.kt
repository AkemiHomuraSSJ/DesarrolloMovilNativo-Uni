package com.example.calculadorabasica

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var pantalla: TextView
    private var numeroActual = ""
    private var primerNumero = 0.0
    private var operadorSeleccionado = ""
    private var esperandoSegundoNumero = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pantalla = findViewById(R.id.pantalla)
    }

    fun numeroPresionado(view: View) {
        val boton = view as Button
        val numeroPresionado = boton.text.toString()

        if (esperandoSegundoNumero) {
            numeroActual = ""
            esperandoSegundoNumero = false
        }

        numeroActual += numeroPresionado
        pantalla.text = numeroActual
    }

    fun operacionPresionada(view: View) {
        val boton = view as Button
        val operador = boton.text.toString()

        if (numeroActual.isEmpty()) {
            Toast.makeText(this, "Primero escribe un numero", Toast.LENGTH_SHORT).show()
            return
        }

        primerNumero = numeroActual.toDouble()
        operadorSeleccionado = operador
        esperandoSegundoNumero = true
    }

    fun igualPresionado(view: View) {

        if (numeroActual.isEmpty()) {
            Toast.makeText(this, "Falta escribir el segundo numero", Toast.LENGTH_SHORT).show()
            return
        }

        if (operadorSeleccionado.isEmpty()) {
            Toast.makeText(this, "Primero elige una operacion", Toast.LENGTH_SHORT).show()
            return
        }

        val segundoNumero = numeroActual.toDouble()
        var resultado = 0.0

        when (operadorSeleccionado) {
            "+" -> resultado = primerNumero + segundoNumero
            "-" -> resultado = primerNumero - segundoNumero
            "x" -> resultado = primerNumero * segundoNumero
            "/" -> {
                if (segundoNumero == 0.0) {
                    Toast.makeText(this, "Error: no se puede dividir entre cero", Toast.LENGTH_LONG).show()
                    return
                }
                resultado = primerNumero / segundoNumero
            }
        }

        val resultadoTexto = formatearResultado(resultado)
        pantalla.text = resultadoTexto

        numeroActual = resultadoTexto
        operadorSeleccionado = ""
        esperandoSegundoNumero = true
    }

    fun limpiarPresionado(view: View) {
        numeroActual = ""
        primerNumero = 0.0
        operadorSeleccionado = ""
        esperandoSegundoNumero = false
        pantalla.text = "0"
    }

    fun formatearResultado(numero: Double): String {
        return if (numero == numero.toLong().toDouble()) {
            numero.toLong().toString()
        } else {
            numero.toString()
        }
    }
}
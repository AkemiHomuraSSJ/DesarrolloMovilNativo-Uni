package com.example.controlesavanzados

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val carreras = arrayOf("Sistemas", "Industrial", "Civil", "Electronica")
    private val pasatiempos = mutableListOf<String>()
    private var carrera = carreras[0]
    private var turno = "Ninguno"
    private var notificaciones = "Desactivadas"
    private var nivel = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spCarrera = findViewById<Spinner>(R.id.spCarrera)
        val rgTurno = findViewById<RadioGroup>(R.id.rgTurno)
        val cbMusica = findViewById<CheckBox>(R.id.cbMusica)
        val cbDeportes = findViewById<CheckBox>(R.id.cbDeportes)
        val cbLectura = findViewById<CheckBox>(R.id.cbLectura)
        val swNotificaciones = findViewById<Switch>(R.id.swNotificaciones)
        val sbNivel = findViewById<SeekBar>(R.id.sbNivel)
        val tvNivel = findViewById<TextView>(R.id.tvNivel)
        val btnProcesar = findViewById<Button>(R.id.btnProcesar)
        val tvResumen = findViewById<TextView>(R.id.tvResumen)

        spCarrera.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, carreras)

        spCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, posicion: Int, id: Long) {
                carrera = carreras[posicion]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        rgTurno.setOnCheckedChangeListener { _, idSeleccionado ->
            turno = findViewById<RadioButton>(idSeleccionado).text.toString()
        }

        val escuchadorPasatiempos = CompoundButton.OnCheckedChangeListener { boton, marcado ->
            val texto = boton.text.toString()
            if (marcado) {
                pasatiempos.add(texto)
            } else {
                pasatiempos.remove(texto)
            }
        }
        cbMusica.setOnCheckedChangeListener(escuchadorPasatiempos)
        cbDeportes.setOnCheckedChangeListener(escuchadorPasatiempos)
        cbLectura.setOnCheckedChangeListener(escuchadorPasatiempos)

        swNotificaciones.setOnCheckedChangeListener { _, activado ->
            notificaciones = if (activado) "Activadas" else "Desactivadas"
        }

        sbNivel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(barra: SeekBar?, valor: Int, usuario: Boolean) {
                nivel = valor
                tvNivel.text = "Nivel de interes: $valor"
            }

            override fun onStartTrackingTouch(barra: SeekBar?) {}

            override fun onStopTrackingTouch(barra: SeekBar?) {}
        })

        btnProcesar.setOnClickListener {
            val lista = if (pasatiempos.isEmpty()) "Ninguno" else pasatiempos.joinToString(", ")
            tvResumen.text = "Resumen:\n" +
                    "Carrera: $carrera\n" +
                    "Turno: $turno\n" +
                    "Pasatiempos: $lista\n" +
                    "Notificaciones: $notificaciones\n" +
                    "Nivel de interes: $nivel"
        }
    }
}
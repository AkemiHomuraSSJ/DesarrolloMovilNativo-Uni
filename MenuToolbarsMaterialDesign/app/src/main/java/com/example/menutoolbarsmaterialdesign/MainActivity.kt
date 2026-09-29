package com.example.menutoolbarsmaterialdesign

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private lateinit var textoResultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val barraSuperior = findViewById<MaterialToolbar>(R.id.barraSuperior)
        setSupportActionBar(barraSuperior)
        supportActionBar?.title = "Práctica 07"

        textoResultado = findViewById(R.id.textoResultado)

        barraSuperior.setNavigationOnClickListener {
            mostrarAccion("Navegación")
        }

        val botonPopup = findViewById<Button>(R.id.botonPopup)
        botonPopup.setOnClickListener { vista ->
            mostrarMenuPopup(vista)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.accion_buscar -> mostrarAccion("Buscar")
            R.id.accion_compartir -> mostrarAccion("Compartir")
            R.id.accion_configuracion -> mostrarAccion("Configuración")
            R.id.accion_acerca -> mostrarAccion("Acerca de")
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun mostrarMenuPopup(vista: View) {
        val menuPopup = PopupMenu(this, vista)
        menuPopup.menuInflater.inflate(R.menu.menu_popup, menuPopup.menu)
        menuPopup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.popup_nuevo -> mostrarAccion("Popup: Nuevo")
                R.id.popup_editar -> mostrarAccion("Popup: Editar")
                R.id.popup_eliminar -> mostrarAccion("Popup: Eliminar")
            }
            true
        }
        menuPopup.show()
    }

    private fun mostrarAccion(nombre: String) {
        textoResultado.text = "Acción seleccionada: $nombre"
        Toast.makeText(this, nombre, Toast.LENGTH_SHORT).show()
    }
}
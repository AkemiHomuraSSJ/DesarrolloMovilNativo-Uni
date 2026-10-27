package com.example.redsociallocal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PublicacionViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    private val dao = BaseDatos.obtener(aplicacion).publicacionDao()

    val publicaciones: StateFlow<List<Publicacion>> = dao.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardar(titulo: String, descripcion: String, fotoUri: String, fecha: Long) {
        viewModelScope.launch {
            dao.insertar(
                Publicacion(
                    titulo = titulo,
                    descripcion = descripcion,
                    fotoUri = fotoUri,
                    fecha = fecha
                )
            )
        }
    }
}
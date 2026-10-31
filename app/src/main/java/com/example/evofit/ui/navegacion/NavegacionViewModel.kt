package com.example.evofit.ui.navegacion

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface Destino {
    data object Inicio : Destino
    data object Recomendadas : Destino
    data object Crear : Destino
    data object MisRutinas : Destino
    data object Realizar : Destino
    data object Resumen : Destino
    data object Historial : Destino
}

data class EdicionRutina(val id: Long?, val token: Long)

class NavegacionViewModel : ViewModel() {
    private val _destino = MutableStateFlow<Destino>(Destino.Inicio)
    val destino: StateFlow<Destino> = _destino.asStateFlow()
    private val _edicion = MutableStateFlow(EdicionRutina(null, 0L))
    val edicion: StateFlow<EdicionRutina> = _edicion.asStateFlow()

    fun abrir(destino: Destino) {
        if (destino == Destino.Crear) {
            _edicion.value = EdicionRutina(null, System.currentTimeMillis())
        }
        _destino.value = destino
    }

    fun editarRutina(id: Long) {
        _edicion.value = EdicionRutina(id, System.currentTimeMillis())
        _destino.value = Destino.Crear
    }

    fun volver() {
        _destino.value = Destino.Inicio
    }
}

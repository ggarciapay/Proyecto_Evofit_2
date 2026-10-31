package com.example.evofit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evofit.ui.EvoFitViewModel
import com.example.evofit.ui.Pantalla
import com.example.evofit.ui.crear.CrearRutinaScreen
import com.example.evofit.ui.historial.HistorialScreen
import com.example.evofit.ui.misrutinas.MisRutinasScreen
import com.example.evofit.ui.navegacion.Destino
import com.example.evofit.ui.navegacion.NavegacionViewModel
import com.example.evofit.ui.principal.PrincipalScreen
import com.example.evofit.ui.realizar.RealizarRutinaScreen
import com.example.evofit.ui.recomendadas.RecomendadasScreen
import com.example.evofit.ui.resumen.ResumenMensualScreen
import com.example.evofit.ui.registro.RegistroScreen
import com.example.evofit.ui.theme.EvoBackground
import com.example.evofit.ui.theme.EvoFitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fondo = 0xFF031725.toInt()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(fondo),
            navigationBarStyle = SystemBarStyle.dark(fondo)
        )
        setContent {
            EvoFitTheme {
                EvoFitApp()
            }
        }
    }
}

@Composable
private fun EvoFitApp(viewModel: EvoFitViewModel = viewModel()) {
    val pantalla by viewModel.pantalla.collectAsStateWithLifecycle()
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()

    when (val estado = pantalla) {
        Pantalla.Cargando -> Box(Modifier.fillMaxSize().background(EvoBackground))
        Pantalla.Registro -> RegistroScreen(
            estado = formulario,
            onCorreo = viewModel::onCorreoChange,
            onNombre = viewModel::onNombreChange,
            onContrasena = viewModel::onContrasenaChange,
            onConfirmar = viewModel::onConfirmarChange,
            onEdad = viewModel::onEdadChange,
            onSexo = viewModel::onSexoChange,
            onRegistrar = viewModel::registrar
        )
        is Pantalla.Principal -> ZonaPrincipal(estado.nombreUsuario)
    }
}

@Composable
private fun ZonaPrincipal(nombreUsuario: String, navegacion: NavegacionViewModel = viewModel()) {
    val destino by navegacion.destino.collectAsStateWithLifecycle()
    val edicion by navegacion.edicion.collectAsStateWithLifecycle()
    BackHandler(enabled = destino !is Destino.Inicio) { navegacion.volver() }
    when (destino) {
        Destino.Inicio -> PrincipalScreen(
            nombreUsuario = nombreUsuario,
            onRealizar = { navegacion.abrir(Destino.Realizar) },
            onCrear = { navegacion.abrir(Destino.Crear) },
            onRecomendadas = { navegacion.abrir(Destino.Recomendadas) },
            onMisRutinas = { navegacion.abrir(Destino.MisRutinas) },
            onResumen = { navegacion.abrir(Destino.Resumen) },
            onHistorial = { navegacion.abrir(Destino.Historial) }
        )
        Destino.Recomendadas -> RecomendadasScreen(onAtras = navegacion::volver)
        Destino.Crear -> CrearRutinaScreen(
            onAtras = navegacion::volver,
            edicionToken = edicion.token,
            rutinaId = edicion.id
        )
        Destino.MisRutinas -> MisRutinasScreen(
            onAtras = navegacion::volver,
            onEditar = navegacion::editarRutina
        )
        Destino.Realizar -> RealizarRutinaScreen(onAtras = navegacion::volver)
        Destino.Resumen -> ResumenMensualScreen(onAtras = navegacion::volver)
        Destino.Historial -> HistorialScreen(onAtras = navegacion::volver)
    }
}

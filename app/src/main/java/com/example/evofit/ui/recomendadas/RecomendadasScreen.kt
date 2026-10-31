package com.example.evofit.ui.recomendadas

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evofit.data.ResultadoGuardado
import com.example.evofit.data.catalogo.DiaPlantilla
import com.example.evofit.data.catalogo.RutinaPlantilla
import com.example.evofit.data.catalogo.RutinasRecomendadas
import com.example.evofit.data.catalogo.descripcionSeries
import com.example.evofit.data.repositorioRutinas
import com.example.evofit.ui.comun.BarraPantalla
import com.example.evofit.ui.comun.BotonVerde
import com.example.evofit.ui.comun.FondoEvo
import com.example.evofit.ui.comun.tarjetaEvo
import com.example.evofit.ui.theme.EvoBorder
import com.example.evofit.ui.theme.EvoCard
import com.example.evofit.ui.theme.EvoError
import com.example.evofit.ui.theme.EvoGreen
import com.example.evofit.ui.theme.EvoOnGreen
import com.example.evofit.ui.theme.EvoTextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecomendadasViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = repositorioRutinas(application)
    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()
    private val _esError = MutableStateFlow(false)
    val esError: StateFlow<Boolean> = _esError.asStateFlow()

    fun agregar(plantilla: RutinaPlantilla) {
        if (_guardando.value || plantilla.dias.isEmpty()) return
        _guardando.value = true
        viewModelScope.launch {
            when (val resultado = repositorio.copiarRecomendada(plantilla)) {
                ResultadoGuardado.Exito -> {
                    _esError.value = false
                    _mensaje.value = "Rutina agregada a Mis rutinas."
                }
                is ResultadoGuardado.Aviso -> {
                    _esError.value = true
                    _mensaje.value = resultado.mensaje
                }
                is ResultadoGuardado.Error -> {
                    _esError.value = true
                    _mensaje.value = resultado.mensaje
                }
            }
            _guardando.value = false
        }
    }
}

@Composable
fun RecomendadasScreen(onAtras: () -> Unit, viewModel: RecomendadasViewModel = viewModel()) {
    val guardando by viewModel.guardando.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val esError by viewModel.esError.collectAsStateWithLifecycle()
    var nivel by rememberSaveable { mutableStateOf(RutinasRecomendadas.niveles.first()) }
    var diaNumero by rememberSaveable { mutableIntStateOf(1) }
    val plantilla = RutinasRecomendadas.deNivel(nivel)
    LaunchedEffect(nivel) {
        val primero = plantilla.dias.firstOrNull { !it.esDescanso }?.dia?.numero ?: return@LaunchedEffect
        if (plantilla.dias.none { it.dia.numero == diaNumero }) diaNumero = primero
    }
    val diaSeleccionado = plantilla.dias.find { it.dia.numero == diaNumero }

    Column(Modifier.fillMaxSize().background(FondoEvo)) {
        BarraPantalla("Rutinas recomendadas", onAtras)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RutinasRecomendadas.niveles.forEach { opcion ->
                    val activo = opcion == nivel
                    Text(
                        text = opcion,
                        color = if (activo) EvoOnGreen else EvoTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (activo) EvoGreen else Color.Transparent)
                            .clickable { nivel = opcion }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            if (plantilla.dias.isEmpty()) {
                Text(
                    "El plan detallado de este nivel todavía no está cargado.",
                    color = EvoTextSecondary,
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else {
                TarjetaResumen(plantilla)
                Text(
                    "DISTRIBUCIÓN SEMANAL",
                    color = EvoTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                )
                plantilla.dias.forEach { dia ->
                    FilaDia(
                        dia = dia,
                        seleccionado = dia.dia.numero == diaNumero,
                        onClick = { diaNumero = dia.dia.numero }
                    )
                }
                Text(
                    "DETALLE DE EJERCICIOS",
                    color = EvoTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                )
                if (diaSeleccionado != null) DetalleDia(diaSeleccionado)
                if (plantilla.recomendaciones.isNotBlank()) {
                    Text(
                        "RECOMENDACIONES GENERALES",
                        color = EvoTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                    )
                    Column(Modifier.tarjetaEvo().padding(14.dp)) {
                        plantilla.recomendaciones.lines().filter { it.isNotBlank() }.forEach { linea ->
                            Text("• $linea", color = Color.White, modifier = Modifier.padding(bottom = 6.dp))
                        }
                    }
                }
            }
        }
        if (mensaje != null) {
            Text(
                mensaje.orEmpty(),
                color = if (esError) EvoError else EvoGreen,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        BotonVerde(
            texto = "Agregar a mis rutinas",
            onClick = { viewModel.agregar(plantilla) },
            habilitado = plantilla.dias.isNotEmpty(),
            cargando = guardando,
            icono = {
                Icon(Icons.Filled.Add, contentDescription = null, tint = EvoOnGreen)
            }
        )
    }
}

@Composable
private fun TarjetaResumen(plantilla: RutinaPlantilla) {
    Column(Modifier.fillMaxWidth().padding(top = 16.dp).tarjetaEvo().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = EvoGreen)
            Text(
                plantilla.nombre,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp).weight(1f)
            )
            Text(
                "${plantilla.diasEntrenamiento} días de entrenamiento",
                color = EvoGreen,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(EvoGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        if (plantilla.objetivo.isNotBlank()) {
            Text(plantilla.objetivo, color = EvoTextSecondary, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun FilaDia(dia: DiaPlantilla, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(EvoCard)
            .border(1.dp, if (seleccionado) EvoGreen else EvoBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(dia.dia.nombre, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(dia.etiqueta, color = if (dia.esDescanso) EvoTextSecondary else EvoGreen, fontSize = 14.sp)
    }
}

@Composable
private fun DetalleDia(dia: DiaPlantilla) {
    Column(Modifier.fillMaxWidth().tarjetaEvo().padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${dia.dia.nombre} – ${dia.etiqueta}",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (dia.ejercicios.size == 1) "1 Ejercicio" else "${dia.ejercicios.size} Ejercicios",
                color = EvoTextSecondary,
                fontSize = 13.sp
            )
        }
        if (dia.esDescanso) {
            Text("Día de descanso.", color = EvoTextSecondary, modifier = Modifier.padding(top = 10.dp))
        } else if (dia.ejercicios.isEmpty()) {
            Text(
                "Esta jornada todavía no tiene ejercicios cargados.",
                color = EvoTextSecondary,
                modifier = Modifier.padding(top = 10.dp)
            )
        } else {
            dia.ejercicios.forEachIndexed { indice, ejercicio ->
                Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text("${indice + 1}. ${ejercicio.nombre}", color = Color.White, fontWeight = FontWeight.Medium)
                    Text(
                        descripcionSeries(ejercicio.series, ejercicio.repeticiones),
                        color = EvoTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

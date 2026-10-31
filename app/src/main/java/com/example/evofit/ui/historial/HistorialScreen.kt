package com.example.evofit.ui.historial

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.evofit.data.SEMANAS_POR_CICLO
import com.example.evofit.data.SesionCiclo
import com.example.evofit.data.catalogo.DiaSemana
import com.example.evofit.data.repositorioRutinas
import com.example.evofit.data.sesionesEvaluadas
import com.example.evofit.ui.comun.BarraPantalla
import com.example.evofit.ui.theme.EvoBackground
import com.example.evofit.ui.theme.EvoBorder
import com.example.evofit.ui.theme.EvoCard
import com.example.evofit.ui.theme.EvoGreen
import com.example.evofit.ui.theme.EvoTextSecondary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

data class SemanaHistorial(val semana: Int, val fecha: String, val lineas: List<String>)

data class CicloHistorial(
    val rutina: String,
    val dia: String,
    val ciclo: Int,
    val completo: Boolean,
    val reciente: Long,
    val semanas: List<SemanaHistorial>
)

class HistorialViewModel(application: Application) : AndroidViewModel(application) {
    val ciclos: StateFlow<List<CicloHistorial>> = repositorioRutinas(application)
        .observarSeriesCompletadas()
        .map { filas -> armarHistorial(sesionesEvaluadas(filas)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

private fun armarHistorial(sesiones: List<SesionCiclo>): List<CicloHistorial> {
    return sesiones.groupBy { "${it.rutinaId ?: it.nombreRutina}|${it.diaSemana}|${it.ciclo}" }
        .values
        .map { grupo ->
            val ordenadas = grupo.sortedBy { it.semana }
            val primera = ordenadas.first()
            val nombreDia = DiaSemana.entries.find { it.numero == primera.diaSemana }?.nombre ?: "Día"
            CicloHistorial(
                rutina = primera.nombreRutina,
                dia = "$nombreDia – ${primera.etiquetaDia}",
                ciclo = primera.ciclo,
                completo = ordenadas.size >= SEMANAS_POR_CICLO,
                reciente = ordenadas.maxOf { it.inicioMillis },
                semanas = ordenadas.map { sesion ->
                    SemanaHistorial(
                        semana = sesion.semana,
                        fecha = fechaLarga(sesion.fecha),
                        lineas = sesion.filas.groupBy { it.ejercicioNombre }
                            .entries
                            .sortedBy { it.value.minOf { fila -> fila.orden } }
                            .map { (nombre, series) -> lineaEjercicio(nombre, series) }
                    )
                }
            )
        }
        .sortedByDescending { it.reciente }
}

private fun lineaEjercicio(nombre: String, series: List<com.example.evofit.data.local.FilaSerieResumen>): String {
    val unidad = series.map { it.unidad }.distinct().singleOrNull() ?: "KG"
    val pesos = if (series.map { it.unidad }.distinct().size == 1) {
        series.map { it.peso }
    } else {
        series.map { if (it.unidad.equals("LB", true)) it.peso / 2.20462 else it.peso }
    }
    val unidadTexto = if (series.map { it.unidad }.distinct().size == 1) unidad else "KG"
    val promedio = pesos.average()
    val texto = if (abs(promedio - promedio.roundToInt()) < 0.05) {
        promedio.roundToInt().toString()
    } else {
        String.format(Locale.ROOT, "%.1f", promedio)
    }
    return "$nombre · $texto $unidadTexto × ${series.maxOf { it.repeticiones }} reps · ${series.size} series"
}

private fun fechaLarga(fecha: String): String {
    val partes = fecha.split("-")
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else fecha
}

@Composable
fun HistorialScreen(onAtras: () -> Unit, viewModel: HistorialViewModel = viewModel()) {
    val ciclos by viewModel.ciclos.collectAsStateWithLifecycle()
    Scaffold(containerColor = EvoBackground, topBar = { BarraPantalla("Historial", onAtras) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ciclos.isEmpty()) {
                Text(
                    "Todavía no hay sesiones guardadas. Cada rutina terminada se registra aquí como semana 1, 2, 3 o 4.",
                    color = EvoTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(EvoCard)
                        .border(1.dp, EvoBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                )
            } else {
                ciclos.forEach { ciclo -> TarjetaCiclo(ciclo) }
            }
        }
    }
}

@Composable
private fun TarjetaCiclo(ciclo: CicloHistorial) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EvoCard)
            .border(1.dp, EvoBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(ciclo.rutina, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(ciclo.dia, color = EvoTextSecondary, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ciclo ${ciclo.ciclo}", color = EvoGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                if (ciclo.completo) "4 de 4 semanas" else "${ciclo.semanas.size} de 4 semanas",
                color = EvoTextSecondary,
                fontSize = 13.sp
            )
        }
        ciclo.semanas.forEach { semana ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EvoBackground, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Semana ${semana.semana} · ${semana.fecha}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                semana.lineas.forEach { linea ->
                    Text(linea, color = EvoTextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

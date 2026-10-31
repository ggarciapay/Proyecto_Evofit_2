package com.example.evofit.ui.misrutinas

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.evofit.data.catalogo.DiaGuardado
import com.example.evofit.data.catalogo.RutinaGuardada
import com.example.evofit.data.catalogo.descripcionSeries
import com.example.evofit.data.repositorioRutinas
import com.example.evofit.ui.comun.BarraPantalla
import com.example.evofit.ui.comun.FondoEvo
import com.example.evofit.ui.comun.tarjetaEvo
import com.example.evofit.ui.theme.EvoBorder
import com.example.evofit.ui.theme.EvoCard
import com.example.evofit.ui.theme.EvoError
import com.example.evofit.ui.theme.EvoGreen
import com.example.evofit.ui.theme.EvoOnGreen
import com.example.evofit.ui.theme.EvoTextSecondary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MisRutinasViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = repositorioRutinas(application)
    val rutinas: StateFlow<List<RutinaGuardada>> = repositorio
        .observar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun eliminar(id: Long) {
        viewModelScope.launch { repositorio.eliminar(id) }
    }
}

@Composable
fun MisRutinasScreen(
    onAtras: () -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: MisRutinasViewModel = viewModel()
) {
    val rutinas by viewModel.rutinas.collectAsStateWithLifecycle()
    var seleccionada by rememberSaveable { mutableStateOf<Long?>(null) }
    var diaNumero by rememberSaveable { mutableIntStateOf(1) }
    var confirmarBorrado by rememberSaveable { mutableStateOf(false) }
    val rutina = rutinas.find { it.id == seleccionada }

    Column(Modifier.fillMaxSize().background(FondoEvo)) {
        BarraPantalla("Mis rutinas", onAtras)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (rutinas.isEmpty()) {
                Text(
                    "Todavía no tienes rutinas. Agrégalas desde Rutinas recomendadas o Crear rutina.",
                    color = EvoTextSecondary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                Text(
                    "Tus rutinas",
                    color = EvoTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                rutinas.sortedBy { it.nombre }.forEach { item ->
                    val activa = item.id == rutina?.id
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (activa) EvoGreen.copy(alpha = 0.12f) else EvoCard)
                            .border(1.dp, if (activa) EvoGreen else EvoBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                seleccionada = item.id
                                diaNumero = item.dias.firstOrNull { !it.esDescanso }?.dia?.numero ?: 1
                            }
                            .padding(14.dp)
                    ) {
                        Text(item.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "${item.nivel} · ${item.dias.count { !it.esDescanso }} días",
                            color = if (activa) EvoGreen else EvoTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                if (rutina != null) {
                    DetalleGuardada(rutina, diaNumero) { diaNumero = it }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Editar",
                            color = EvoOnGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EvoGreen)
                                .clickable { onEditar(rutina.id) }
                                .padding(vertical = 14.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            "Eliminar",
                            color = EvoError,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, EvoError, RoundedCornerShape(14.dp))
                                .clickable { confirmarBorrado = true }
                                .padding(vertical = 14.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
    if (confirmarBorrado && rutina != null) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            containerColor = EvoCard,
            title = { Text("Eliminar rutina", color = Color.White) },
            text = { Text("Se eliminará \"${rutina.nombre}\". Los entrenamientos ya guardados no se borran.", color = EvoTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(rutina.id)
                    seleccionada = null
                    confirmarBorrado = false
                }) { Text("Eliminar", color = EvoError) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar", color = EvoTextSecondary) }
            }
        )
    }
}

@Composable
private fun DetalleGuardada(rutina: RutinaGuardada, diaNumero: Int, onDia: (Int) -> Unit) {
    val dia = rutina.dias.find { it.dia.numero == diaNumero } ?: rutina.dias.firstOrNull()
    if (rutina.objetivo.isNotBlank()) {
        Text(rutina.objetivo, color = EvoTextSecondary, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
    }
    Text(
        "DISTRIBUCIÓN SEMANAL",
        color = EvoTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
    )
    rutina.dias.forEach { item ->
        Fila(item, item.dia.numero == dia?.dia?.numero) { onDia(item.dia.numero) }
    }
    Text(
        "DETALLE DE EJERCICIOS",
        color = EvoTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
    )
    if (dia != null) {
        Column(Modifier.fillMaxWidth().tarjetaEvo().padding(14.dp)) {
            Text("${dia.dia.nombre} – ${dia.etiqueta}", color = Color.White, fontWeight = FontWeight.SemiBold)
            when {
                dia.esDescanso -> Text("Día de descanso.", color = EvoTextSecondary, modifier = Modifier.padding(top = 8.dp))
                dia.ejercicios.isEmpty() -> Text("Este día no tiene ejercicios.", color = EvoTextSecondary, modifier = Modifier.padding(top = 8.dp))
                else -> dia.ejercicios.forEachIndexed { indice, ejercicio ->
                    Text("${indice + 1}. ${ejercicio.nombre}", color = Color.White, modifier = Modifier.padding(top = 10.dp))
                    Text(
                        descripcionSeries(ejercicio.series, ejercicio.repeticiones),
                        color = EvoTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
    if (rutina.recomendaciones.isNotBlank()) {
        Text(
            "RECOMENDACIONES GENERALES",
            color = EvoTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Column(Modifier.tarjetaEvo().padding(14.dp)) {
            rutina.recomendaciones.lines().filter { it.isNotBlank() }.forEach { linea ->
                Text("• $linea", color = Color.White, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
    }
}

@Composable
private fun Fila(dia: DiaGuardado, seleccionado: Boolean, onClick: () -> Unit) {
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
        Text(dia.etiqueta, color = if (dia.esDescanso) EvoTextSecondary else EvoGreen, fontSize = 13.sp)
    }
}

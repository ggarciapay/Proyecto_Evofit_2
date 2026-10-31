package com.example.evofit.ui.resumen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evofit.data.SEMANAS_POR_CICLO
import com.example.evofit.data.SesionCiclo
import com.example.evofit.data.catalogo.DiaGuardado
import com.example.evofit.data.catalogo.RutinaGuardada
import com.example.evofit.data.catalogo.UnidadPeso
import com.example.evofit.data.local.FilaSerieResumen
import com.example.evofit.data.repositorioRutinas
import com.example.evofit.data.sesionesEvaluadas
import com.example.evofit.ui.comun.coloresCampo
import com.example.evofit.ui.theme.EvoBackground
import com.example.evofit.ui.theme.EvoBorder
import com.example.evofit.ui.theme.EvoCard
import com.example.evofit.ui.theme.EvoGreen
import com.example.evofit.ui.theme.EvoNavBar
import com.example.evofit.ui.theme.EvoTextSecondary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val FACTOR_LB = 2.20462
private const val TOLERANCIA_KG = 0.25
private val Alerta = Color(0xFFEAB308)
private val LineaPeso = Color(0xFF38BDF8)

data class OpcionResumen(val id: Long, val titulo: String)

data class SemanaEjercicioUi(
    val tieneDatos: Boolean,
    val pesoKg: Double,
    val mejoresReps: Int,
    val series: Int
)

data class EjercicioMesUi(
    val nombre: String,
    val pesoActualKg: Double,
    val repsActuales: Int,
    val seriesActuales: Int,
    val deltaReps: Int?,
    val semanas: List<SemanaEjercicioUi>,
    val pesoSugeridoKg: Double?,
    val repsSugeridas: Int?
)

data class EstadoResumenMensual(
    val tituloCiclo: String,
    val notaCiclo: String,
    val fechasSemana: List<String>,
    val unidad: UnidadPeso,
    val rutinas: List<OpcionResumen>,
    val rutinaId: Long?,
    val dias: List<OpcionResumen>,
    val diaId: Long?,
    val seriesPorSemana: List<Int>,
    val ejercicios: List<EjercicioMesUi>,
    val sinRutinas: Boolean,
    val sinDias: Boolean,
    val sinDatos: Boolean
)

class ResumenViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = repositorioRutinas(application)
    private val rutinaId = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)
    private val diaId = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)
    private val unidad = kotlinx.coroutines.flow.MutableStateFlow(UnidadPeso.KG)

    val estado: StateFlow<EstadoResumenMensual> = combine(
        repositorio.observar(),
        repositorio.observarSeriesCompletadas(),
        rutinaId,
        diaId,
        unidad
    ) { rutinas, series, id, dia, unidadPeso ->
        armarResumen(rutinas, series, id, dia, unidadPeso)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), estadoInicial())

    fun seleccionarRutina(id: Long) {
        rutinaId.value = id
        diaId.value = null
    }

    fun seleccionarDia(id: Long) {
        diaId.value = id
    }

    fun alternarUnidad() {
        unidad.value = if (unidad.value == UnidadPeso.KG) UnidadPeso.LB else UnidadPeso.KG
    }
}

private fun estadoInicial(): EstadoResumenMensual {
    return EstadoResumenMensual(
        tituloCiclo = "Ciclo de 4 semanas",
        notaCiclo = "Cada sesión completada avanza una semana.",
        fechasSemana = List(SEMANAS_POR_CICLO) { "" },
        unidad = UnidadPeso.KG,
        rutinas = emptyList(),
        rutinaId = null,
        dias = emptyList(),
        diaId = null,
        seriesPorSemana = emptyList(),
        ejercicios = emptyList(),
        sinRutinas = true,
        sinDias = false,
        sinDatos = false
    )
}

private fun armarResumen(
    rutinas: List<RutinaGuardada>,
    series: List<FilaSerieResumen>,
    rutinaPedida: Long?,
    diaPedido: Long?,
    unidad: UnidadPeso
): EstadoResumenMensual {
    val ordenadas = rutinas.sortedBy { it.nombre.lowercase(Locale.ROOT) }
    val rutina = ordenadas.find { it.id == rutinaPedida } ?: ordenadas.firstOrNull()
    val dias = rutina?.dias?.filter { !it.esDescanso }?.sortedBy { it.dia.numero }.orEmpty()
    val dia = dias.find { it.id == diaPedido } ?: dias.firstOrNull()
    val sesiones = if (rutina == null || dia == null) {
        emptyList()
    } else {
        sesionesEvaluadas(series).filter { sesion ->
            sesion.diaSemana == dia.dia.numero && pertenece(sesion, rutina)
        }
    }
    val ciclo = sesiones.lastOrNull()?.ciclo
    val delCiclo = if (ciclo == null) emptyList() else sesiones.filter { it.ciclo == ciclo }
    val porSemana = List(SEMANAS_POR_CICLO) { indice ->
        delCiclo.find { it.semana == indice + 1 }?.filas.orEmpty()
    }
    val ejercicios = if (dia == null || delCiclo.isEmpty()) emptyList() else ejerciciosDelDia(porSemana, dia)
    val seriesPorSemana = if (ejercicios.isEmpty()) {
        emptyList()
    } else {
        List(SEMANAS_POR_CICLO) { indice -> ejercicios.sumOf { it.semanas[indice].series } }
    }
    val hechas = delCiclo.size
    return EstadoResumenMensual(
        tituloCiclo = if (ciclo == null) "Ciclo de 4 semanas" else "Ciclo $ciclo",
        notaCiclo = when {
            hechas == 0 -> "La primera sesión completada de este día queda como semana 1."
            hechas >= SEMANAS_POR_CICLO -> "Ciclo completo. La próxima sesión de este día empieza otra vez en la semana 1."
            else -> "Semana $hechas de $SEMANAS_POR_CICLO. Cada sesión completada avanza una semana."
        },
        fechasSemana = List(SEMANAS_POR_CICLO) { indice ->
            delCiclo.find { it.semana == indice + 1 }?.fecha?.let(::fechaCorta).orEmpty()
        },
        unidad = unidad,
        rutinas = ordenadas.map { OpcionResumen(it.id, it.nombre) },
        rutinaId = rutina?.id,
        dias = dias.map { OpcionResumen(it.id, "${it.dia.nombre} – ${it.etiqueta}") },
        diaId = dia?.id,
        seriesPorSemana = seriesPorSemana,
        ejercicios = ejercicios,
        sinRutinas = ordenadas.isEmpty(),
        sinDias = rutina != null && dias.isEmpty(),
        sinDatos = rutina != null && dia != null && ejercicios.isEmpty()
    )
}

private fun pertenece(sesion: SesionCiclo, rutina: RutinaGuardada): Boolean =
    if (sesion.rutinaId != null) sesion.rutinaId == rutina.id
    else sesion.nombreRutina.equals(rutina.nombre, ignoreCase = true)

private fun ejerciciosDelDia(
    porSemana: List<List<FilaSerieResumen>>,
    dia: DiaGuardado
): List<EjercicioMesUi> {
    val config = dia.ejercicios.associateBy { it.nombre }
    val nombres = porSemana.flatten().groupBy { it.ejercicioNombre }
    return nombres.entries
        .sortedWith(compareBy({ it.value.minOf { fila -> fila.orden } }, { it.key }))
        .map { (nombre, registros) ->
            val semanas = porSemana.map { filas ->
                val delPeriodo = filas.filter { it.ejercicioNombre == nombre }
                if (delPeriodo.isEmpty()) {
                    SemanaEjercicioUi(false, 0.0, 0, 0)
                } else {
                    SemanaEjercicioUi(
                        tieneDatos = true,
                        pesoKg = delPeriodo.map { aKilogramos(it.peso, it.unidad) }.average(),
                        mejoresReps = delPeriodo.maxOf { it.repeticiones },
                        series = delPeriodo.size
                    )
                }
            }
            val conDatos = semanas.withIndex().filter { it.value.tieneDatos }
            val primera = conDatos.firstOrNull()?.value
            val ultima = conDatos.lastOrNull()
            val delta = if (primera != null && ultima != null && conDatos.size >= 2) {
                ultima.value.mejoresReps - primera.mejoresReps
            } else {
                null
            }
            val objetivo = config[nombre]?.repeticiones ?: registros.last().repeticionesObjetivo
            val seriesObjetivo = config[nombre]?.series ?: registros.maxOf { it.seriesObjetivo }
            val indicePrevio = conDatos.dropLast(1).lastOrNull()?.index
            val sugerencia = if (ultima == null || indicePrevio == null) {
                null
            } else {
                sugerenciaCarga(
                    porSemana[ultima.index].filter { it.ejercicioNombre == nombre },
                    porSemana[indicePrevio].filter { it.ejercicioNombre == nombre },
                    objetivo,
                    seriesObjetivo
                )
            }
            EjercicioMesUi(
                nombre = nombre,
                pesoActualKg = ultima?.value?.pesoKg ?: 0.0,
                repsActuales = ultima?.value?.mejoresReps ?: 0,
                seriesActuales = ultima?.value?.series ?: 0,
                deltaReps = delta,
                semanas = semanas,
                pesoSugeridoKg = sugerencia?.first,
                repsSugeridas = sugerencia?.second
            )
        }
}

private fun fechaCorta(fecha: String): String {
    val partes = fecha.split("-")
    return if (partes.size == 3) "${partes[2]}/${partes[1]}" else fecha
}

private fun sugerenciaCarga(
    ultimaSemana: List<FilaSerieResumen>,
    anteriores: List<FilaSerieResumen>,
    objetivo: String,
    seriesObjetivo: Int
): Pair<Double, Int>? {
    val limite = limiteSuperior(objetivo) ?: return null
    val trabajo = ultimaSemana.filter { !it.intensidad.equals("Baja", ignoreCase = true) }
    if (trabajo.isEmpty() || seriesObjetivo <= 0 || trabajo.size < seriesObjetivo) return null
    if (trabajo.any { it.repeticiones < limite }) return null
    val pesos = trabajo.map { aKilogramos(it.peso, it.unidad) }
    val promedio = pesos.average()
    if (pesos.any { abs(it - promedio) > TOLERANCIA_KG }) return null
    val previas = anteriores.filter { !it.intensidad.equals("Baja", ignoreCase = true) }
    if (previas.isEmpty()) return null
    val pesoPrevio = previas.map { aKilogramos(it.peso, it.unidad) }.average()
    if (abs(pesoPrevio - promedio) > TOLERANCIA_KG) return null
    return promedio to limite
}

private fun limiteSuperior(objetivo: String): Int? {
    if (objetivo.contains("segundo", ignoreCase = true)) return null
    val numeros = Regex("\\d+").findAll(objetivo).map { it.value.toInt() }.toList()
    return numeros.maxOrNull()?.takeIf { it > 0 }
}

private fun aKilogramos(peso: Double, unidad: String): Double =
    if (unidad.equals(UnidadPeso.LB.etiqueta, ignoreCase = true)) peso / FACTOR_LB else peso

fun textoPeso(pesoKg: Double, unidad: UnidadPeso): String {
    val valor = if (unidad == UnidadPeso.LB) pesoKg * FACTOR_LB else pesoKg
    val redondeado = if (abs(valor - valor.roundToInt()) < 0.05) {
        valor.roundToInt().toString()
    } else {
        String.format(Locale.ROOT, "%.1f", valor)
    }
    return "$redondeado ${unidad.etiqueta}"
}

@Composable
fun ResumenMensualScreen(
    onAtras: () -> Unit,
    viewModel: ResumenViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = EvoBackground,
        topBar = { EncabezadoResumen(estado.unidad, onAtras, viewModel::alternarUnidad) },
        bottomBar = { PieSobrecarga() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BannerCiclo(estado.tituloCiclo, estado.notaCiclo)
            when {
                estado.sinRutinas -> Aviso("Todavía no tienes rutinas guardadas. Al terminar una sesión, queda registrada como semana 1.")
                estado.sinDias -> {
                    Selectores(estado, viewModel::seleccionarRutina, viewModel::seleccionarDia)
                    Aviso("Esta rutina no tiene días de entrenamiento.")
                }
                estado.sinDatos -> {
                    Selectores(estado, viewModel::seleccionarRutina, viewModel::seleccionarDia)
                    Aviso("Todavía no hay series completadas para este día. La primera sesión queda como semana 1.")
                }
                else -> {
                    Selectores(estado, viewModel::seleccionarRutina, viewModel::seleccionarDia)
                    TarjetaSeries(estado.seriesPorSemana, estado.fechasSemana)
                    Text(
                        "AVANCE POR EJERCICIO",
                        color = EvoTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    estado.ejercicios.forEach { ejercicio ->
                        TarjetaEjercicio(ejercicio, estado.unidad)
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoResumen(unidad: UnidadPeso, onAtras: () -> Unit, onUnidad: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver",
            tint = Color.White,
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onAtras)
                .padding(8.dp)
        )
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Resumen Mensual", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Progreso y sobrecarga progresiva", color = EvoGreen, fontSize = 11.sp)
        }
        Text(
            text = unidad.etiqueta,
            color = EvoGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, EvoGreen, RoundedCornerShape(12.dp))
                .clickable(onClick = onUnidad)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun BannerCiclo(titulo: String, nota: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EvoCard)
            .border(1.dp, EvoBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(nota, color = EvoTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun Selectores(
    estado: EstadoResumenMensual,
    onRutina: (Long) -> Unit,
    onDia: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EvoCard)
            .border(1.dp, EvoBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EtiquetaSelector(Icons.Filled.FitnessCenter, "SELECCIONAR RUTINA")
        MenuOpciones(estado.rutinas, estado.rutinaId, onRutina)
        EtiquetaSelector(Icons.Filled.DateRange, "DÍA DE ENTRENAMIENTO")
        MenuOpciones(estado.dias, estado.diaId, onDia)
    }
}

@Composable
private fun EtiquetaSelector(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icono, contentDescription = null, tint = EvoGreen, modifier = Modifier.size(16.dp))
        Text(texto, color = EvoTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuOpciones(opciones: List<OpcionResumen>, seleccionado: Long?, onElegir: (Long) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    val titulo = opciones.find { it.id == seleccionado }?.titulo.orEmpty()
    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { if (opciones.isNotEmpty()) abierto = it }) {
        OutlinedTextField(
            value = titulo,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(abierto) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = coloresCampo()
        )
        ExposedDropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            modifier = Modifier.heightIn(max = 320.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = EvoCard
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opcion.titulo,
                            color = if (opcion.id == seleccionado) EvoGreen else Color.White,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onElegir(opcion.id)
                        abierto = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaSeries(series: List<Int>, fechas: List<String>) {
    if (series.isEmpty()) return
    val primera = series.indexOfFirst { it > 0 }
    val ultima = series.indexOfLast { it > 0 }
    val diferencia = if (primera >= 0 && ultima >= 0) series[ultima] - series[primera] else 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EvoCard)
            .border(1.dp, EvoBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(EvoGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Layers, contentDescription = null, tint = EvoGreen)
                }
                Column {
                    Text("Series completadas del ciclo", color = EvoTextSecondary, fontSize = 10.sp)
                    Text("Una sesión terminada equivale a una semana", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            val positivo = diferencia >= 0
            Text(
                text = "${if (positivo) "+" else ""}$diferencia series",
                color = if (positivo) EvoGreen else Color(0xFFFF8A80),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        if (positivo) EvoGreen.copy(alpha = 0.15f) else Color(0xFFFF8A80).copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            series.forEachIndexed { indice, total ->
                val fecha = fechas.getOrNull(indice).orEmpty()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(EvoNavBar, RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Semana ${indice + 1}", color = EvoTextSecondary, fontSize = 9.sp)
                    Text(if (fecha.isBlank()) "—" else "$total", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(if (fecha.isBlank()) "Pendiente" else fecha, color = EvoTextSecondary, fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
private fun TarjetaEjercicio(ejercicio: EjercicioMesUi, unidad: UnidadPeso) {
    val sugerir = ejercicio.pesoSugeridoKg != null && ejercicio.repsSugeridas != null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EvoCard)
            .border(1.dp, if (sugerir) Alerta else EvoBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(ejercicio.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (sugerir) {
                        Text(
                            "Subir peso",
                            color = Alerta,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Alerta.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    "Última semana: ${textoPeso(ejercicio.pesoActualKg, unidad)} × ${ejercicio.repsActuales} reps (${ejercicio.seriesActuales} series)",
                    color = EvoTextSecondary,
                    fontSize = 11.sp
                )
                Text("Peso: promedio. Repeticiones: las mejores de la semana.", color = EvoTextSecondary, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Mejores reps", color = EvoTextSecondary, fontSize = 9.sp)
                Text(
                    ejercicio.deltaReps?.let { delta -> "${if (delta >= 0) "+" else ""}$delta" } ?: "—",
                    color = EvoGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
        if (sugerir) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.horizontalGradient(listOf(Alerta.copy(alpha = 0.25f), Color.Transparent)))
                    .border(1.dp, Alerta.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Alerta, modifier = Modifier.size(14.dp))
                    Text("Sugerencia de carga", color = Alerta, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Las series de trabajo llegaron a ${ejercicio.repsSugeridas} repeticiones manteniendo ${textoPeso(ejercicio.pesoSugeridoKg ?: 0.0, unidad)}. Puedes incrementar el peso en la próxima sesión. El registro no se modifica solo.",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
        GraficaSemanas(ejercicio.semanas, unidad, sugerir)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            ejercicio.semanas.forEachIndexed { indice, semana ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(EvoNavBar, RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("S${indice + 1}", color = EvoTextSecondary, fontSize = 9.sp)
                    if (semana.tieneDatos) {
                        Text(textoPeso(semana.pesoKg, unidad), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("${semana.mejoresReps} reps", color = if (sugerir && indice == ejercicio.semanas.indexOfLast { it.tieneDatos }) Alerta else EvoGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("—", color = EvoTextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun GraficaSemanas(semanas: List<SemanaEjercicioUi>, unidad: UnidadPeso, sugerir: Boolean) {
    val maxReps = semanas.maxOf { it.mejoresReps }.coerceAtLeast(1)
    val ultima = semanas.indexOfLast { it.tieneDatos }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(EvoNavBar, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Barras: mejores repeticiones", color = EvoTextSecondary, fontSize = 9.sp)
            Text("Número: peso promedio ${unidad.etiqueta}", color = LineaPeso, fontSize = 9.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            semanas.forEachIndexed { indice, semana ->
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (semana.tieneDatos) {
                        Text(textoPeso(semana.pesoKg, unidad).substringBefore(' '), color = LineaPeso, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        val alto = (semana.mejoresReps.toFloat() / maxReps.toFloat()).coerceIn(0.12f, 1f)
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(width = 18.dp, height = (52 * alto).dp)
                                .background(
                                    if (sugerir && indice == ultima) Alerta else EvoGreen,
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Text("${semana.mejoresReps}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("—", color = EvoTextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Aviso(texto: String) {
    Text(
        texto,
        color = EvoTextSecondary,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EvoCard)
            .border(1.dp, EvoBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    )
}

@Composable
private fun PieSobrecarga() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EvoBackground)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = EvoGreen, modifier = Modifier.size(16.dp))
            Text("Principio de sobrecarga", color = Color.White, fontSize = 12.sp)
        }
        Text("Al tope de reps, subir peso", color = EvoTextSecondary, fontSize = 11.sp)
    }
}

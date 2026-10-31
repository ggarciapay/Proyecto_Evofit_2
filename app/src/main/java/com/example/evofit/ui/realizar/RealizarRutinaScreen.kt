package com.example.evofit.ui.realizar

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evofit.data.EjercicioParaGuardar
import com.example.evofit.data.EntrenamientoParaGuardar
import com.example.evofit.data.ResultadoGuardado
import com.example.evofit.data.SerieParaGuardar
import com.example.evofit.data.catalogo.DiaGuardado
import com.example.evofit.data.catalogo.EjercicioGuardado
import com.example.evofit.data.catalogo.Intensidad
import com.example.evofit.data.catalogo.RutinaGuardada
import com.example.evofit.data.catalogo.UnidadPeso
import com.example.evofit.data.repositorioRutinas
import com.example.evofit.ui.comun.BarraPantalla
import com.example.evofit.ui.comun.BotonVerde
import com.example.evofit.ui.comun.FondoEvo
import com.example.evofit.ui.comun.coloresCampo
import com.example.evofit.ui.comun.tarjetaEvo
import com.example.evofit.ui.theme.EvoBorder
import com.example.evofit.ui.theme.EvoCard
import com.example.evofit.ui.theme.EvoError
import com.example.evofit.ui.theme.EvoGreen
import com.example.evofit.ui.theme.EvoOnGreen
import com.example.evofit.ui.theme.EvoTextSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SerieCurso(
    val numero: Int,
    val peso: String = "0",
    val repeticiones: String = "0",
    val intensidad: Intensidad = Intensidad.EFECTIVA,
    val completada: Boolean = false
)

data class EjercicioCurso(
    val nombre: String,
    val grupo: String,
    val orden: Int,
    val repeticionesObjetivo: String,
    val descansoSegundos: Int,
    val series: List<SerieCurso>
)

data class EstadoRealizar(
    val rutinas: List<RutinaGuardada> = emptyList(),
    val rutinaId: Long? = null,
    val diaId: Long? = null,
    val unidad: UnidadPeso = UnidadPeso.KG,
    val ejercicios: List<EjercicioCurso> = emptyList(),
    val inicioMillis: Long? = null,
    val descansoRestante: Int = 0,
    val descansoTotal: Int = 0,
    val descansoActivo: Boolean = false,
    val descansoPausado: Boolean = false,
    val avisoDescanso: Boolean = false,
    val confirmar: Boolean = false,
    val mensaje: String? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false
)

class RealizarRutinaViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = repositorioRutinas(application)
    private val _estado = MutableStateFlow(EstadoRealizar())
    val estado: StateFlow<EstadoRealizar> = _estado.asStateFlow()
    private var trabajoDescanso: Job? = null
    private val tono = runCatching {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
    }.getOrNull()

    init {
        viewModelScope.launch {
            repositorio.observar().collect { rutinas ->
                _estado.update { actual ->
                    val rutinaSigue = rutinas.any { it.id == actual.rutinaId }
                    if (rutinaSigue) actual.copy(rutinas = rutinas) else actual.copy(rutinas = rutinas)
                }
            }
        }
    }

    fun seleccionarRutina(id: Long) {
        val rutina = _estado.value.rutinas.find { it.id == id } ?: return
        val dia = rutina.dias.firstOrNull { !it.esDescanso }
        aplicarDia(rutina, dia)
    }

    fun seleccionarDia(diaId: Long) {
        val rutina = rutinaActual() ?: return
        aplicarDia(rutina, rutina.dias.find { it.id == diaId })
    }

    fun cambiarUnidad(unidad: UnidadPeso) {
        _estado.update { it.copy(unidad = unidad) }
    }

    fun cambiarPeso(ejercicio: Int, serie: Int, valor: String) {
        val limpio = valor.replace(',', '.').filter { it.isDigit() || it == '.' }
        val partes = limpio.split('.')
        val texto = if (partes.size <= 2) limpio else partes.first() + "." + partes.drop(1).joinToString("")
        editarSerie(ejercicio, serie) { it.copy(peso = texto) }
    }

    fun cambiarRepeticiones(ejercicio: Int, serie: Int, valor: String) {
        editarSerie(ejercicio, serie) { it.copy(repeticiones = valor.filter(Char::isDigit).take(3)) }
    }

    fun cambiarIntensidad(ejercicio: Int, serie: Int, intensidad: Intensidad) {
        editarSerie(ejercicio, serie) { it.copy(intensidad = intensidad) }
    }

    fun marcarSerie(ejercicio: Int, serie: Int, completada: Boolean) {
        val descanso = _estado.value.ejercicios.getOrNull(ejercicio)?.descansoSegundos ?: 0
        editarSerie(ejercicio, serie) { it.copy(completada = completada) }
        if (completada && descanso > 0) iniciarDescanso(descanso)
    }

    fun pausarDescanso() = _estado.update { it.copy(descansoPausado = true) }

    fun reanudarDescanso() = _estado.update { it.copy(descansoPausado = false) }

    fun reiniciarDescanso() {
        _estado.update { it.copy(descansoRestante = it.descansoTotal, descansoPausado = false, avisoDescanso = false) }
    }

    fun saltarDescanso() {
        trabajoDescanso?.cancel()
        _estado.update { it.copy(descansoActivo = false, descansoRestante = 0, descansoPausado = false) }
    }

    fun pedirConfirmacion() {
        val estado = _estado.value
        if (estado.rutinaId == null || estado.diaId == null) {
            _estado.update { it.copy(mensaje = "Selecciona una rutina y un día.") }
            return
        }
        _estado.update { it.copy(confirmar = true, mensaje = null) }
    }

    fun cancelarConfirmacion() = _estado.update { it.copy(confirmar = false) }

    fun finalizar() {
        val estado = _estado.value
        val rutina = rutinaActual() ?: return
        val dia = rutina.dias.find { it.id == estado.diaId } ?: return
        if (estado.guardando) return
        val fin = System.currentTimeMillis()
        val inicio = estado.inicioMillis ?: fin
        _estado.update { it.copy(guardando = true, confirmar = false) }
        viewModelScope.launch {
            val resultado = repositorio.guardarEntrenamiento(
                EntrenamientoParaGuardar(
                    rutinaId = rutina.id,
                    nombreRutina = rutina.nombre,
                    nivel = rutina.nivel,
                    diaSemana = dia.dia.numero,
                    etiquetaDia = "${dia.dia.nombre} – ${dia.etiqueta}",
                    inicioMillis = inicio,
                    finMillis = fin,
                    unidadPeso = estado.unidad.etiqueta,
                    ejercicios = estado.ejercicios.map { ejercicio ->
                        EjercicioParaGuardar(
                            nombre = ejercicio.nombre,
                            grupo = ejercicio.grupo,
                            orden = ejercicio.orden,
                            repeticionesObjetivo = ejercicio.repeticionesObjetivo,
                            descansoSegundos = ejercicio.descansoSegundos,
                            series = ejercicio.series.map { serie ->
                                SerieParaGuardar(
                                    numero = serie.numero,
                                    peso = serie.peso.replace(',', '.').toDoubleOrNull() ?: 0.0,
                                    unidad = estado.unidad.etiqueta,
                                    repeticiones = serie.repeticiones.toIntOrNull() ?: 0,
                                    intensidad = serie.intensidad.etiqueta,
                                    completada = serie.completada
                                )
                            }
                        )
                    }
                )
            )
            when (resultado) {
                ResultadoGuardado.Exito -> {
                    saltarDescanso()
                    _estado.update {
                        it.copy(
                            guardando = false,
                            guardado = true,
                            mensaje = "Entrenamiento guardado.",
                            ejercicios = emptyList(),
                            rutinaId = null,
                            diaId = null,
                            inicioMillis = null
                        )
                    }
                }
                is ResultadoGuardado.Error -> _estado.update { it.copy(guardando = false, mensaje = resultado.mensaje) }
                is ResultadoGuardado.Aviso -> _estado.update { it.copy(guardando = false, mensaje = resultado.mensaje) }
            }
        }
    }

    override fun onCleared() {
        trabajoDescanso?.cancel()
        tono?.release()
    }

    private fun rutinaActual(): RutinaGuardada? {
        val id = _estado.value.rutinaId ?: return null
        return _estado.value.rutinas.find { it.id == id }
    }

    private fun aplicarDia(rutina: RutinaGuardada, dia: DiaGuardado?) {
        saltarDescanso()
        _estado.update {
            it.copy(
                rutinaId = rutina.id,
                diaId = dia?.id,
                ejercicios = dia?.ejercicios?.sortedBy { ejercicio -> ejercicio.orden }?.map(::aCurso).orEmpty(),
                inicioMillis = if (dia == null) null else System.currentTimeMillis(),
                mensaje = null,
                guardado = false,
                avisoDescanso = false
            )
        }
    }

    private fun aCurso(ejercicio: EjercicioGuardado) = EjercicioCurso(
        nombre = ejercicio.nombre,
        grupo = ejercicio.grupo,
        orden = ejercicio.orden,
        repeticionesObjetivo = ejercicio.repeticiones,
        descansoSegundos = ejercicio.descansoSegundos,
        series = (1..ejercicio.series.coerceAtLeast(1)).map { SerieCurso(numero = it) }
    )

    private fun editarSerie(ejercicio: Int, serie: Int, cambio: (SerieCurso) -> SerieCurso) {
        _estado.update { estado ->
            estado.copy(
                ejercicios = estado.ejercicios.mapIndexed { indiceEjercicio, item ->
                    if (indiceEjercicio != ejercicio) item
                    else item.copy(
                        series = item.series.mapIndexed { indiceSerie, actual ->
                            if (indiceSerie == serie) cambio(actual) else actual
                        }
                    )
                }
            )
        }
    }

    private fun iniciarDescanso(segundos: Int) {
        trabajoDescanso?.cancel()
        _estado.update {
            it.copy(
                descansoTotal = segundos,
                descansoRestante = segundos,
                descansoActivo = true,
                descansoPausado = false,
                avisoDescanso = false
            )
        }
        trabajoDescanso = viewModelScope.launch {
            while (true) {
                delay(1000)
                val actual = _estado.value
                if (!actual.descansoActivo) break
                if (actual.descansoPausado) continue
                val siguiente = actual.descansoRestante - 1
                if (siguiente <= 0) {
                    _estado.update { it.copy(descansoRestante = 0, descansoActivo = false, avisoDescanso = true) }
                    tono?.startTone(ToneGenerator.TONE_PROP_BEEP, 400)
                    break
                }
                _estado.update { it.copy(descansoRestante = siguiente) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealizarRutinaScreen(onAtras: () -> Unit, viewModel: RealizarRutinaViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var rutinaAbierta by remember { mutableStateOf(false) }
    var diaAbierto by remember { mutableStateOf(false) }
    LaunchedEffect(estado.inicioMillis) {
        while (estado.inicioMillis != null) {
            delay(1000)
            ahora = System.currentTimeMillis()
        }
    }
    val rutina = estado.rutinas.find { it.id == estado.rutinaId }
    val dias = rutina?.dias?.filter { !it.esDescanso }.orEmpty()
    val dia = dias.find { it.id == estado.diaId }
    val totalSeries = estado.ejercicios.sumOf { it.series.size }
    val hechas = estado.ejercicios.sumOf { ejercicio -> ejercicio.series.count { it.completada } }
    val porcentaje = if (totalSeries == 0) 0 else (hechas * 100) / totalSeries
    val transcurrido = estado.inicioMillis?.let { ((ahora - it) / 1000L).coerceAtLeast(0L) } ?: 0L

    Column(Modifier.fillMaxSize().background(FondoEvo).imePadding()) {
        BarraPantalla(
            titulo = "Realizar Rutina",
            onAtras = onAtras,
            accion = {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, EvoBorder, RoundedCornerShape(50))
                ) {
                    UnidadPeso.entries.forEach { unidad ->
                        val activa = unidad == estado.unidad
                        Text(
                            unidad.etiqueta,
                            color = if (activa) EvoOnGreen else EvoTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(if (activa) EvoGreen else Color.Transparent)
                                .clickable { viewModel.cambiarUnidad(unidad) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        )
        Text(
            "Tiempo: ${formato(transcurrido)}",
            color = EvoGreen,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (estado.rutinas.isEmpty()) {
                Text(
                    "No tienes rutinas guardadas. Agrégalas desde Recomendadas o Crear rutina.",
                    color = EvoTextSecondary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                Selector(
                    titulo = "SELECCIONAR RUTINA",
                    valor = rutina?.nombre ?: "Elige una rutina",
                    abierto = rutinaAbierta,
                    onExpandir = { rutinaAbierta = it }
                ) {
                    estado.rutinas.sortedBy { it.nombre }.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.nombre, color = Color.White) },
                            onClick = {
                                viewModel.seleccionarRutina(item.id)
                                rutinaAbierta = false
                            }
                        )
                    }
                }
                Selector(
                    titulo = "DÍA A REALIZAR",
                    valor = dia?.let { "${it.dia.nombre} – ${it.etiqueta}" } ?: "Elige un día",
                    abierto = diaAbierto,
                    onExpandir = { if (rutina != null) diaAbierto = it }
                ) {
                    dias.forEach { item ->
                        DropdownMenuItem(
                            text = { Text("${item.dia.nombre} – ${item.etiqueta}", color = Color.White) },
                            onClick = {
                                viewModel.seleccionarDia(item.id)
                                diaAbierto = false
                            }
                        )
                    }
                }
                if (dia != null) {
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp).tarjetaEvo().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = EvoGreen)
                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                            Text(dia.dia.nombre, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("$hechas de $totalSeries series completadas", color = EvoTextSecondary, fontSize = 13.sp)
                        }
                        Text("$porcentaje%", color = EvoGreen, fontWeight = FontWeight.Bold)
                    }
                }
                if (estado.descansoActivo || estado.avisoDescanso) {
                    PanelDescanso(estado, viewModel)
                }
                estado.ejercicios.forEachIndexed { indice, ejercicio ->
                    TarjetaEjercicioReal(
                        ejercicio = ejercicio,
                        unidad = estado.unidad,
                        onPeso = { serie, valor -> viewModel.cambiarPeso(indice, serie, valor) },
                        onReps = { serie, valor -> viewModel.cambiarRepeticiones(indice, serie, valor) },
                        onIntensidad = { serie, intensidad -> viewModel.cambiarIntensidad(indice, serie, intensidad) },
                        onCompletar = { serie, valor -> viewModel.marcarSerie(indice, serie, valor) }
                    )
                }
            }
        }
        if (estado.mensaje != null) {
            Text(
                estado.mensaje.orEmpty(),
                color = if (estado.guardado) EvoGreen else EvoError,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        BotonVerde(
            texto = "Finalizar Entrenamiento",
            onClick = viewModel::pedirConfirmacion,
            cargando = estado.guardando,
            habilitado = estado.rutinaId != null && estado.diaId != null
        )
    }
    if (estado.confirmar) {
        val incompletas = totalSeries - hechas
        AlertDialog(
            onDismissRequest = viewModel::cancelarConfirmacion,
            containerColor = EvoCard,
            title = { Text("Finalizar entrenamiento", color = Color.White) },
            text = {
                Text(
                    if (incompletas > 0) {
                        "Se guardará la sesión con $hechas series completadas y $incompletas pendientes."
                    } else {
                        "Se guardará la sesión con todas las series completadas."
                    },
                    color = EvoTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::finalizar) { Text("Guardar", color = EvoGreen) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarConfirmacion) { Text("Cancelar", color = EvoTextSecondary) }
            }
        )
    }
}

@Composable
private fun PanelDescanso(estado: EstadoRealizar, viewModel: RealizarRutinaViewModel) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp).tarjetaEvo().padding(14.dp)) {
        Text(
            if (estado.avisoDescanso) "Descanso terminado" else "Descanso ${formato(estado.descansoRestante.toLong())}",
            color = EvoGreen,
            fontWeight = FontWeight.Bold
        )
        if (estado.descansoActivo) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                AccionDescanso(if (estado.descansoPausado) "Reanudar" else "Pausar") {
                    if (estado.descansoPausado) viewModel.reanudarDescanso() else viewModel.pausarDescanso()
                }
                AccionDescanso("Reiniciar", viewModel::reiniciarDescanso)
                AccionDescanso("Saltar", viewModel::saltarDescanso)
            }
        }
    }
}

@Composable
private fun AccionDescanso(texto: String, onClick: () -> Unit) {
    Text(texto, color = Color.White, modifier = Modifier.clickable(onClick = onClick))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Selector(
    titulo: String,
    valor: String,
    abierto: Boolean,
    onExpandir: (Boolean) -> Unit,
    contenido: @Composable () -> Unit
) {
    Text(titulo, color = EvoTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp))
    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = onExpandir) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(abierto) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = coloresCampo()
        )
        ExposedDropdownMenu(
            expanded = abierto,
            onDismissRequest = { onExpandir(false) },
            modifier = Modifier.heightIn(max = 320.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = EvoCard
        ) {
            contenido()
        }
    }
}

@Composable
private fun TarjetaEjercicioReal(
    ejercicio: EjercicioCurso,
    unidad: UnidadPeso,
    onPeso: (Int, String) -> Unit,
    onReps: (Int, String) -> Unit,
    onIntensidad: (Int, Intensidad) -> Unit,
    onCompletar: (Int, Boolean) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp).tarjetaEvo().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(ejercicio.nombre, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Meta: ${ejercicio.series.size} series × ${ejercicio.repeticionesObjetivo} reps",
                    color = EvoTextSecondary,
                    fontSize = 13.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timer, contentDescription = null, tint = EvoGreen, modifier = Modifier.size(16.dp))
                Text("${ejercicio.descansoSegundos}s", color = EvoGreen, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp)) {
            listOf("SERIE", "PESO (${unidad.etiqueta})", "REPS", "INTENS.", "OK").forEach { titulo ->
                Text(titulo, color = EvoTextSecondary, fontSize = 10.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        ejercicio.series.forEachIndexed { indice, serie ->
            FilaSerie(serie, onPeso = { onPeso(indice, it) }, onReps = { onReps(indice, it) }, onIntensidad = { onIntensidad(indice, it) }, onCompletar = { onCompletar(indice, it) })
        }
    }
}

@Composable
private fun FilaSerie(
    serie: SerieCurso,
    onPeso: (String) -> Unit,
    onReps: (String) -> Unit,
    onIntensidad: (Intensidad) -> Unit,
    onCompletar: (Boolean) -> Unit
) {
    var intensidades by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(serie.numero.toString(), color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        CampoSerie(serie.peso, onPeso, Modifier.weight(1f))
        CampoSerie(serie.repeticiones, onReps, Modifier.weight(1f), KeyboardType.Number)
        Text(
            serie.intensidad.corto,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .clickable { intensidades = true }
        )
        androidx.compose.foundation.layout.Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val marca = if (serie.completada) EvoGreen else Color.Transparent
            Icon(
                Icons.Filled.Check,
                contentDescription = if (serie.completada) "Serie completada" else "Marcar serie",
                tint = if (serie.completada) EvoOnGreen else EvoTextSecondary,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(marca)
                    .border(1.dp, if (serie.completada) EvoGreen else EvoBorder, CircleShape)
                    .clickable { onCompletar(!serie.completada) }
                    .padding(4.dp)
            )
        }
    }
    if (intensidades) {
        AlertDialog(
            onDismissRequest = { intensidades = false },
            containerColor = EvoCard,
            title = { Text("Intensidad", color = Color.White) },
            text = {
                Column {
                    Intensidad.entries.forEach { intensidad ->
                        Text(
                            intensidad.etiqueta,
                            color = if (intensidad == serie.intensidad) EvoGreen else Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onIntensidad(intensidad)
                                    intensidades = false
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun CampoSerie(
    valor: String,
    onValor: (String) -> Unit,
    modifier: Modifier,
    teclado: KeyboardType = KeyboardType.Decimal
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValor,
        modifier = modifier.height(52.dp).padding(horizontal = 2.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(10.dp),
        colors = coloresCampo()
    )
}

private fun formato(segundos: Long): String {
    val minutos = segundos / 60
    val resto = segundos % 60
    return "%02d:%02d".format(minutos, resto)
}

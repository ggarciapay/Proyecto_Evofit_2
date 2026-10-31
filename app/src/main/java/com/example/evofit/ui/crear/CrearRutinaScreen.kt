package com.example.evofit.ui.crear

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.evofit.data.catalogo.CatalogoEjercicios
import com.example.evofit.data.catalogo.descripcionSeries
import com.example.evofit.data.catalogo.DiaPlantilla
import com.example.evofit.data.catalogo.DiaSemana
import com.example.evofit.data.catalogo.EjercicioPlantilla
import com.example.evofit.data.catalogo.GrupoMuscular
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val descansos = listOf(30, 45, 60, 90, 120, 180)

data class EjercicioBorrador(
    val clave: Long,
    val catalogoId: String? = null,
    val nombre: String = "",
    val series: Int = 4,
    val repeticiones: Int = 12,
    val descansoSegundos: Int = 60
)

data class DiaBorrador(
    val grupos: Set<GrupoMuscular> = emptySet(),
    val ejercicios: List<EjercicioBorrador> = emptyList()
)

data class EstadoCrear(
    val nombre: String = "",
    val seleccionados: List<DiaSemana> = emptyList(),
    val activo: DiaSemana? = null,
    val porDia: Map<DiaSemana, DiaBorrador> = emptyMap(),
    val error: String? = null,
    val mensaje: String? = null,
    val guardando: Boolean = false
)

class CrearRutinaViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = repositorioRutinas(application)
    private val _estado = MutableStateFlow(EstadoCrear())
    val estado: StateFlow<EstadoCrear> = _estado.asStateFlow()
    private var siguienteClave = 1L
    private var editandoId: Long? = null

    fun preparar(id: Long?) {
        if (id == null) {
            editandoId = null
            _estado.value = EstadoCrear()
            return
        }
        viewModelScope.launch {
            val rutina = repositorio.obtener(id) ?: return@launch
            editandoId = id
            val dias = rutina.dias.filter { !it.esDescanso }
            _estado.value = EstadoCrear(
                nombre = rutina.nombre,
                seleccionados = dias.map { it.dia },
                activo = dias.firstOrNull()?.dia,
                porDia = dias.associate { dia ->
                    dia.dia to DiaBorrador(
                        grupos = dia.grupos.mapNotNull { GrupoMuscular.desde(it) }.toSet(),
                        ejercicios = dia.ejercicios.sortedBy { it.orden }.map { ejercicio ->
                            EjercicioBorrador(
                                clave = siguienteClave++,
                                catalogoId = ejercicio.catalogoId.ifBlank { null },
                                nombre = ejercicio.nombre,
                                series = ejercicio.series.coerceIn(1, 10),
                                repeticiones = ejercicio.repeticiones.takeWhile { it.isDigit() }.toIntOrNull()?.coerceIn(1, 50) ?: 12,
                                descansoSegundos = ejercicio.descansoSegundos
                            )
                        }
                    )
                }
            )
        }
    }

    fun onNombre(valor: String) = _estado.update { it.copy(nombre = valor, error = null, mensaje = null) }

    fun alternarDia(dia: DiaSemana) {
        _estado.update { estado ->
            val seleccionados = estado.seleccionados.toMutableList()
            val activo = if (dia in seleccionados) {
                seleccionados.remove(dia)
                if (estado.activo == dia) seleccionados.firstOrNull() else estado.activo
            } else {
                seleccionados.add(dia)
                dia
            }
            estado.copy(seleccionados = seleccionados, activo = activo, error = null, mensaje = null)
        }
    }

    fun enfocar(dia: DiaSemana) {
        if (dia in _estado.value.seleccionados) {
            _estado.update { it.copy(activo = dia) }
        }
    }

    fun alternarGrupo(grupo: GrupoMuscular) {
        val dia = _estado.value.activo ?: return
        _estado.update { estado ->
            val actual = estado.porDia[dia] ?: DiaBorrador()
            val grupos = actual.grupos.toMutableSet()
            if (!grupos.add(grupo)) grupos.remove(grupo)
            estado.copy(
                porDia = estado.porDia + (dia to actual.copy(grupos = grupos)),
                error = null
            )
        }
    }

    fun agregarEjercicio() {
        val dia = _estado.value.activo ?: return
        val actual = _estado.value.porDia[dia] ?: DiaBorrador()
        if (actual.grupos.isEmpty()) {
            _estado.update { it.copy(error = "Selecciona al menos un grupo muscular para este día.") }
            return
        }
        val nuevo = EjercicioBorrador(clave = siguienteClave++)
        _estado.update {
            it.copy(
                porDia = it.porDia + (dia to actual.copy(ejercicios = actual.ejercicios + nuevo)),
                error = null
            )
        }
    }

    fun eliminar(clave: Long) = cambiarLista(clave) { lista, indice -> lista.removeAt(indice) }

    fun mover(clave: Long, subir: Boolean) = cambiarLista(clave) { lista, indice ->
        val destino = if (subir) indice - 1 else indice + 1
        if (destino in lista.indices) {
            val item = lista.removeAt(indice)
            lista.add(destino, item)
        }
    }

    fun cambiarEjercicio(clave: Long, catalogoId: String) {
        val catalogo = CatalogoEjercicios.porId(catalogoId)
        cambiarItem(clave) { it.copy(catalogoId = catalogoId, nombre = catalogo?.nombre.orEmpty()) }
    }

    fun cambiarSeries(clave: Long, delta: Int) = cambiarItem(clave) {
        it.copy(series = (it.series + delta).coerceIn(1, 10))
    }

    fun cambiarRepeticiones(clave: Long, delta: Int) = cambiarItem(clave) {
        it.copy(repeticiones = (it.repeticiones + delta).coerceIn(1, 50))
    }

    fun cambiarDescanso(clave: Long, segundos: Int) = cambiarItem(clave) { it.copy(descansoSegundos = segundos) }

    fun limpiar() {
        editandoId = null
        _estado.value = EstadoCrear()
    }

    fun guardar() {
        val estado = _estado.value
        if (estado.guardando) return
        val nombre = estado.nombre.trim()
        val error = validar(estado, nombre)
        if (error != null) {
            _estado.update { it.copy(error = error, mensaje = null) }
            return
        }
        val dias = estado.seleccionados.sortedBy { it.numero }.map { dia ->
            val borrador = estado.porDia[dia] ?: DiaBorrador()
            DiaPlantilla(
                dia = dia,
                etiqueta = borrador.grupos.joinToString(", ") { it.etiqueta }.ifBlank { dia.nombre },
                grupos = borrador.grupos.toList(),
                ejercicios = borrador.ejercicios.map { ejercicio ->
                    val catalogo = CatalogoEjercicios.porId(ejercicio.catalogoId.orEmpty())
                    EjercicioPlantilla(
                        nombre = catalogo?.nombre ?: ejercicio.nombre,
                        series = ejercicio.series,
                        repeticiones = ejercicio.repeticiones.toString(),
                        descansoSegundos = ejercicio.descansoSegundos,
                        catalogoId = catalogo?.id.orEmpty(),
                        grupo = catalogo?.grupo?.etiqueta ?: ""
                    )
                }
            )
        }
        val id = editandoId
        _estado.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            when (val resultado = repositorio.guardarPersonalizada(nombre, "", dias, id)) {
                ResultadoGuardado.Exito -> {
                    if (id == null) {
                        editandoId = null
                        _estado.value = EstadoCrear(mensaje = "Rutina guardada.")
                    } else {
                        _estado.update { it.copy(guardando = false, error = null, mensaje = "Rutina actualizada.") }
                    }
                }
                is ResultadoGuardado.Aviso -> _estado.update { it.copy(guardando = false, error = resultado.mensaje) }
                is ResultadoGuardado.Error -> _estado.update { it.copy(guardando = false, error = resultado.mensaje) }
            }
        }
    }

    private fun validar(estado: EstadoCrear, nombre: String): String? {
        if (nombre.isEmpty()) return "Escribe el nombre de la rutina."
        if (estado.seleccionados.isEmpty()) return "Selecciona al menos un día."
        estado.seleccionados.forEach { dia ->
            val borrador = estado.porDia[dia] ?: DiaBorrador()
            if (borrador.ejercicios.isEmpty()) {
                return "Agrega al menos un ejercicio en ${dia.nombre}."
            }
            if (borrador.ejercicios.any { it.catalogoId == null }) {
                return "Selecciona el ejercicio de cada tarjeta en ${dia.nombre}."
            }
        }
        return null
    }

    private fun cambiarItem(clave: Long, cambio: (EjercicioBorrador) -> EjercicioBorrador) {
        val dia = _estado.value.activo ?: return
        val actual = _estado.value.porDia[dia] ?: return
        _estado.update {
            it.copy(
                porDia = it.porDia + (dia to actual.copy(
                    ejercicios = actual.ejercicios.map { ejercicio ->
                        if (ejercicio.clave == clave) cambio(ejercicio) else ejercicio
                    }
                ))
            )
        }
    }

    private fun cambiarLista(clave: Long, cambio: (MutableList<EjercicioBorrador>, Int) -> Unit) {
        val dia = _estado.value.activo ?: return
        val actual = _estado.value.porDia[dia] ?: return
        val lista = actual.ejercicios.toMutableList()
        val indice = lista.indexOfFirst { it.clave == clave }
        if (indice < 0) return
        cambio(lista, indice)
        _estado.update { it.copy(porDia = it.porDia + (dia to actual.copy(ejercicios = lista))) }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CrearRutinaScreen(
    onAtras: () -> Unit,
    edicionToken: Long,
    rutinaId: Long?,
    viewModel: CrearRutinaViewModel = viewModel()
) {
    LaunchedEffect(edicionToken) { viewModel.preparar(rutinaId) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val dia = estado.activo
    val borrador = if (dia == null) DiaBorrador() else estado.porDia[dia] ?: DiaBorrador()
    val opciones = CatalogoEjercicios.deGrupos(borrador.grupos)

    Column(Modifier.fillMaxSize().background(FondoEvo).imePadding()) {
        BarraPantalla(
            titulo = if (rutinaId == null) "Crear rutina" else "Editar rutina",
            onAtras = onAtras,
            accion = {
                Text(
                    "Limpiar",
                    color = EvoGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = viewModel::limpiar)
                )
            }
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Etiqueta("NOMBRE DE LA RUTINA")
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = viewModel::onNombre,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: Hipertrofia Pecho & Tríceps") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = coloresCampo()
            )
            Etiqueta("SELECCIONAR DÍA(S)")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DiaSemana.entries.forEach { item ->
                    val seleccionado = item in estado.seleccionados
                    val activo = item == estado.activo
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (seleccionado) EvoGreen else Color.Transparent)
                                .border(1.5.dp, if (seleccionado) EvoGreen else EvoBorder, CircleShape)
                                .clickable { viewModel.alternarDia(item) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(item.letra, color = if (seleccionado) EvoOnGreen else Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            item.corto,
                            color = if (activo) EvoGreen else EvoTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            if (estado.seleccionados.size > 1) {
                Text("Editando", color = EvoTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    estado.seleccionados.forEach { item ->
                        val activo = item == estado.activo
                        Text(
                            item.nombre,
                            color = if (activo) EvoOnGreen else Color.White,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (activo) EvoGreen else Color.Transparent)
                                .border(1.dp, if (activo) EvoGreen else EvoBorder, RoundedCornerShape(50))
                                .clickable { viewModel.enfocar(item) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Etiqueta("GRUPOS MUSCULARES")
            if (dia == null) {
                Text("Selecciona un día para configurar sus grupos y ejercicios.", color = EvoTextSecondary)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GrupoMuscular.entries.forEach { grupo ->
                        val activo = grupo in borrador.grupos
                        Text(
                            grupo.etiqueta,
                            color = if (activo) EvoOnGreen else Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (activo) EvoGreen else Color.Transparent)
                                .border(1.dp, if (activo) EvoGreen else EvoBorder, RoundedCornerShape(50))
                                .clickable { viewModel.alternarGrupo(grupo) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("EJERCICIOS Y CONFIGURACIÓN", color = EvoTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    val cantidad = borrador.ejercicios.size
                    Text(
                        if (cantidad == 1) "1 Ejercicio" else "$cantidad Ejercicios",
                        color = EvoGreen,
                        fontSize = 13.sp
                    )
                }
                borrador.ejercicios.forEachIndexed { indice, ejercicio ->
                    TarjetaEjercicio(
                        numero = indice + 1,
                        ejercicio = ejercicio,
                        opciones = opciones,
                        onElegir = { viewModel.cambiarEjercicio(ejercicio.clave, it) },
                        onSeries = { viewModel.cambiarSeries(ejercicio.clave, it) },
                        onReps = { viewModel.cambiarRepeticiones(ejercicio.clave, it) },
                        onDescanso = { viewModel.cambiarDescanso(ejercicio.clave, it) },
                        onSubir = { viewModel.mover(ejercicio.clave, true) },
                        onBajar = { viewModel.mover(ejercicio.clave, false) },
                        onEliminar = { viewModel.eliminar(ejercicio.clave) }
                    )
                }
                Text(
                    "Añadir otro ejercicio",
                    color = EvoGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(onClick = viewModel::agregarEjercicio)
                        .padding(vertical = 12.dp)
                )
            }
            if (estado.seleccionados.isNotEmpty()) {
                Etiqueta("VISTA SEMANAL")
                estado.seleccionados.sortedBy { it.numero }.forEach { item ->
                    val datos = estado.porDia[item] ?: DiaBorrador()
                    val etiqueta = datos.grupos.joinToString(", ") { it.etiqueta }.ifBlank { "Sin grupos" }
                    val enfocado = item == estado.activo
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(EvoCard)
                            .border(1.dp, if (enfocado) EvoGreen else EvoBorder, RoundedCornerShape(14.dp))
                            .clickable { viewModel.enfocar(item) }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.nombre, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(etiqueta, color = EvoGreen, fontSize = 13.sp)
                    }
                }
                val diaVista = estado.activo
                val detalle = if (diaVista == null) null else estado.porDia[diaVista]
                if (diaVista != null && detalle != null) {
                    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp).tarjetaEvo().padding(14.dp)) {
                        Text(
                            "${diaVista.nombre} – ${detalle.grupos.joinToString(", ") { it.etiqueta }.ifBlank { "Sin grupos" }}",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (detalle.ejercicios.isEmpty()) {
                            Text("Agrega ejercicios para este día.", color = EvoTextSecondary, modifier = Modifier.padding(top = 8.dp))
                        } else {
                            detalle.ejercicios.forEachIndexed { indice, ejercicio ->
                                val titulo = CatalogoEjercicios.porId(ejercicio.catalogoId.orEmpty())?.nombre
                                    ?: ejercicio.nombre.ifBlank { "Ejercicio sin elegir" }
                                Text(
                                    "${indice + 1}. $titulo",
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                                Text(
                                    descripcionSeries(ejercicio.series, ejercicio.repeticiones.toString()),
                                    color = EvoTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        val aviso = estado.error ?: estado.mensaje
        if (aviso != null) {
            Text(
                aviso,
                color = if (estado.error != null) EvoError else EvoGreen,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        BotonVerde(
            texto = if (rutinaId == null) "Guardar Rutina" else "Guardar cambios",
            onClick = viewModel::guardar,
            cargando = estado.guardando,
            icono = { Icon(Icons.Filled.Save, contentDescription = null, tint = EvoOnGreen) }
        )
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        texto,
        color = EvoTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaEjercicio(
    numero: Int,
    ejercicio: EjercicioBorrador,
    opciones: List<com.example.evofit.data.catalogo.EjercicioCatalogo>,
    onElegir: (String) -> Unit,
    onSeries: (Int) -> Unit,
    onReps: (Int) -> Unit,
    onDescanso: (Int) -> Unit,
    onSubir: () -> Unit,
    onBajar: () -> Unit,
    onEliminar: () -> Unit
) {
    var abierto by rememberSaveable(ejercicio.clave) { mutableStateOf(false) }
    var descansoAbierto by rememberSaveable(ejercicio.clave) { mutableStateOf(false) }
    val nombre = CatalogoEjercicios.porId(ejercicio.catalogoId.orEmpty())?.nombre ?: ejercicio.nombre
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp).tarjetaEvo().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                numero.toString(),
                color = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .border(1.dp, EvoBorder, CircleShape)
                    .padding(top = 4.dp),
                fontWeight = FontWeight.Bold
            )
            ExposedDropdownMenuBox(
                expanded = abierto,
                onExpandedChange = { abierto = it },
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Seleccionar ejercicio") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(abierto) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = coloresCampo()
                )
                ExposedDropdownMenu(
                    expanded = abierto,
                    onDismissRequest = { abierto = false },
                    modifier = Modifier.heightIn(max = 360.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = EvoCard
                ) {
                    if (opciones.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Elige un grupo muscular", color = EvoTextSecondary) },
                            onClick = { abierto = false }
                        )
                    } else {
                        val ordenadas = opciones.sortedWith(compareBy({ it.grupo.ordinal }, { it.nombre }))
                        var grupoPrevio: GrupoMuscular? = null
                        ordenadas.forEach { opcion ->
                            if (opcion.grupo != grupoPrevio) {
                                Text(
                                    opcion.grupo.etiqueta.uppercase(),
                                    color = EvoGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 2.dp)
                                )
                                grupoPrevio = opcion.grupo
                            }
                            DropdownMenuItem(
                                text = { Text(opcion.nombre, color = Color.White) },
                                onClick = {
                                    onElegir(opcion.id)
                                    abierto = false
                                },
                                colors = MenuDefaults.itemColors(textColor = Color.White)
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onSubir) { Icon(Icons.Filled.ArrowUpward, "Subir", tint = EvoTextSecondary) }
            IconButton(onClick = onBajar) { Icon(Icons.Filled.ArrowDownward, "Bajar", tint = EvoTextSecondary) }
            IconButton(onClick = onEliminar) { Icon(Icons.Filled.Delete, "Eliminar", tint = EvoTextSecondary) }
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ControlNumero("SERIES", ejercicio.series.toString(), { onSeries(-1) }, { onSeries(1) }, Modifier.weight(1f))
            ControlNumero("REPS", ejercicio.repeticiones.toString(), { onReps(-1) }, { onReps(1) }, Modifier.weight(1f))
            Column(Modifier.weight(1f)) {
                Text("DESCANSO", color = EvoTextSecondary, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                ExposedDropdownMenuBox(expanded = descansoAbierto, onExpandedChange = { descansoAbierto = it }) {
                    Text(
                        "${ejercicio.descansoSegundos} seg",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .padding(top = 8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = descansoAbierto,
                        onDismissRequest = { descansoAbierto = false },
                        containerColor = EvoCard
                    ) {
                        descansos.forEach { segundos ->
                            DropdownMenuItem(
                                text = { Text("$segundos seg", color = Color.White) },
                                onClick = {
                                    onDescanso(segundos)
                                    descansoAbierto = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlNumero(
    titulo: String,
    valor: String,
    onMenos: () -> Unit,
    onMas: () -> Unit,
    modifier: Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(titulo, color = EvoTextSecondary, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
            Paso("-", onMenos)
            Text(valor, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp))
            Paso("+", onMas)
        }
    }
}

@Composable
private fun Paso(texto: String, onClick: () -> Unit) {
    Text(
        texto,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(EvoBorder)
            .clickable(onClick = onClick)
            .padding(top = 3.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

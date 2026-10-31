package com.example.evofit.data

import android.app.Application
import androidx.room.withTransaction
import com.example.evofit.data.catalogo.CatalogoEjercicios
import com.example.evofit.data.catalogo.DiaGuardado
import com.example.evofit.data.catalogo.DiaPlantilla
import com.example.evofit.data.catalogo.DiaSemana
import com.example.evofit.data.catalogo.EjercicioGuardado
import com.example.evofit.data.catalogo.OrigenRutina
import com.example.evofit.data.catalogo.RutinaGuardada
import com.example.evofit.data.catalogo.RutinaPlantilla
import com.example.evofit.data.local.DiaGrupo
import com.example.evofit.data.local.DiaRutina
import com.example.evofit.data.local.EjercicioRealizado
import com.example.evofit.data.local.FilaSerieResumen
import com.example.evofit.data.local.EjercicioRutina
import com.example.evofit.data.local.Entrenamiento
import com.example.evofit.data.local.EntrenamientoDao
import com.example.evofit.data.local.EvoFitDatabase
import com.example.evofit.data.local.Rutina
import com.example.evofit.data.local.RutinaCompleta
import com.example.evofit.data.local.RutinaDao
import com.example.evofit.data.local.SerieConfigurada
import com.example.evofit.data.local.SerieRealizada
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface ResultadoGuardado {
    data object Exito : ResultadoGuardado
    data class Aviso(val mensaje: String) : ResultadoGuardado
    data class Error(val mensaje: String) : ResultadoGuardado
}

data class SerieParaGuardar(
    val numero: Int,
    val peso: Double,
    val unidad: String,
    val repeticiones: Int,
    val intensidad: String,
    val completada: Boolean
)

data class EjercicioParaGuardar(
    val nombre: String,
    val grupo: String,
    val orden: Int,
    val repeticionesObjetivo: String,
    val descansoSegundos: Int,
    val series: List<SerieParaGuardar>
)

data class EntrenamientoParaGuardar(
    val rutinaId: Long?,
    val nombreRutina: String,
    val nivel: String,
    val diaSemana: Int,
    val etiquetaDia: String,
    val inicioMillis: Long,
    val finMillis: Long,
    val unidadPeso: String,
    val ejercicios: List<EjercicioParaGuardar>
)

fun repositorioRutinas(application: Application): RutinaRepository {
    val base = EvoFitDatabase.obtener(application)
    return RutinaRepository(base, base.rutinaDao(), base.entrenamientoDao())
}

class RutinaRepository(
    private val base: EvoFitDatabase,
    private val rutinas: RutinaDao,
    private val entrenamientos: EntrenamientoDao
) {
    fun observar(): Flow<List<RutinaGuardada>> = rutinas.observar().map { lista ->
        lista.map { it.aGuardada() }
    }

    fun observarSeriesCompletadas(): Flow<List<FilaSerieResumen>> =
        entrenamientos.observarSeriesCompletadas()

    suspend fun copiarRecomendada(plantilla: RutinaPlantilla): ResultadoGuardado {
        if (rutinas.existe(OrigenRutina.RECOMENDADA, plantilla.nivel, plantilla.nombre, -1L)) {
            return ResultadoGuardado.Aviso("Esta rutina ya está en Mis rutinas.")
        }
        return insertar(
            nombre = plantilla.nombre,
            nivel = plantilla.nivel,
            objetivo = plantilla.objetivo,
            recomendaciones = plantilla.recomendaciones,
            origen = OrigenRutina.RECOMENDADA,
            dias = plantilla.dias
        )
    }

    suspend fun obtener(id: Long): RutinaGuardada? = rutinas.obtener(id)?.aGuardada()

    suspend fun eliminar(id: Long) {
        rutinas.eliminar(id)
    }

    suspend fun guardarPersonalizada(
        nombre: String,
        objetivo: String,
        dias: List<DiaPlantilla>,
        id: Long? = null
    ): ResultadoGuardado {
        if (id != null) return actualizar(id, nombre, objetivo, dias)
        if (rutinas.existe(OrigenRutina.PERSONALIZADA, "Personalizada", nombre, -1L)) {
            return ResultadoGuardado.Aviso("Ya existe una rutina con ese nombre.")
        }
        return insertar(
            nombre = nombre,
            nivel = "Personalizada",
            objetivo = objetivo,
            recomendaciones = "",
            origen = OrigenRutina.PERSONALIZADA,
            dias = dias
        )
    }

    private suspend fun actualizar(
        id: Long,
        nombre: String,
        objetivo: String,
        dias: List<DiaPlantilla>
    ): ResultadoGuardado {
        val actual = rutinas.obtener(id) ?: return ResultadoGuardado.Error("No se encontró la rutina.")
        if (rutinas.existe(actual.rutina.origen, actual.rutina.nivel, nombre, id)) {
            return ResultadoGuardado.Aviso("Ya existe una rutina con ese nombre.")
        }
        return try {
            base.withTransaction {
                rutinas.actualizarDatos(id, nombre, objetivo)
                rutinas.eliminarDias(id)
                insertarDias(id, dias)
            }
            ResultadoGuardado.Exito
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            ResultadoGuardado.Error("No se pudo actualizar la rutina.")
        }
    }

    suspend fun guardarEntrenamiento(datos: EntrenamientoParaGuardar): ResultadoGuardado {
        return try {
            base.withTransaction {
                val entrenamientoId = entrenamientos.insertar(
                    Entrenamiento(
                        rutinaId = datos.rutinaId,
                        nombreRutina = datos.nombreRutina,
                        nivel = datos.nivel,
                        diaSemana = datos.diaSemana,
                        etiquetaDia = datos.etiquetaDia,
                        fecha = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date(datos.inicioMillis)),
                        inicioMillis = datos.inicioMillis,
                        finMillis = datos.finMillis,
                        duracionSegundos = ((datos.finMillis - datos.inicioMillis) / 1000L).coerceAtLeast(0L),
                        unidadPeso = datos.unidadPeso
                    )
                )
                datos.ejercicios.forEach { ejercicio ->
                    val ejercicioId = entrenamientos.insertarEjercicio(
                        EjercicioRealizado(
                            entrenamientoId = entrenamientoId,
                            nombre = ejercicio.nombre,
                            grupo = ejercicio.grupo,
                            orden = ejercicio.orden,
                            repeticionesObjetivo = ejercicio.repeticionesObjetivo,
                            descansoSegundos = ejercicio.descansoSegundos,
                            seriesObjetivo = ejercicio.series.size
                        )
                    )
                    entrenamientos.insertarSeries(
                        ejercicio.series.map { serie ->
                            SerieRealizada(
                                ejercicioRealizadoId = ejercicioId,
                                numero = serie.numero,
                                peso = serie.peso,
                                unidad = serie.unidad,
                                repeticiones = serie.repeticiones,
                                intensidad = serie.intensidad,
                                completada = serie.completada
                            )
                        }
                    )
                }
            }
            ResultadoGuardado.Exito
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            ResultadoGuardado.Error("No se pudo guardar el entrenamiento.")
        }
    }

    private suspend fun insertar(
        nombre: String,
        nivel: String,
        objetivo: String,
        recomendaciones: String,
        origen: String,
        dias: List<DiaPlantilla>
    ): ResultadoGuardado {
        return try {
            base.withTransaction {
                val rutinaId = rutinas.insertarRutina(
                    Rutina(
                        nombre = nombre,
                        nivel = nivel,
                        objetivo = objetivo,
                        recomendaciones = recomendaciones,
                        origen = origen,
                        creadaEn = System.currentTimeMillis()
                    )
                )
                insertarDias(rutinaId, dias)
            }
            ResultadoGuardado.Exito
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            ResultadoGuardado.Error("No se pudo guardar la rutina.")
        }
    }

    private suspend fun insertarDias(rutinaId: Long, dias: List<DiaPlantilla>) {
        dias.forEach { dia ->
            val diaId = rutinas.insertarDia(
                DiaRutina(
                    rutinaId = rutinaId,
                    diaSemana = dia.dia.numero,
                    etiqueta = dia.etiqueta,
                    esDescanso = dia.esDescanso
                )
            )
            if (dia.grupos.isNotEmpty()) {
                rutinas.insertarGrupos(dia.grupos.map { DiaGrupo(diaId, it.etiqueta) })
            }
            dia.ejercicios.forEachIndexed { indice, ejercicio ->
                val catalogo = CatalogoEjercicios.porId(ejercicio.catalogoId)
                val ejercicioId = rutinas.insertarEjercicio(
                    EjercicioRutina(
                        diaRutinaId = diaId,
                        catalogoId = catalogo?.id ?: ejercicio.catalogoId,
                        nombre = ejercicio.nombre.ifBlank { catalogo?.nombre ?: "Ejercicio" },
                        grupo = ejercicio.grupo.ifBlank { catalogo?.grupo?.etiqueta.orEmpty() },
                        orden = indice,
                        repeticiones = ejercicio.repeticiones,
                        descansoSegundos = ejercicio.descansoSegundos
                    )
                )
                if (ejercicio.series > 0) {
                    rutinas.insertarSeries(
                        (1..ejercicio.series).map { numero ->
                            SerieConfigurada(
                                ejercicioRutinaId = ejercicioId,
                                numero = numero,
                                repeticiones = ejercicio.repeticiones
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun RutinaCompleta.aGuardada(): RutinaGuardada {
    return RutinaGuardada(
        id = rutina.id,
        nombre = rutina.nombre,
        nivel = rutina.nivel,
        objetivo = rutina.objetivo,
        recomendaciones = rutina.recomendaciones,
        origen = rutina.origen,
        dias = dias.map { detalle ->
            DiaGuardado(
                id = detalle.dia.id,
                dia = DiaSemana.desde(detalle.dia.diaSemana),
                etiqueta = detalle.dia.etiqueta,
                esDescanso = detalle.dia.esDescanso,
                grupos = detalle.grupos.map { it.grupo },
                ejercicios = detalle.ejercicios
                    .sortedBy { it.ejercicio.orden }
                    .map { item ->
                        EjercicioGuardado(
                            nombre = item.ejercicio.nombre,
                            grupo = item.ejercicio.grupo,
                            orden = item.ejercicio.orden,
                            repeticiones = item.ejercicio.repeticiones,
                            descansoSegundos = item.ejercicio.descansoSegundos,
                            series = item.series.size,
                            catalogoId = item.ejercicio.catalogoId
                        )
                    }
            )
        }.sortedBy { it.dia.numero }
    )
}

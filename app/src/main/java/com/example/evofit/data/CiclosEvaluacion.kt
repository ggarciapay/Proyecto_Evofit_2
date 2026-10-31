package com.example.evofit.data

import com.example.evofit.data.local.FilaSerieResumen
import java.util.Locale

const val SEMANAS_POR_CICLO = 4

data class SesionCiclo(
    val entrenamientoId: Long,
    val inicioMillis: Long,
    val fecha: String,
    val nombreRutina: String,
    val rutinaId: Long?,
    val diaSemana: Int,
    val etiquetaDia: String,
    val ciclo: Int,
    val semana: Int,
    val filas: List<FilaSerieResumen>
)

fun sesionesEvaluadas(filas: List<FilaSerieResumen>): List<SesionCiclo> {
    return filas.groupBy { claveCiclo(it) }.values.flatMap { delDia ->
        delDia.groupBy { it.entrenamientoId }
            .values
            .sortedBy { it.first().inicioMillis }
            .mapIndexed { indice, grupo ->
                val primera = grupo.first()
                SesionCiclo(
                    entrenamientoId = primera.entrenamientoId,
                    inicioMillis = primera.inicioMillis,
                    fecha = primera.fecha,
                    nombreRutina = primera.nombreRutina,
                    rutinaId = primera.rutinaId,
                    diaSemana = primera.diaSemana,
                    etiquetaDia = primera.etiquetaDia,
                    ciclo = indice / SEMANAS_POR_CICLO + 1,
                    semana = indice % SEMANAS_POR_CICLO + 1,
                    filas = grupo
                )
            }
    }
}

private fun claveCiclo(fila: FilaSerieResumen): String {
    val rutina = fila.rutinaId?.toString() ?: "nombre:${fila.nombreRutina.lowercase(Locale.ROOT)}"
    return "$rutina|${fila.diaSemana}"
}

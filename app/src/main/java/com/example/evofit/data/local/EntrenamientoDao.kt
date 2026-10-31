package com.example.evofit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EntrenamientoDao {
    @Insert
    suspend fun insertar(entrenamiento: Entrenamiento): Long

    @Insert
    suspend fun insertarEjercicio(ejercicio: EjercicioRealizado): Long

    @Insert
    suspend fun insertarSeries(series: List<SerieRealizada>)

    @Query(
        """
        SELECT
            e.id AS entrenamientoId,
            e.rutinaId AS rutinaId,
            e.nombreRutina AS nombreRutina,
            e.diaSemana AS diaSemana,
            e.etiquetaDia AS etiquetaDia,
            e.fecha AS fecha,
            e.inicioMillis AS inicioMillis,
            er.nombre AS ejercicioNombre,
            er.orden AS orden,
            er.repeticionesObjetivo AS repeticionesObjetivo,
            er.seriesObjetivo AS seriesObjetivo,
            s.peso AS peso,
            s.unidad AS unidad,
            s.repeticiones AS repeticiones,
            s.intensidad AS intensidad
        FROM series_realizadas AS s
        INNER JOIN ejercicios_realizados AS er ON er.id = s.ejercicioRealizadoId
        INNER JOIN entrenamientos AS e ON e.id = er.entrenamientoId
        WHERE s.completada = 1
          AND e.finMillis > 0
        ORDER BY e.inicioMillis ASC, er.orden ASC, s.numero ASC
        """
    )
    fun observarSeriesCompletadas(): Flow<List<FilaSerieResumen>>
}

package com.example.evofit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RutinaDao {
    @Insert
    suspend fun insertarRutina(rutina: Rutina): Long

    @Insert
    suspend fun insertarDia(dia: DiaRutina): Long

    @Insert
    suspend fun insertarGrupos(grupos: List<DiaGrupo>)

    @Insert
    suspend fun insertarEjercicio(ejercicio: EjercicioRutina): Long

    @Insert
    suspend fun insertarSeries(series: List<SerieConfigurada>)

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM rutinas
            WHERE origen = :origen AND nivel = :nivel AND nombre = :nombre COLLATE NOCASE AND id != :excepto
        )
        """
    )
    suspend fun existe(origen: String, nivel: String, nombre: String, excepto: Long): Boolean

    @Transaction
    @Query("SELECT * FROM rutinas ORDER BY creadaEn DESC")
    fun observar(): Flow<List<RutinaCompleta>>

    @Transaction
    @Query("SELECT * FROM rutinas WHERE id = :id")
    suspend fun obtener(id: Long): RutinaCompleta?

    @Query("DELETE FROM rutinas WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query("DELETE FROM dias_rutina WHERE rutinaId = :rutinaId")
    suspend fun eliminarDias(rutinaId: Long)

    @Query("UPDATE rutinas SET nombre = :nombre, objetivo = :objetivo WHERE id = :id")
    suspend fun actualizarDatos(id: Long, nombre: String, objetivo: String)
}

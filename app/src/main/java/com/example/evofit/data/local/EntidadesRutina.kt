package com.example.evofit.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "rutinas")
data class Rutina(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val nivel: String,
    val objetivo: String,
    val recomendaciones: String,
    val origen: String,
    val creadaEn: Long
)

@Entity(
    tableName = "dias_rutina",
    foreignKeys = [
        ForeignKey(
            entity = Rutina::class,
            parentColumns = ["id"],
            childColumns = ["rutinaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("rutinaId")]
)
data class DiaRutina(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rutinaId: Long,
    val diaSemana: Int,
    val etiqueta: String,
    val esDescanso: Boolean
)

@Entity(
    tableName = "dia_grupos",
    primaryKeys = ["diaRutinaId", "grupo"],
    foreignKeys = [
        ForeignKey(
            entity = DiaRutina::class,
            parentColumns = ["id"],
            childColumns = ["diaRutinaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("diaRutinaId")]
)
data class DiaGrupo(
    val diaRutinaId: Long,
    val grupo: String
)

@Entity(
    tableName = "ejercicios_rutina",
    foreignKeys = [
        ForeignKey(
            entity = DiaRutina::class,
            parentColumns = ["id"],
            childColumns = ["diaRutinaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("diaRutinaId")]
)
data class EjercicioRutina(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val diaRutinaId: Long,
    val catalogoId: String,
    val nombre: String,
    val grupo: String,
    val orden: Int,
    val repeticiones: String,
    val descansoSegundos: Int
)

@Entity(
    tableName = "series_configuradas",
    foreignKeys = [
        ForeignKey(
            entity = EjercicioRutina::class,
            parentColumns = ["id"],
            childColumns = ["ejercicioRutinaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ejercicioRutinaId")]
)
data class SerieConfigurada(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ejercicioRutinaId: Long,
    val numero: Int,
    val repeticiones: String
)

@Entity(tableName = "entrenamientos")
data class Entrenamiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rutinaId: Long?,
    val nombreRutina: String,
    val nivel: String,
    val diaSemana: Int,
    val etiquetaDia: String,
    val fecha: String,
    val inicioMillis: Long,
    val finMillis: Long,
    val duracionSegundos: Long,
    val unidadPeso: String
)

@Entity(
    tableName = "ejercicios_realizados",
    foreignKeys = [
        ForeignKey(
            entity = Entrenamiento::class,
            parentColumns = ["id"],
            childColumns = ["entrenamientoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("entrenamientoId")]
)
data class EjercicioRealizado(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entrenamientoId: Long,
    val nombre: String,
    val grupo: String,
    val orden: Int,
    val repeticionesObjetivo: String,
    val descansoSegundos: Int,
    val seriesObjetivo: Int
)

@Entity(
    tableName = "series_realizadas",
    foreignKeys = [
        ForeignKey(
            entity = EjercicioRealizado::class,
            parentColumns = ["id"],
            childColumns = ["ejercicioRealizadoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ejercicioRealizadoId")]
)
data class SerieRealizada(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ejercicioRealizadoId: Long,
    val numero: Int,
    val peso: Double,
    val unidad: String,
    val repeticiones: Int,
    val intensidad: String,
    val completada: Boolean
)

data class EjercicioConSeries(
    @Embedded val ejercicio: EjercicioRutina,
    @Relation(parentColumn = "id", entityColumn = "ejercicioRutinaId")
    val series: List<SerieConfigurada>
)

data class DiaConDetalle(
    @Embedded val dia: DiaRutina,
    @Relation(parentColumn = "id", entityColumn = "diaRutinaId")
    val grupos: List<DiaGrupo>,
    @Relation(entity = EjercicioRutina::class, parentColumn = "id", entityColumn = "diaRutinaId")
    val ejercicios: List<EjercicioConSeries>
)

data class FilaSerieResumen(
    val entrenamientoId: Long,
    val rutinaId: Long?,
    val nombreRutina: String,
    val diaSemana: Int,
    val etiquetaDia: String,
    val fecha: String,
    val inicioMillis: Long,
    val ejercicioNombre: String,
    val orden: Int,
    val repeticionesObjetivo: String,
    val seriesObjetivo: Int,
    val peso: Double,
    val unidad: String,
    val repeticiones: Int,
    val intensidad: String
)

data class RutinaCompleta(
    @Embedded val rutina: Rutina,
    @Relation(entity = DiaRutina::class, parentColumn = "id", entityColumn = "rutinaId")
    val dias: List<DiaConDetalle>
)

package com.example.evofit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Usuario::class,
        Rutina::class,
        DiaRutina::class,
        DiaGrupo::class,
        EjercicioRutina::class,
        SerieConfigurada::class,
        Entrenamiento::class,
        EjercicioRealizado::class,
        SerieRealizada::class
    ],
    version = 2,
    exportSchema = false
)
abstract class EvoFitDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun rutinaDao(): RutinaDao
    abstract fun entrenamientoDao(): EntrenamientoDao

    companion object {
        @Volatile
        private var instancia: EvoFitDatabase? = null

        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `rutinas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL, `nivel` TEXT NOT NULL, `objetivo` TEXT NOT NULL, `recomendaciones` TEXT NOT NULL, `origen` TEXT NOT NULL, `creadaEn` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `dias_rutina` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `rutinaId` INTEGER NOT NULL, `diaSemana` INTEGER NOT NULL, `etiqueta` TEXT NOT NULL, `esDescanso` INTEGER NOT NULL, FOREIGN KEY(`rutinaId`) REFERENCES `rutinas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_dias_rutina_rutinaId` ON `dias_rutina` (`rutinaId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `dia_grupos` (`diaRutinaId` INTEGER NOT NULL, `grupo` TEXT NOT NULL, PRIMARY KEY(`diaRutinaId`, `grupo`), FOREIGN KEY(`diaRutinaId`) REFERENCES `dias_rutina`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_dia_grupos_diaRutinaId` ON `dia_grupos` (`diaRutinaId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `ejercicios_rutina` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `diaRutinaId` INTEGER NOT NULL, `catalogoId` TEXT NOT NULL, `nombre` TEXT NOT NULL, `grupo` TEXT NOT NULL, `orden` INTEGER NOT NULL, `repeticiones` TEXT NOT NULL, `descansoSegundos` INTEGER NOT NULL, FOREIGN KEY(`diaRutinaId`) REFERENCES `dias_rutina`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ejercicios_rutina_diaRutinaId` ON `ejercicios_rutina` (`diaRutinaId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `series_configuradas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ejercicioRutinaId` INTEGER NOT NULL, `numero` INTEGER NOT NULL, `repeticiones` TEXT NOT NULL, FOREIGN KEY(`ejercicioRutinaId`) REFERENCES `ejercicios_rutina`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_series_configuradas_ejercicioRutinaId` ON `series_configuradas` (`ejercicioRutinaId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `entrenamientos` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `rutinaId` INTEGER, `nombreRutina` TEXT NOT NULL, `nivel` TEXT NOT NULL, `diaSemana` INTEGER NOT NULL, `etiquetaDia` TEXT NOT NULL, `fecha` TEXT NOT NULL, `inicioMillis` INTEGER NOT NULL, `finMillis` INTEGER NOT NULL, `duracionSegundos` INTEGER NOT NULL, `unidadPeso` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `ejercicios_realizados` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entrenamientoId` INTEGER NOT NULL, `nombre` TEXT NOT NULL, `grupo` TEXT NOT NULL, `orden` INTEGER NOT NULL, `repeticionesObjetivo` TEXT NOT NULL, `descansoSegundos` INTEGER NOT NULL, `seriesObjetivo` INTEGER NOT NULL, FOREIGN KEY(`entrenamientoId`) REFERENCES `entrenamientos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ejercicios_realizados_entrenamientoId` ON `ejercicios_realizados` (`entrenamientoId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `series_realizadas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ejercicioRealizadoId` INTEGER NOT NULL, `numero` INTEGER NOT NULL, `peso` REAL NOT NULL, `unidad` TEXT NOT NULL, `repeticiones` INTEGER NOT NULL, `intensidad` TEXT NOT NULL, `completada` INTEGER NOT NULL, FOREIGN KEY(`ejercicioRealizadoId`) REFERENCES `ejercicios_realizados`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_series_realizadas_ejercicioRealizadoId` ON `series_realizadas` (`ejercicioRealizadoId`)")
            }
        }

        fun obtener(context: Context): EvoFitDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    EvoFitDatabase::class.java,
                    "evofit.db"
                ).addMigrations(MIGRACION_1_2).build().also { instancia = it }
            }
        }
    }
}

package com.example.evofit.data.catalogo

enum class GrupoMuscular(val etiqueta: String) {
    PECHO("Pecho"),
    ESPALDA("Espalda"),
    PIERNAS("Piernas"),
    HOMBROS("Hombros"),
    BICEPS("Bíceps"),
    TRICEPS("Tríceps"),
    ABDOMEN("Abdomen");

    companion object {
        fun desde(etiqueta: String): GrupoMuscular? = entries.find { it.etiqueta == etiqueta }
    }
}

enum class DiaSemana(
    val numero: Int,
    val letra: String,
    val corto: String,
    val nombre: String
) {
    LUNES(1, "L", "Lun", "Lunes"),
    MARTES(2, "M", "Mar", "Martes"),
    MIERCOLES(3, "M", "Mié", "Miércoles"),
    JUEVES(4, "J", "Jue", "Jueves"),
    VIERNES(5, "V", "Vie", "Viernes"),
    SABADO(6, "S", "Sáb", "Sábado"),
    DOMINGO(7, "D", "Dom", "Domingo");

    companion object {
        fun desde(numero: Int): DiaSemana = entries.first { it.numero == numero }
    }
}

enum class Intensidad(val etiqueta: String, val corto: String) {
    BAJA("Baja", "B"),
    EFECTIVA("Efectiva", "E"),
    ALTA("Alta", "A")
}

enum class UnidadPeso(val etiqueta: String) {
    KG("KG"),
    LB("LB")
}

data class EjercicioPlantilla(
    val nombre: String,
    val series: Int,
    val repeticiones: String,
    val descansoSegundos: Int = 90,
    val catalogoId: String = "",
    val grupo: String = ""
)

fun descripcionSeries(series: Int, repeticiones: String): String {
    val porPierna = repeticiones.contains("por pierna", ignoreCase = true)
    val base = repeticiones.replace("por pierna", "", ignoreCase = true).trim()
    val medida = if (base.contains("segundo", ignoreCase = true)) base else "$base repeticiones"
    val sufijo = if (porPierna) " por pierna" else ""
    return "$series series de $medida$sufijo"
}

data class DiaPlantilla(
    val dia: DiaSemana,
    val etiqueta: String,
    val grupos: List<GrupoMuscular> = emptyList(),
    val ejercicios: List<EjercicioPlantilla> = emptyList(),
    val esDescanso: Boolean = false
)

data class RutinaPlantilla(
    val nivel: String,
    val nombre: String,
    val objetivo: String,
    val recomendaciones: String = "",
    val dias: List<DiaPlantilla>
) {
    val diasEntrenamiento: Int get() = dias.count { !it.esDescanso }
}

object OrigenRutina {
    const val RECOMENDADA = "RECOMENDADA"
    const val PERSONALIZADA = "PERSONALIZADA"
}

data class EjercicioGuardado(
    val nombre: String,
    val grupo: String,
    val orden: Int,
    val repeticiones: String,
    val descansoSegundos: Int,
    val series: Int,
    val catalogoId: String = ""
)

data class DiaGuardado(
    val id: Long,
    val dia: DiaSemana,
    val etiqueta: String,
    val esDescanso: Boolean,
    val grupos: List<String>,
    val ejercicios: List<EjercicioGuardado>
)

data class RutinaGuardada(
    val id: Long,
    val nombre: String,
    val nivel: String,
    val objetivo: String,
    val recomendaciones: String,
    val origen: String,
    val dias: List<DiaGuardado>
)

package com.example.evofit.data.catalogo

object RutinasRecomendadas {
    val niveles = listOf("Principiante", "Intermedio", "Avanzado")

    private val RECOMENDACIONES_PRINCIPIANTE = """
Calentar de 5 a 10 minutos antes de entrenar.
Descansar entre 60 y 120 segundos entre series.
Utilizar pesos que permitan mantener una buena técnica.
Evitar llegar al fallo muscular en todas las series.
Aumentar progresivamente el peso conforme mejore la fuerza.
Dormir entre 7 y 9 horas para favorecer la recuperación.
    """.trimIndent()

    val todas: List<RutinaPlantilla> = listOf(
        principiante(),
        intermedio(),
        avanzado()
    )

    fun deNivel(nivel: String): RutinaPlantilla = todas.first { it.nivel == nivel }

    private fun principiante() = RutinaPlantilla(
        nivel = "Principiante",
        nombre = "Rutina Principiante",
        objetivo = "",
        recomendaciones = RECOMENDACIONES_PRINCIPIANTE,
        dias = listOf(
            sesion(DiaSemana.LUNES, "Pecho, tríceps y hombro", listOf(GrupoMuscular.PECHO, GrupoMuscular.TRICEPS, GrupoMuscular.HOMBROS), listOf(
                e("Press de banca con barra", 3, "10-12"),
                e("Press inclinado con mancuernas", 3, "10-12"),
                e("Aperturas en máquina (Peck Deck)", 2, "12-15"),
                e("Press de hombro en máquina", 3, "10-12"),
                e("Elevaciones laterales", 2, "12-15"),
                e("Extensión de tríceps en polea", 3, "12-15"),
                e("Extensión de tríceps sobre cabeza", 2, "12-15")
            )),
            sesion(DiaSemana.MARTES, "Espalda, bíceps y abdomen", listOf(GrupoMuscular.ESPALDA, GrupoMuscular.BICEPS, GrupoMuscular.ABDOMEN), listOf(
                e("Jalón al pecho", 3, "10-12"),
                e("Remo sentado en polea", 3, "10-12"),
                e("Remo con mancuerna", 2, "10-12"),
                e("Face Pull en polea", 2, "12-15"),
                e("Curl de bíceps con barra", 3, "10-12"),
                e("Curl martillo con mancuernas", 2, "12"),
                e("Crunch abdominal", 3, "15"),
                e("Elevaciones de piernas", 2, "12")
            )),
            sesion(DiaSemana.MIERCOLES, "Piernas completas", listOf(GrupoMuscular.PIERNAS), listOf(
                e("Sentadilla en máquina Smith", 3, "10-12"),
                e("Prensa de piernas", 3, "10-12"),
                e("Extensión de cuádriceps", 2, "12-15"),
                e("Curl femoral sentado", 3, "12"),
                e("Hip Thrust en máquina", 3, "10-12"),
                e("Elevación de pantorrillas de pie", 3, "15"),
                e("Elevación de pantorrillas sentado", 2, "15")
            )),
            sesion(DiaSemana.JUEVES, "Espalda, bíceps y abdomen", listOf(GrupoMuscular.ESPALDA, GrupoMuscular.BICEPS, GrupoMuscular.ABDOMEN), listOf(
                e("Jalón al pecho con agarre estrecho", 3, "10-12"),
                e("Remo con pecho apoyado", 3, "10-12"),
                e("Pullover en polea", 2, "12-15"),
                e("Pájaros en máquina", 2, "12-15"),
                e("Curl alterno con mancuernas", 3, "10-12"),
                e("Curl de bíceps en banco Scott", 2, "12"),
                e("Plancha abdominal", 3, "30 segundos"),
                e("Crunch en polea", 2, "15")
            )),
            sesion(DiaSemana.VIERNES, "Pecho, tríceps y hombro", listOf(GrupoMuscular.PECHO, GrupoMuscular.TRICEPS, GrupoMuscular.HOMBROS), listOf(
                e("Press inclinado en máquina", 3, "10-12"),
                e("Press de pecho en máquina", 3, "10-12"),
                e("Cruce de poleas", 2, "12-15"),
                e("Elevaciones laterales en polea", 3, "12-15"),
                e("Press de hombro con mancuernas", 2, "10-12"),
                e("Extensión de tríceps con cuerda", 3, "12-15"),
                e("Extensión de tríceps unilateral", 2, "12")
            )),
            sesion(DiaSemana.SABADO, "Piernas completas", listOf(GrupoMuscular.PIERNAS), listOf(
                e("Peso muerto rumano con mancuernas", 3, "10-12"),
                e("Hip Thrust", 3, "10-12"),
                e("Curl femoral acostado", 3, "12"),
                e("Sentadilla en máquina Smith", 2, "10-12"),
                e("Extensión de cuádriceps", 2, "12-15"),
                e("Abducción de cadera en máquina", 2, "15"),
                e("Elevación de pantorrillas", 3, "15")
            )),
            DiaPlantilla(DiaSemana.DOMINGO, "Descanso", esDescanso = true)
        )
    )

    private fun intermedio() = RutinaPlantilla(
        nivel = "Intermedio",
        nombre = "Rutina Intermedio",
        objetivo = "Hipertrofia muscular, aumento de fuerza y desarrollo equilibrado de todos los grupos musculares.",
        dias = listOf(
            sesion(DiaSemana.LUNES, "Pecho y tríceps", listOf(GrupoMuscular.PECHO, GrupoMuscular.TRICEPS), listOf(
                e("Press de banca con barra", 4, "8-10"),
                e("Press inclinado con mancuernas", 3, "10-12"),
                e("Aperturas en polea", 3, "12-15"),
                e("Press de pecho en máquina", 3, "10-12"),
                e("Extensión de tríceps con cuerda", 3, "10-12"),
                e("Extensión de tríceps sobre cabeza", 3, "12"),
                e("Fondos asistidos", 2, "10-12")
            )),
            sesion(DiaSemana.MARTES, "Espalda y bíceps", listOf(GrupoMuscular.ESPALDA, GrupoMuscular.BICEPS), listOf(
                e("Dominadas", 3, "8-10"),
                e("Remo con barra", 4, "8-10"),
                e("Jalón al pecho", 3, "10-12"),
                e("Remo sentado en polea", 3, "10-12"),
                e("Face Pull", 3, "12-15"),
                e("Curl de bíceps con barra Z", 3, "10-12"),
                e("Curl inclinado con mancuernas", 3, "10-12"),
                e("Curl martillo", 2, "12")
            )),
            sesion(DiaSemana.MIERCOLES, "Piernas completas", listOf(GrupoMuscular.PIERNAS), listOf(
                e("Sentadilla con barra", 4, "8-10"),
                e("Prensa de piernas", 3, "10-12"),
                e("Peso muerto rumano", 3, "10-12"),
                e("Extensión de cuádriceps", 3, "12-15"),
                e("Curl femoral acostado", 3, "10-12"),
                e("Hip Thrust", 3, "10-12"),
                e("Elevación de pantorrillas de pie", 4, "12-15")
            )),
            sesion(DiaSemana.JUEVES, "Hombros y abdomen", listOf(GrupoMuscular.HOMBROS, GrupoMuscular.ABDOMEN), listOf(
                e("Press militar con barra", 3, "8-10"),
                e("Elevaciones laterales con mancuernas", 4, "12-15"),
                e("Pájaros con mancuernas", 3, "12-15"),
                e("Elevaciones laterales en polea", 2, "12-15"),
                e("Encogimientos de hombros con mancuernas", 3, "12"),
                e("Crunch en polea", 3, "15"),
                e("Elevaciones de piernas", 3, "12-15"),
                e("Plancha abdominal", 3, "30-45 segundos")
            )),
            sesion(DiaSemana.VIERNES, "Torso completo", listOf(GrupoMuscular.PECHO, GrupoMuscular.ESPALDA, GrupoMuscular.HOMBROS, GrupoMuscular.BICEPS, GrupoMuscular.TRICEPS), listOf(
                e("Press inclinado con barra", 3, "8-10"),
                e("Remo con mancuerna", 3, "10-12"),
                e("Press de pecho en máquina", 3, "10-12"),
                e("Jalón al pecho con agarre estrecho", 3, "10-12"),
                e("Elevaciones laterales", 3, "12-15"),
                e("Curl de bíceps en polea", 2, "12"),
                e("Extensión de tríceps en polea", 2, "12")
            )),
            DiaPlantilla(DiaSemana.SABADO, "Descanso", esDescanso = true),
            DiaPlantilla(DiaSemana.DOMINGO, "Descanso", esDescanso = true)
        )
    )

    private fun avanzado() = RutinaPlantilla(
        nivel = "Avanzado",
        nombre = "Rutina Avanzado",
        objetivo = "Maximizar la hipertrofia muscular, desarrollar fuerza, trabajar puntos débiles y conseguir un desarrollo muscular más especializado.",
        dias = listOf(
            sesion(DiaSemana.LUNES, "Pecho y espalda (énfasis en fuerza)", listOf(GrupoMuscular.PECHO, GrupoMuscular.ESPALDA), listOf(
                e("Press de banca con barra", 4, "6-8"),
                e("Dominadas con peso", 4, "6-8"),
                e("Press inclinado con mancuernas", 3, "8-10"),
                e("Remo con barra", 3, "8-10"),
                e("Aperturas en polea", 3, "12-15"),
                e("Jalón al pecho", 3, "10-12")
            )),
            sesion(DiaSemana.MARTES, "Piernas (cuádriceps y pantorrillas)", listOf(GrupoMuscular.PIERNAS), listOf(
                e("Sentadilla con barra", 4, "6-8"),
                e("Prensa de piernas", 4, "10-12"),
                e("Sentadilla búlgara", 3, "10-12 por pierna"),
                e("Extensión de cuádriceps", 3, "12-15"),
                e("Hack squat", 3, "10-12"),
                e("Elevación de pantorrillas de pie", 4, "12-15"),
                e("Elevación de pantorrillas sentado", 3, "15-20")
            )),
            sesion(DiaSemana.MIERCOLES, "Hombros y brazos", listOf(GrupoMuscular.HOMBROS, GrupoMuscular.BICEPS, GrupoMuscular.TRICEPS), listOf(
                e("Press militar con barra", 3, "6-8"),
                e("Elevaciones laterales con mancuernas", 4, "12-15"),
                e("Pájaros en máquina", 3, "12-15"),
                e("Curl de bíceps con barra Z", 3, "8-10"),
                e("Curl inclinado con mancuernas", 3, "10-12"),
                e("Curl martillo", 2, "12"),
                e("Press francés", 3, "8-10"),
                e("Extensión de tríceps con cuerda", 3, "10-12")
            )),
            sesion(DiaSemana.JUEVES, "Pecho y espalda (énfasis en hipertrofia)", listOf(GrupoMuscular.PECHO, GrupoMuscular.ESPALDA), listOf(
                e("Press inclinado con barra", 3, "8-10"),
                e("Remo en máquina con pecho apoyado", 3, "10-12"),
                e("Press de pecho en máquina", 3, "10-12"),
                e("Jalón al pecho con agarre amplio", 3, "10-12"),
                e("Cruce de poleas", 3, "12-15"),
                e("Pullover en polea", 3, "12-15")
            )),
            sesion(DiaSemana.VIERNES, "Piernas (glúteos e isquiotibiales)", listOf(GrupoMuscular.PIERNAS), listOf(
                e("Peso muerto rumano", 4, "8-10"),
                e("Hip Thrust", 4, "8-12"),
                e("Curl femoral sentado", 3, "10-12"),
                e("Curl femoral acostado", 3, "12-15"),
                e("Zancadas caminando", 3, "10-12 por pierna"),
                e("Abducción de cadera en máquina", 3, "15-20"),
                e("Elevación de pantorrillas", 4, "12-15")
            )),
            sesion(DiaSemana.SABADO, "Hombros, brazos y abdomen", listOf(GrupoMuscular.HOMBROS, GrupoMuscular.BICEPS, GrupoMuscular.TRICEPS, GrupoMuscular.ABDOMEN), listOf(
                e("Press de hombros con mancuernas", 3, "8-10"),
                e("Elevaciones laterales en polea", 4, "12-15"),
                e("Pájaros con mancuernas", 3, "12-15"),
                e("Curl Scott", 3, "10-12"),
                e("Curl de bíceps en polea", 3, "12-15"),
                e("Extensión de tríceps sobre cabeza", 3, "10-12"),
                e("Extensión de tríceps unilateral", 2, "12-15"),
                e("Crunch abdominal", 3, "15"),
                e("Elevaciones de piernas", 3, "12-15")
            )),
            DiaPlantilla(DiaSemana.DOMINGO, "Descanso", esDescanso = true)
        )
    )

    private fun e(nombre: String, series: Int, repeticiones: String) =
        EjercicioPlantilla(nombre = nombre, series = series, repeticiones = repeticiones)

    private fun sesion(
        dia: DiaSemana,
        etiqueta: String,
        grupos: List<GrupoMuscular>,
        ejercicios: List<EjercicioPlantilla>
    ) = DiaPlantilla(dia, etiqueta, grupos, ejercicios)
}

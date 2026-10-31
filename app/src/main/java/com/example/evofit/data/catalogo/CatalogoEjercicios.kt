package com.example.evofit.data.catalogo

data class EjercicioCatalogo(
    val id: String,
    val nombre: String,
    val grupo: GrupoMuscular
)

object CatalogoEjercicios {
    val todos: List<EjercicioCatalogo> = listOf(
        item("pecho-01", "Press de banca plano con barra", GrupoMuscular.PECHO),
        item("pecho-02", "Press inclinado con mancuernas", GrupoMuscular.PECHO),
        item("pecho-03", "Press de pecho en máquina", GrupoMuscular.PECHO),
        item("pecho-04", "Press declinado con barra", GrupoMuscular.PECHO),
        item("pecho-05", "Aperturas con mancuernas", GrupoMuscular.PECHO),
        item("pecho-06", "Peck Deck (aperturas en máquina)", GrupoMuscular.PECHO),
        item("pecho-07", "Cruce de poleas de arriba hacia abajo", GrupoMuscular.PECHO),
        item("pecho-08", "Cruce de poleas de abajo hacia arriba", GrupoMuscular.PECHO),
        item("pecho-09", "Fondos en paralelas inclinando el torso", GrupoMuscular.PECHO),
        item("pecho-10", "Press inclinado en máquina", GrupoMuscular.PECHO),
        item("espalda-01", "Dominadas pronas", GrupoMuscular.ESPALDA),
        item("espalda-02", "Jalón al pecho con agarre amplio", GrupoMuscular.ESPALDA),
        item("espalda-03", "Remo con barra", GrupoMuscular.ESPALDA),
        item("espalda-04", "Remo con mancuerna a una mano", GrupoMuscular.ESPALDA),
        item("espalda-05", "Remo sentado en polea", GrupoMuscular.ESPALDA),
        item("espalda-06", "Remo en máquina con pecho apoyado", GrupoMuscular.ESPALDA),
        item("espalda-07", "Remo en T con barra", GrupoMuscular.ESPALDA),
        item("espalda-08", "Pullover en polea", GrupoMuscular.ESPALDA),
        item("espalda-09", "Peso muerto convencional", GrupoMuscular.ESPALDA),
        item("espalda-10", "Hiperextensiones lumbares", GrupoMuscular.ESPALDA),
        item("piernas-01", "Sentadilla con barra", GrupoMuscular.PIERNAS),
        item("piernas-02", "Prensa de piernas", GrupoMuscular.PIERNAS),
        item("piernas-03", "Sentadilla búlgara", GrupoMuscular.PIERNAS),
        item("piernas-04", "Extensión de cuádriceps en máquina", GrupoMuscular.PIERNAS),
        item("piernas-05", "Curl femoral sentado", GrupoMuscular.PIERNAS),
        item("piernas-06", "Curl femoral acostado", GrupoMuscular.PIERNAS),
        item("piernas-07", "Peso muerto rumano", GrupoMuscular.PIERNAS),
        item("piernas-08", "Hip Thrust con barra", GrupoMuscular.PIERNAS),
        item("piernas-09", "Elevación de pantorrillas de pie", GrupoMuscular.PIERNAS),
        item("piernas-10", "Elevación de pantorrillas sentado", GrupoMuscular.PIERNAS),
        item("hombros-01", "Press militar con barra", GrupoMuscular.HOMBROS),
        item("hombros-02", "Press de hombros con mancuernas", GrupoMuscular.HOMBROS),
        item("hombros-03", "Press Arnold", GrupoMuscular.HOMBROS),
        item("hombros-04", "Elevaciones laterales con mancuernas", GrupoMuscular.HOMBROS),
        item("hombros-05", "Elevaciones laterales en polea", GrupoMuscular.HOMBROS),
        item("hombros-06", "Elevaciones frontales con mancuernas", GrupoMuscular.HOMBROS),
        item("hombros-07", "Pájaros con mancuernas", GrupoMuscular.HOMBROS),
        item("hombros-08", "Reverse Peck Deck", GrupoMuscular.HOMBROS),
        item("hombros-09", "Face Pull en polea", GrupoMuscular.HOMBROS),
        item("hombros-10", "Remo al mentón", GrupoMuscular.HOMBROS),
        item("biceps-01", "Curl de bíceps con barra recta", GrupoMuscular.BICEPS),
        item("biceps-02", "Curl con barra Z", GrupoMuscular.BICEPS),
        item("biceps-03", "Curl alterno con mancuernas", GrupoMuscular.BICEPS),
        item("biceps-04", "Curl inclinado con mancuernas", GrupoMuscular.BICEPS),
        item("biceps-05", "Curl martillo", GrupoMuscular.BICEPS),
        item("biceps-06", "Curl concentrado", GrupoMuscular.BICEPS),
        item("biceps-07", "Curl en banco Scott", GrupoMuscular.BICEPS),
        item("biceps-08", "Curl de bíceps en polea baja", GrupoMuscular.BICEPS),
        item("biceps-09", "Curl Bayesian en polea", GrupoMuscular.BICEPS),
        item("biceps-10", "Curl inverso con barra Z", GrupoMuscular.BICEPS),
        item("triceps-01", "Extensión de tríceps con cuerda en polea", GrupoMuscular.TRICEPS),
        item("triceps-02", "Press francés con barra Z", GrupoMuscular.TRICEPS),
        item("triceps-03", "Extensión de tríceps sobre cabeza con mancuerna", GrupoMuscular.TRICEPS),
        item("triceps-04", "Extensión de tríceps con barra recta en polea", GrupoMuscular.TRICEPS),
        item("triceps-05", "Fondos en paralelas", GrupoMuscular.TRICEPS),
        item("triceps-06", "Press de banca con agarre cerrado", GrupoMuscular.TRICEPS),
        item("triceps-07", "Extensión unilateral de tríceps en polea", GrupoMuscular.TRICEPS),
        item("triceps-08", "Extensión de tríceps con cuerda sobre cabeza", GrupoMuscular.TRICEPS),
        item("triceps-09", "Extensión de tríceps con agarre inverso", GrupoMuscular.TRICEPS),
        item("triceps-10", "Extensión de tríceps en máquina", GrupoMuscular.TRICEPS),
        item("abdomen-01", "Crunch abdominal", GrupoMuscular.ABDOMEN),
        item("abdomen-02", "Crunch en polea", GrupoMuscular.ABDOMEN),
        item("abdomen-03", "Crunch en máquina", GrupoMuscular.ABDOMEN),
        item("abdomen-04", "Elevaciones de piernas", GrupoMuscular.ABDOMEN),
        item("abdomen-05", "Elevaciones de rodillas colgado", GrupoMuscular.ABDOMEN),
        item("abdomen-06", "Crunch inverso", GrupoMuscular.ABDOMEN),
        item("abdomen-07", "Giros rusos (Russian Twist)", GrupoMuscular.ABDOMEN),
        item("abdomen-08", "Plancha abdominal", GrupoMuscular.ABDOMEN),
        item("abdomen-09", "Plancha lateral", GrupoMuscular.ABDOMEN),
        item("abdomen-10", "Pallof Press", GrupoMuscular.ABDOMEN)
    )

    fun porId(id: String): EjercicioCatalogo? = todos.find { it.id == id }

    fun deGrupos(grupos: Set<GrupoMuscular>): List<EjercicioCatalogo> {
        if (grupos.isEmpty()) return emptyList()
        return todos.filter { it.grupo in grupos }
    }

    private fun item(id: String, nombre: String, grupo: GrupoMuscular) =
        EjercicioCatalogo(id, nombre, grupo)
}

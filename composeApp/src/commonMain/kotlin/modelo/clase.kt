package modelo

import kotlinx.serialization.Serializable

@Serializable
data class Clase(
    val nombre: String,
    val horaInicio: String,
    val horaFin: String,
    val lugar: String,
    val profesor: String
)
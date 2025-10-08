package modelo

import kotlinx.serialization.Serializable

@Serializable
data class Tarea(
    val asunto: String,
    val fechaEntrega: String,
    val correo: String,
    val id_clase: String
)
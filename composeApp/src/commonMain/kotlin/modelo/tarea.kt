package modelo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tarea(
    @SerialName("id_tarea") val idTarea: Int,
    @SerialName("id_clase") val idClase: Int,
    @SerialName("asunto") val asunto: String,
    @SerialName("descripcion") val descripcion: String,
    @SerialName("fecha_inicio") val fechaInicio: String,
    @SerialName("fecha_fin") val fechaFin: String,
    @SerialName("observaciones") val observaciones: String? = null,
    @SerialName("nombre_clase") val nombreClase: String
)
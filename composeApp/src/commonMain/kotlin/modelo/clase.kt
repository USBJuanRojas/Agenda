package modelo

import kotlinx.serialization.Serializable

@Serializable
data class Clase(
    val id_clase: Int,
    val nombre_clase: String,
    val descripcion: String, // nullable
    val hora_inicio: String,
    val hora_fin: String,
    val lugar: String,
    val id_profesor: Int?,
    val profesor_nombre: String? = "",
    val profesor_apellido: String? = "",
    var dias_semana: String? = null
)
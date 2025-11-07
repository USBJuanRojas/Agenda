package modelo

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id_usuario: Int,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val user: String,
    val id_rol: Int
)
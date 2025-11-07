package modelo

import kotlinx.serialization.Serializable

@Serializable
data class StudentResponse(
    val success: Boolean,
    val usuarios: List<User>
)
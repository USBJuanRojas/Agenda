package repository

import androidx.compose.runtime.mutableStateListOf
import org.jjrn.Agenda.User

object UsersRepository {
    val users = mutableStateListOf(
        User("Administrador", "admin", "admin@example.com", "1234"),
        User("Nicolas", "nico", "nicolas@example.com", "nico"),
        User("Juan Jose", "juanjo", "jjrojas@example.com", "juanjo")
    )

    fun addUser(user: User) {
        users.add(user)
    }

    fun getUser(user: String): User? {
        return users.find { it.user == user }
    }

}
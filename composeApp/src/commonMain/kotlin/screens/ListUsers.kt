package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bottombar.BottomBarScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ListUsers : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var users by remember { mutableStateOf<List<User>>(emptyList()) }
        var cargando by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }

        var showDialog by remember { mutableStateOf(false) }
        var userToDelete by remember { mutableStateOf<User?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }

        val listarUrl = "http://10.0.2.2/API/listarUsuario.php"
        val eliminarUrl = "http://10.0.2.2/API/eliminarUsuario.php"

        fun cargar() {
            scope.launch {
                cargando = true
                error = null
                val client = HttpClient()
                try {
                    val respuesta = client.get(listarUrl).bodyAsText()
                    val lista = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }.decodeFromString<List<User>>(respuesta)
                    users = lista
                } catch (e: Exception) {
                    error = e.message ?: "Error desconocido"
                } finally {
                    client.close()
                    cargando = false
                }
            }
        }

        // Función para eliminar usuario
        fun eliminarUsuario(user: User) {
            scope.launch {
                val client = HttpClient()
                try {
                    val response = client.submitForm(
                        url = eliminarUrl,
                        formParameters = Parameters.build {
                            append("user", user.user)
                        }
                    ).bodyAsText()

                    if (response.contains("success", ignoreCase = true)) {
                        users = users.filterNot { it.user == user.user }
                        snackbarHostState.showSnackbar("Usuario eliminado correctamente")
                    } else {
                        snackbarHostState.showSnackbar("Error al eliminar usuario")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Error de conexión: ${e.message}")
                } finally {
                    client.close()
                }
            }
        }

        LaunchedEffect(Unit) { cargar() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Listado de Usuarios") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(BottomBarScreen()) }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { cargar() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    cargando -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    error != null -> {
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    users.isEmpty() -> {
                        Text(
                            text = "No hay usuarios para mostrar.",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(users) { u ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            text = u.user,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text("Nombre: ${u.nombre} ${u.apellido}")
                                        Text("Correo: ${u.correo}")
                                        Text("Rol: ${u.id_rol}")

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            OutlinedButton(onClick = {
                                                navigator.push(EditUserScreen(u))
                                            }) {
                                                Text("Editar")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = {
                                                    userToDelete = u
                                                    showDialog = true
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error
                                                )
                                            ) {
                                                Text("Eliminar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 🔹 Diálogo de confirmación
            if (showDialog && userToDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Seguro que deseas eliminar al usuario ${userToDelete?.user}?") },
                    confirmButton = {
                        TextButton(onClick = {
                            eliminarUsuario(userToDelete!!)
                            showDialog = false
                        }) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Serializable
data class User(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val user: String,
    val id_rol: Int
)
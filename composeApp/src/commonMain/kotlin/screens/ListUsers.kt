package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bottombar.BottomBarScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import modelo.User

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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFF751F), // Naranja
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(BottomBarScreen()) }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { cargar() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Recargar",
                                tint = Color.White
                            )
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
                            CircularProgressIndicator(color = Color(0xFFFF751F))
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

                                        val roles = listOf(
                                            1 to "Administrador",
                                            2 to "Profesor",
                                            3 to "Estudiante"
                                        )

                                        val rolNombre = roles.find { it.first == u.id_rol }?.second
                                            ?: "Desconocido"

                                        Text("Rol: $rolNombre")

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    navigator.push(EditUserScreen(u))
                                                }, colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Gray,
                                                    contentColor = Color.White
                                                )
                                            ) {
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
                        TextButton(
                            onClick = {
                                eliminarUsuario(userToDelete!!)
                                showDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ),
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}
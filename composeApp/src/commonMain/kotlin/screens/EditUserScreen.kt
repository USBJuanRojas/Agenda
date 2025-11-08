package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import modelo.User

class EditUserScreen(private val user: User) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        // Estados locales para los campos
        var nombre by remember { mutableStateOf(user.nombre) }
        var apellido by remember { mutableStateOf(user.apellido) }
        var correo by remember { mutableStateOf(user.correo) }
        var usuario by remember { mutableStateOf(user.user) }
        var rol by remember { mutableStateOf(user.id_rol.toString()) }

        var cargando by remember { mutableStateOf(false) }
        var mensaje by remember { mutableStateOf<String?>(null) }

        val url = "http://10.0.2.2/API/modificarUsuario.php"

        fun editarUsuario() {
            scope.launch {
                cargando = true
                mensaje = null
                val client = HttpClient()
                try {
                    val response = client.submitForm(
                        url = url,
                        formParameters = Parameters.build {
                            append("nombre", nombre)
                            append("apellido", apellido)
                            append("correo", correo)
                            append("user", usuario)
                            append("id_rol", rol)
                        }
                    ).bodyAsText()

                    if (response.contains("exito", ignoreCase = true)) {
                        mensaje = "Usuario actualizado correctamente."
                        navigator.push(BottomBarScreen())
                    } else {
                        mensaje = "Error al actualizar: $response"
                    }
                } catch (e: Exception) {
                    mensaje = "Error de red: ${e.message}"
                } finally {
                    client.close()
                    cargando = false
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar Usuario") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(ListUsers()) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = usuario,
                    onValueChange = { usuario = it },
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth()
                )

                //Dropdown de roles
                val roles = listOf(
                    "1" to "Administrador",
                    "2" to "Profesor",
                    "3" to "Estudiante"
                )
                var expandedRol by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandedRol,
                    onExpandedChange = { expandedRol = !expandedRol }
                ) {
                    OutlinedTextField(
                        value = roles.find { it.first == rol }?.second ?: "Seleccionar rol",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedRol,
                        onDismissRequest = { expandedRol = false }
                    ) {
                        roles.forEach { (id, nombre) ->
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = {
                                    rol = id // Guarda el ID numérico que espera la API
                                    expandedRol = false
                                }
                            )
                        }
                    }
                }


                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = { editarUsuario() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar cambios")
                    }
                }

                mensaje?.let {
                    Text(
                        text = it,
                        color = if (it.contains("exito", true))
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
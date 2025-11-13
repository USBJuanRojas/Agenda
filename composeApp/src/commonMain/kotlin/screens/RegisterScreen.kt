package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import kotlinx.coroutines.launch

class RegisterScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current
        var name by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var user by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var responseMessage by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Regístrate", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFF751F), // Naranja
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navigator?.push(LoginScreen()) }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    colors = TextFieldDefaults.colors(
                        cursorColor = Color.DarkGray,
                        focusedIndicatorColor = Color(0xFFFF751F),
                        focusedLabelColor = Color(0xFFFF751F)
                    )
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Apellido") },
                    colors = TextFieldDefaults.colors(
                        cursorColor = Color.DarkGray,
                        focusedIndicatorColor = Color(0xFFFF751F),
                        focusedLabelColor = Color(0xFFFF751F)
                    )
                )
                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Usuario") },
                    colors = TextFieldDefaults.colors(
                        cursorColor = Color.DarkGray,
                        focusedIndicatorColor = Color(0xFFFF751F),
                        focusedLabelColor = Color(0xFFFF751F)
                    )
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail") },
                    colors = TextFieldDefaults.colors(
                        cursorColor = Color.DarkGray,
                        focusedIndicatorColor = Color(0xFFFF751F),
                        focusedLabelColor = Color(0xFFFF751F)
                    )
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    colors = TextFieldDefaults.colors(
                        cursorColor = Color.DarkGray,
                        focusedIndicatorColor = Color(0xFFFF751F),
                        focusedLabelColor = Color(0xFFFF751F)
                    )
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirma Contraseña") },
                    colors = TextFieldDefaults.colors(
                        cursorColor = Color.DarkGray,
                        focusedIndicatorColor = Color(0xFFFF751F),
                        focusedLabelColor = Color(0xFFFF751F)
                    )
                )


                Button(
                    onClick = {
                        scope.launch {
                            try {
                                if (password != confirmPassword) {
                                    responseMessage = "Las contraseñas no coinciden"
                                    return@launch
                                }
                                val client = HttpClient(CIO)
                                val response: String =
                                    client.post("http://10.0.2.2/API/crearUsuario.php") {
                                        contentType(ContentType.Application.FormUrlEncoded)
                                        setBody(
                                            Parameters.build {
                                                append("nombre", name.toString())
                                                append("apellido", lastName.toString())
                                                append("correo", email.toString())
                                                append("user", user.toString())
                                                append("password", password.toString())
                                            }.formUrlEncode()
                                        )
                                    }.bodyAsText()
                                responseMessage = "Respuesta: $response"
                                client.close()
                                navigator?.push(LoginScreen())
                            } catch (e: Exception) {
                                responseMessage = "Error: ${e.message}"
                            }
                        }
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF751F),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar")
                }//cambios

                if (responseMessage.isNotEmpty()) {
                    Text(responseMessage, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
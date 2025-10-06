package screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import bottombar.BottomBarScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import modelo.Objlogin

class LoginScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var user by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf(false) }

        val client = remember { HttpClient(CIO) }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Inicia Sesión") }) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Usuario") }
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    }
                )

                Button(onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response: HttpResponse = client.get("http://10.0.0.2/API/login.php") {
                                url {
                                    parameters.append("user", user)
                                    parameters.append("password", password) // 👈 debe coincidir con PHP
                                }
                            }



                            val responseText = response.bodyAsText()
                            println("Respuesta del servidor: $responseText")

                            val json = Json.parseToJsonElement(responseText).jsonObject
                            val success = json["success"]?.jsonPrimitive?.booleanOrNull ?: false

                            if (success) {
                                Objlogin.idUsu = json["user"]?.jsonPrimitive?.content ?: ""
                                Objlogin.nomUsu = json["nombre"]?.jsonPrimitive?.content ?: ""
                                Objlogin.apeUsu = json["apellido"]?.jsonPrimitive?.content ?: ""
                                Objlogin.perfil = json["idRol"]?.jsonPrimitive?.content ?: ""

                                withContext(Dispatchers.Main) {
                                    navigator.push(BottomBarScreen())
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    loginError = true
                                }
                            }

                        } catch (e: Exception) {
                            println("Error: ${e.message}")
                            withContext(Dispatchers.Main) {
                                loginError = true
                            }
                        }
                    }
                }) {
                    Text("Iniciar Sesión")
                }

                if (loginError) {
                    Text("Usuario o contraseña incorrectos", color = MaterialTheme.colorScheme.error)
                }

                TextButton(onClick = { navigator.push(RegisterScreen()) }) {
                    Text("Registrarse")
                }
            }
        }
    }
}

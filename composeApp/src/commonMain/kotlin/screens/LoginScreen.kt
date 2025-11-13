package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import bottombar.BottomBarScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inicia Sesión", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFF751F), // Naranja
                        titleContentColor = Color.White
                    )
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
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image =
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    }, colors = TextFieldDefaults.colors(
                        cursorColor = Color.DarkGray,
                        focusedIndicatorColor = Color(0xFFFF751F),
                        focusedLabelColor = Color(0xFFFF751F)
                    )
                )

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val response: HttpResponse =
                                    client.get("http://10.0.2.2/API/login.php") {
                                        url {
                                            parameters.append("user", user)
                                            parameters.append("password", password)
                                        }
                                    }

                                val responseText = response.bodyAsText()
                                println("Respuesta: $responseText")

                                val json = Json.parseToJsonElement(responseText).jsonObject
                                val success = json["success"]?.jsonPrimitive?.booleanOrNull ?: false

                                if (success) {
                                    // Guardar datos en el objeto global
                                    Objlogin.idUsu = json["idUsu"]?.jsonPrimitive?.content ?: ""
                                    Objlogin.nomUsu = json["nomUsu"]?.jsonPrimitive?.content ?: ""
                                    Objlogin.apeUsu = json["apeUsu"]?.jsonPrimitive?.content ?: ""
                                    Objlogin.perfil = json["perfil"]?.jsonPrimitive?.content ?: ""

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
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF751F),
                        contentColor = Color.White
                    )
                ) {
                    Text("Iniciar Sesión")
                }

                if (loginError) {
                    Text(
                        "Usuario o contraseña incorrectos",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                TextButton(onClick = { navigator.push(RegisterScreen()) }) {
                    Text("Registrarse", color = Color(0xFFFF751F))
                }
            }
        }
    }
}

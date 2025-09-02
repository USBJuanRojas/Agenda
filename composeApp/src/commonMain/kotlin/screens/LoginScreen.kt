package screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.jjrn.Agenda.User

class LoginScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var user by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val usersDB = remember {
            mutableStateListOf(
                User( "Administrador", "admin","admin@example.com", "1234"),
                User( "Nicolas", "nico","nicolas@example.com", "nico"),
                User( "Juan Jose", "juanjo","jjrojas@example.com", "juanjo")
            )
        }
        var passwordVisible by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Inicia Sesión") })
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
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Usuario") })

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
                        val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )

                Button(onClick = {
                    val usuarioValido = usersDB.any { it.user == user && it.password == password }
                    if (usuarioValido) {
                        navigator.push(BottomBarScreen())
                    } else {
                        loginError = true
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

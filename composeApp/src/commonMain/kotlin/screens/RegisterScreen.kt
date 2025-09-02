package screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
class RegisterScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current
        var name by remember { mutableStateOf("") }
        var user by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

    Scaffold(
    topBar = {
        TopAppBar(title = { Text("Regístrate") })
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
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
            OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Usuario") })
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-Mail") })
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirma Contraseña") })


            Button(onClick = { navigator?.push(LoginScreen()) }) { Text("Regístrarse") } //Por ahora se simula
            TextButton(onClick = { navigator?.push(LoginScreen()) }) { Text("Cancelar") } //Por ahora se simula
            }
        }
    }
}

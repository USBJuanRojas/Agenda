package screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bottombar.BottomBarScreen
import bottombar.HomeTab
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class AddClassScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var hora by remember { mutableStateOf("") }
        var lugar by remember { mutableStateOf("") }
        var asignatura by remember { mutableStateOf("") }
        var profesor by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Agregar Clase") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(BottomBarScreen()) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = hora, onValueChange = { hora = it }, label = { Text("Hora") })
                OutlinedTextField(value = lugar, onValueChange = { lugar = it }, label = { Text("Lugar") })
                OutlinedTextField(value = asignatura, onValueChange = { asignatura = it }, label = { Text("Asignatura") })
                OutlinedTextField(value = profesor, onValueChange = { profesor = it }, label = { Text("Profesor") })

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    navigator.pop() // Por ahora solo vuelve
                }) {
                    Text("Guardar (simulado)")
                }
            }
        }
    }
}

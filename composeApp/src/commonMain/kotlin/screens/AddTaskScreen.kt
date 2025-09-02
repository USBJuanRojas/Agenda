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

class AddTaskScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        var clase by remember { mutableStateOf("") }
        var fecha by remember { mutableStateOf("") }
        var nombre by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Agregar Tarea") },
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
                OutlinedTextField(value = clase, onValueChange = { clase = it }, label = { Text("Clase") })
                OutlinedTextField(value = fecha, onValueChange = { fecha = it }, label = { Text("Fecha") })
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    navigator.push(BottomBarScreen()) // Por ahora solo vuelve
                }) {
                    Text("Guardar (simulado)")
                }
            }
        }
    }
}

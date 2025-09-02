package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class ViewScheduleScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val horarioEjemplo = listOf(
            Clase("08:00 - 09:30", "Aula 101", "Matemáticas", "Prof. López"),
            Clase("09:40 - 11:10", "Aula 202", "Historia", "Prof. Ramírez"),
            Clase("11:20 - 12:50", "Aula 303", "Programación", "Prof. Torres")
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Horario de Clases") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(LoginScreen()) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(horarioEjemplo) { clase ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(text = clase.asignatura, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Hora: ${clase.hora}")
                            Text(text = "Lugar: ${clase.lugar}")
                            Text(text = "Profesor: ${clase.profesor}")
                        }
                    }
                }
            }
        }
    }
}

data class Clase(val hora: String, val lugar: String, val asignatura: String, val profesor: String)

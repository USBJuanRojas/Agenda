package bottombar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.jjrn.Agenda.Clase
import screens.LoginScreen

object HomeTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember {
                TabOptions(
                    index = 0u,
                    title = "Horario de Clases",
                    icon = icon
                )
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var showDialog by remember { mutableStateOf(false) }
        var claseDelete by remember { mutableStateOf<Clase?>(null) }
        var horarioEjemplo by remember {
            mutableStateOf(
                mutableListOf(
                    Clase("08:00 - 09:30", "Aula 101", "Matemáticas", "Prof. López"),
                    Clase("09:40 - 11:10", "Aula 202", "Historia", "Prof. Ramírez"),
                    Clase("11:20 - 12:50", "Aula 303", "Programación", "Prof. Torres")
                )
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Horario de Clases") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(LoginScreen()) }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    })
            }) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (horarioEjemplo.isEmpty()) {
                    item {
                        Text(
                            text = "No hay clases programadas",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(horarioEjemplo) { clase ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = clase.asignatura,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(text = "Hora: ${clase.hora}")
                                Text(text = "Lugar: ${clase.lugar}")
                                Text(text = "Profesor: ${clase.profesor}")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(onClick = {
                                        /*navigator.push() */
                                    }) { Text(text = "Editar") }
                                    Button(onClick = {
                                        claseDelete = clase
                                        showDialog = true
                                    }) {
                                        Text(text = "Eliminar")
                                    }
                                }
                            }

                            if (showDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDialog = false },
                                    title = { Text("Confirmar eliminación") },
                                    text = { Text("¿Seguro que deseas eliminar la clase ${claseDelete?.asignatura}?") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            horarioEjemplo = horarioEjemplo.toMutableList()
                                                .apply { remove(claseDelete) }
                                            showDialog = false
                                        }) { Text("Eliminar") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            showDialog = false
                                        }) { Text("Cancelar") }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
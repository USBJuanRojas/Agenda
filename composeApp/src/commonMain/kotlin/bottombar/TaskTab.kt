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
import androidx.compose.material.icons.filled.Checklist
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
import org.jjrn.Agenda.Tarea
import screens.LoginScreen

object TaskTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Checklist)
            return remember {
                TabOptions(
                    index = 2u,
                    title = "Tareas",
                    icon = icon
                )
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        var showDialog by remember { mutableStateOf(false) }
        var tareaDelete by remember { mutableStateOf<Tarea?>(null) }
        var tareaEjemplo by remember {
            mutableStateOf(
                mutableListOf(
                    Tarea("Matematicas", "5/09/2025", "Taller 4"),
                    Tarea("Programacion", "4/09/2025", "Proyecto 2")
                )
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tareas Pendientes") },
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
                if (tareaEjemplo.isEmpty()) {
                    item {
                        Text(
                            text = "No hay tareas pendientes",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(tareaEjemplo) { tarea ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = tarea.clase,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(text = "Fecha: ${tarea.fecha}")
                                Text(text = "Lugar: ${tarea.nombre}")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(onClick = {
                                        //navigator.push()
                                    }) {
                                        Text(text = "Editar")
                                    }
                                    Button(onClick = {
                                        tareaDelete = tarea
                                        showDialog = true
                                    }) {
                                        Text(text = "Completada")
                                    }
                                }
                            }
                            if (showDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDialog = false },
                                    title = { Text("¡Tarea Completada!") },
                                    text = { Text("Se eliminará la tarea de ${tareaDelete?.clase} de la lista, confirmar?") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            tareaEjemplo = tareaEjemplo.toMutableList()
                                                .apply { remove(tareaDelete) }
                                            showDialog = false
                                        }) { Text("Confirmar") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDialog = false }) {
                                            Text("Cancelar")
                                        }
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
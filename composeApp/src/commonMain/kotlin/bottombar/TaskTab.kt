package bottombar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import modelo.Objlogin
import screens.LoginScreen

// MODELOS DE DATOS
@Serializable
data class Tarea(
    val id_tarea: Int,
    val asunto: String,
    val descripcion: String,
    val fecha_inicio: String,
    val fecha_fin: String,
    val observaciones: String,
    val nombre_clase: String
)

@Serializable
data class TareasResponse(
    val success: Boolean,
    val tareas: List<Tarea>? = null,
    val message: String? = null
)

@Serializable
data class Clase(
    val id_clase: Int,
    val nombre_clase: String
)

@Serializable
data class ClasesResponse(
    val success: Boolean,
    val clases: List<Clase>? = null,
    val message: String? = null
)

data class ClaseConTareas(
    val nombreClase: String,
    val tareas: List<Tarea>
)

// FUNCIÓN PARA CARGAR TAREAS POR CLASE
suspend fun cargarTareasPorClase(idProfesor: Int): List<ClaseConTareas> {
    val client = HttpClient()
    val json = Json { ignoreUnknownKeys = true }
    val listaFinal = mutableListOf<ClaseConTareas>()

    try {
        // 1️⃣ Obtener clases del profesor
        val clasesUrl = "http://10.0.2.2/API/obtenerClasesUsuario.php?id_usuario=$idProfesor"
        val clasesResponseText = client.get(clasesUrl).body<String>()
        val clasesResponse = json.decodeFromString<ClasesResponse>(clasesResponseText)

        if (clasesResponse.success && !clasesResponse.clases.isNullOrEmpty()) {
            for (clase in clasesResponse.clases) {
                // 2️⃣ Obtener tareas por clase
                val tareasUrl = "http://10.0.2.2/API/obtererTareasClase.php?id_clase=${clase.id_clase}"
                val tareasResponseText = client.get(tareasUrl).body<String>()
                val tareasResponse = json.decodeFromString<TareasResponse>(tareasResponseText)

                if (tareasResponse.success && !tareasResponse.tareas.isNullOrEmpty()) {
                    listaFinal.add(
                        ClaseConTareas(
                            nombreClase = clase.nombre_clase,
                            tareas = tareasResponse.tareas
                        )
                    )
                } else {
                    // Clase sin tareas
                    listaFinal.add(ClaseConTareas(clase.nombre_clase, emptyList()))
                }
            }
        }
    } catch (e: Exception) {
        println("Error cargando tareas: ${e.message}")
    } finally {
        client.close()
    }

    return listaFinal
}

// INTERFAZ PRINCIPAL
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
        var cargando by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var clasesConTareas by remember { mutableStateOf<List<ClaseConTareas>>(emptyList()) }

        val scope = rememberCoroutineScope()

        fun recargarTareas() {
            scope.launch {
                cargando = true
                error = null
                try {
                    clasesConTareas = cargarTareasPorClase(Objlogin.idUsu.toInt())
                    if (clasesConTareas.isEmpty()) {
                        error = "No tienes clases asignadas o no hay tareas registradas."
                    }
                } catch (e: Exception) {
                    error = "Error de conexión: ${e.message}"
                } finally {
                    cargando = false
                }
            }
        }

        LaunchedEffect(Unit) {
            recargarTareas()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tareas por Clase") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(LoginScreen()) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        TextButton(onClick = { recargarTareas() }) {
                            Text("Actualizar")
                        }
                    }
                )
            }
        ) { padding ->
            when {
                cargando -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Cargando tareas...",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error ?: "",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        clasesConTareas.forEach { claseConTareas ->
                            item {
                                Text(
                                    text = claseConTareas.nombreClase,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            if (claseConTareas.tareas.isEmpty()) {
                                item {
                                    Text(
                                        text = "Sin tareas registradas.",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                items(claseConTareas.tareas) { tarea ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(
                                                text = tarea.asunto,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(text = "Descripción: ${tarea.descripcion}")
                                            Text(text = "Inicio: ${tarea.fecha_inicio}")
                                            Text(text = "Fin: ${tarea.fecha_fin}")
                                            Text(text = "Observaciones: ${tarea.observaciones}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

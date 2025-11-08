package bottombar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import modelo.Objlogin

@Serializable
data class Clase(
    @SerialName("id_clase") val idClase: Int,
    @SerialName("nombre_clase") val nombreClase: String
)

@Serializable
data class ClaseResponse(
    val success: Boolean,
    val clases: List<Clase>? = null,
    val message: String? = null
)

@Serializable
data class Tarea(
    @SerialName("id_tarea") val idTarea: Int,
    @SerialName("id_clase") val idClase: Int,
    @SerialName("asunto") val asunto: String,
    @SerialName("descripcion") val descripcion: String,
    @SerialName("fecha_inicio") val fechaInicio: String,
    @SerialName("fecha_fin") val fechaFin: String,
    @SerialName("observaciones") val observaciones: String? = null,
    @SerialName("nombre_clase") val nombreClase: String
)

@Serializable
data class TareaResponse(
    val success: Boolean,
    val tareas: List<Tarea>? = null,
    val message: String? = null
)

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
        val json = Json { ignoreUnknownKeys = true }
        val client = remember { HttpClient(CIO) }

        var clases by remember { mutableStateOf<List<Clase>>(emptyList()) }
        var tareasPorClase by remember { mutableStateOf<Map<Int, List<Tarea>>>(emptyMap()) }
        var mensaje by remember { mutableStateOf<String?>(null) }

        // Cargar clases y tareas
        LaunchedEffect(Unit) {
            try {
                val clasesResponseText = client.get("http://10.0.2.2/API/obtenerClasesUsuario.php?id_usuario=${Objlogin.idUsu}").body<String>()
                val clasesResponse = json.decodeFromString<ClaseResponse>(clasesResponseText)

                if (clasesResponse.success && clasesResponse.clases != null) {
                    clases = clasesResponse.clases

                    val mapaTemporal = mutableMapOf<Int, List<Tarea>>()
                    for (clase in clases) {
                        val tareasResponseText = client.get("http://10.0.2.2/API/obtenerTareasClase.php?id_clase=${clase.idClase}").body<String>()
                        val tareasResponse = json.decodeFromString<TareaResponse>(tareasResponseText)
                        mapaTemporal[clase.idClase] = tareasResponse.tareas ?: emptyList()
                    }
                    tareasPorClase = mapaTemporal
                } else {
                    mensaje = clasesResponse.message ?: "No se encontraron clases"
                }
            } catch (e: Exception) {
                mensaje = "Error de conexión: ${e.message}"
                e.printStackTrace()
            } finally {
                client.close()
            }
        }

        //Interfaz
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tareas") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { padding ->
            if (mensaje != null) {
                Text(
                    text = mensaje ?: "",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else if (clases.isEmpty()) {
                Text(
                    text = "No tienes clases asignadas.",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(clases) { clase ->
                        Column {
                            Text(
                                text = clase.nombreClase,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            val tareasClase = tareasPorClase[clase.idClase] ?: emptyList()

                            if (tareasClase.isEmpty()) {
                                Text(
                                    text = "No hay tareas registradas para esta clase.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            } else {
                                tareasClase.forEach { tarea ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        elevation = CardDefaults.cardElevation(6.dp)
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(
                                                text = tarea.asunto,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text("Descripción: ${tarea.descripcion}")
                                            Text("Inicio: ${tarea.fechaInicio} | Fin: ${tarea.fechaFin}")
                                            tarea.observaciones?.let {
                                                Text("Obs: $it")
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
}

package bottombar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import modelo.Clase
import modelo.Objlogin
import screens.EditTaskScreen

@Serializable
data class TareaSimple(
    val id_tarea: Int,
    val id_clase: Int,
    val asunto: String,
    val descripcion: String,
    val fecha_inicio: String,
    val fecha_fin: String,
    val observaciones: String? = null,
    val nombre_clase: String? = null
)

@Serializable
data class TareaResponseWrapper(
    val success: Boolean,
    val tareas: List<TareaSimple>? = null,
    val message: String? = null
)

object TaskTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Checklist)
            return remember {
                TabOptions(
                    index = 2u,
                    title = if (Objlogin.perfil == "Profesor") {"Gestión de Tareas"} else {"Tareas por Clase"},
                    icon = icon
                )
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var clases by remember { mutableStateOf<List<Clase>>(emptyList()) }
        var tareasPorClase by remember { mutableStateOf<Map<Int, List<TareaSimple>>>(emptyMap()) }
        var cargando by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var showDialog by remember { mutableStateOf(false) }
        var tareaDelete by remember { mutableStateOf<TareaSimple?>(null) }

        val json = Json { ignoreUnknownKeys = true; isLenient = true }

        // Cargar clases y tareas por clase
        fun cargarClasesYtareas() {
            scope.launch {
                cargando = true
                error = null
                val client = HttpClient()
                try {
                    val listarUrl = if (Objlogin.perfil == "Administrador") {
                        "http://10.0.2.2/API/listarClase.php"
                    } else {
                        "http://10.0.2.2/API/obtenerClasesUsuario.php?id_usuario=${Objlogin.idUsu}"
                    }

                    val clasesText = client.get(listarUrl).bodyAsText()
                    // Reusar tu wrapper si tienes uno diferente; aquí solo decodificamos clases en modelo.Clase
                    // El objeto Clase ya viene del paquete modelo (lo importaste)
                    val clasesResp = json.decodeFromString(ClasesResponse.serializer(), clasesText)
                    if (clasesResp.success && clasesResp.clases != null) {
                        clases = clasesResp.clases

                        // Para cada clase, solicitar sus tareas
                        val mapaTemporal = mutableMapOf<Int, List<TareaSimple>>()
                        for (c in clases) {
                            try {
                                val tareasText = client.get("http://10.0.2.2/API/obtenerTareasClase.php?id_clase=${c.id_clase}").bodyAsText()
                                val tareasResp = json.decodeFromString(TareaResponseWrapper.serializer(), tareasText)
                                mapaTemporal[c.id_clase] = tareasResp.tareas ?: emptyList()
                            } catch (e: Exception) {
                                // Si falla una clase, dejar lista vacía y continuar
                                mapaTemporal[c.id_clase] = emptyList()
                            }
                        }
                        tareasPorClase = mapaTemporal
                    } else {
                        error = clasesResp.message ?: "No se encontraron clases"
                        clases = emptyList()
                        tareasPorClase = emptyMap()
                    }
                } catch (e: Exception) {
                    error = "Error de conexión: ${e.message}"
                    clases = emptyList()
                    tareasPorClase = emptyMap()
                } finally {
                    client.close()
                    cargando = false
                }
            }
        }

        // Eliminar tarea (usa el modelo de tu PHP que devuelve success/message)
        fun eliminarTarea(tarea: TareaSimple) {
            scope.launch {
                val client = HttpClient()
                try {
                    val responseText = client.get("http://10.0.2.2/API/eliminarTarea.php?id_tarea=${tarea.id_tarea}").bodyAsText()
                    val parsed = json.parseToJsonElement(responseText).jsonObject
                    val success = parsed["success"]?.jsonPrimitive?.booleanOrNull ?: false
                    val message = parsed["message"]?.jsonPrimitive?.content ?: "Respuesta inesperada"

                    if (success) {
                        // actualizar estado local
                        tareasPorClase = tareasPorClase.toMutableMap().also { map ->
                            val lista = map[tarea.id_clase]?.filterNot { it.id_tarea == tarea.id_tarea }
                            map[tarea.id_clase] = lista ?: emptyList()
                        }
                    } else {
                        error = "Error al eliminar: $message"
                    }
                } catch (e: Exception) {
                    error = "Error de conexión: ${e.message}"
                } finally {
                    client.close()
                }
            }
        }

        LaunchedEffect(Unit) { cargarClasesYtareas() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tareas") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(screens.LoginScreen()) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 100.dp
                )
            ) {
                when {
                    cargando -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFFF751F),)
                            }
                        }
                    }

                    error != null -> {
                        item {
                            Text(
                                text = error ?: "Error desconocido",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    clases.isEmpty() -> {
                        item {
                            Text(
                                text = "No hay clases asignadas.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        // Por cada clase mostramos su título y sus tareas (si las hay)
                        items(clases) { clase ->
                            Column {
                                Text(
                                    text = clase.nombre_clase,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                val tareasClase = tareasPorClase[clase.id_clase] ?: emptyList()

                                if (tareasClase.isEmpty()) {
                                    Text(
                                        text = "No hay tareas registradas para esta clase.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                } else {
                                    // Listado de tareas
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
                                                val horaInicio = tarea.fecha_inicio.split("-")[1]
                                                val horaFin = tarea.fecha_fin.split("-")[1]
                                                val diaInicio = tarea.fecha_inicio.split("-")[0]
                                                val diaFin = tarea.fecha_fin.split("-")[0]
                                                Text("Fecha inicio: $diaInicio")
                                                Text("Fecha fin: $diaFin")
                                                Text("Hora inicio: $horaInicio | Hora fin: $horaFin")
                                                //Text("Inicio: ${tarea.fecha_inicio} | Fin: ${tarea.fecha_fin}")
                                                tarea.observaciones?.let {
                                                    Text("Obs: $it")
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    if (Objlogin.perfil == "Profesor") {
                                                        OutlinedButton(onClick = {
                                                            // Navegar a editar tarea (usa navigator.parent para mantener comportamiento parecido al HomeTab)
                                                            navigator.parent?.push(
                                                                EditTaskScreen(
                                                                    modelo.Tarea(
                                                                        idTarea = tarea.id_tarea,
                                                                        idClase = tarea.id_clase,
                                                                        asunto = tarea.asunto,
                                                                        descripcion = tarea.descripcion,
                                                                        fechaInicio = tarea.fecha_inicio,
                                                                        fechaFin = tarea.fecha_fin,
                                                                        observaciones = tarea.observaciones,
                                                                        nombreClase = tarea.nombre_clase ?: ""
                                                                    )
                                                                )
                                                            )
                                                        }) {
                                                            Text("Editar")
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Button(onClick = {
                                                            tareaDelete = tarea
                                                            showDialog = true
                                                        }) {
                                                            Text("Eliminar")
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

            // Diálogo de confirmación de eliminación
            if (showDialog && tareaDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmar eliminación") },
                    text = {
                        Text("¿Seguro que deseas eliminar la tarea \"${tareaDelete?.asunto}\"?")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            tareaDelete?.let { eliminarTarea(it) }
                            showDialog = false
                        }) {
                            Text("Eliminar")
                        }
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

    @Serializable
    data class ClasesResponse(
        val success: Boolean,
        val clases: List<Clase>? = null,
        val message: String? = null
    )
}

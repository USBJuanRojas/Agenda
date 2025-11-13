package bottombar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
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
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import modelo.Clase
import modelo.Objlogin
import screens.EditClassScreen
import screens.LoginScreen
import screens.ManageStudentsClassScreen

object HomeTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember {
                TabOptions(
                    index = 0u,
                    title = if (Objlogin.perfil == "Administrador") {"Gestion de Clases"} else {"Horario de Clases"},
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
        var cargando by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var showDialog by remember { mutableStateOf(false) }
        var claseDelete by remember { mutableStateOf<Clase?>(null) }

        val json = Json { ignoreUnknownKeys = true; isLenient = true }

        // --- FUNCIONES ---

        fun cargarClases() {
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

                    val responseText = client.get(listarUrl).bodyAsText()
                    val response = json.decodeFromString<ClasesResponse>(responseText)

                    clases = if (response.success && response.clases != null) {
                        response.clases
                    } else {
                        error = response.message ?: "No se encontraron clases"
                        emptyList()
                    }

                    if (clases.isNotEmpty()) {
                        clases = clases.map { clase ->
                            try {
                                // Obtener horario de la clase
                                val diasResponseText = client.get("http://10.0.2.2/API/obtenerHorarioClase.php?id_clase=${clase.id_clase}")
                                    .bodyAsText()
                                val diasArray = json.parseToJsonElement(diasResponseText).jsonArray

                                if (diasArray.isEmpty()) {
                                    // 🚀 Crear nuevo horario vacío si no existe
                                    val createResponse = client.post("http://10.0.2.2/API/guardarHorarioClase.php") {
                                        contentType(io.ktor.http.ContentType.Application.Json)
                                        setBody("""{"id_clase": ${clase.id_clase}, "dias_semana": ""}""")
                                    }.bodyAsText()

                                    println("Horario creado para clase ${clase.id_clase}: $createResponse")

                                    clase.copy(dias_semana = "")
                                } else {
                                    // ✅ Tomar el primer horario retornado
                                    val diasObj = diasArray.first().jsonObject
                                    val diasString = diasObj["dias_semana"]?.jsonPrimitive?.content ?: ""
                                    clase.copy(dias_semana = diasString)
                                }
                            } catch (e: Exception) {
                                println("Error al cargar horario de clase ${clase.id_clase}: ${e.message}")
                                clase.copy(dias_semana = "")
                            }
                        }
                    }

                } catch (e: Exception) {
                    error = "Error de conexión: ${e.message}"
                } finally {
                    client.close()
                    cargando = false
                }
            }
        }



        fun eliminarClase(clase: Clase) {
            scope.launch {
                val client = HttpClient()
                try {
                    val responseText =
                        client.get("http://10.0.2.2/API/eliminarClase.php?id_clase=${clase.id_clase}")
                            .bodyAsText()
                    val jsonResponse = json.parseToJsonElement(responseText).jsonObject

                    val status = jsonResponse["status"]?.jsonPrimitive?.content
                    val message = jsonResponse["message"]?.jsonPrimitive?.content ?: ""

                    if (status == "success") {
                        clases = clases.filterNot { it.id_clase == clase.id_clase }
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

        // --- CARGA AUTOMÁTICA ---
        LaunchedEffect(Unit) { cargarClases() }

        // --- UI PRINCIPAL ---
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
                                text = "No hay clases programadas",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        items(clases) { clase ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = clase.nombre_clase,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    val diasLegibles = when (clase.dias_semana) {
                                        null, "", "0000000" -> "Sin días asignados"
                                        else -> clase.dias_semana!!.map { c ->
                                            when (c) {
                                                'L' -> "Lunes"
                                                'M' -> "Martes"
                                                'X' -> "Miércoles"
                                                'J' -> "Jueves"
                                                'V' -> "Viernes"
                                                'S' -> "Sábado"
                                                'D' -> "Domingo"
                                                else -> null
                                            }
                                        }.filterNotNull().joinToString(", ")
                                    }

                                    Text(text = "Días: $diasLegibles")


                                    Text(text = "Hora: ${clase.hora_inicio} - ${clase.hora_fin}")
                                    Text(text = "Lugar: ${clase.lugar}")

                                        if (clase.profesor_nombre == null && clase.profesor_apellido == null) {
                                            Text(text = "Profesor: Sin asignar")
                                        } else {
                                            Text(text = "Profesor: ${clase.profesor_nombre} ${clase.profesor_apellido}")
                                        }


                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (Objlogin.perfil == "Administrador") {
                                            OutlinedButton(onClick = {
                                                navigator.parent?.push(
                                                    EditClassScreen(clase)
                                                )
                                            }) {
                                                Text("Editar")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(onClick = {
                                                claseDelete = clase
                                                showDialog = true
                                            }) {
                                                Text("Eliminar")
                                            }
                                        } else if (Objlogin.perfil == "Profesor") {
                                            Button(onClick = {
                                                navigator.parent?.push(
                                                    ManageStudentsClassScreen(clase.id_clase)
                                                )
                                            }) {
                                                Text("Gestionar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Diálogo de confirmación ---
            if (showDialog && claseDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmar eliminación") },
                    text = {
                        Text("¿Seguro que deseas eliminar la clase ${claseDelete?.nombre_clase}?")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            claseDelete?.let { eliminarClase(it) }
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



    // Wrapper para deserialización
    @Serializable
    data class ClasesResponse(
        val success: Boolean,
        val clases: List<Clase>? = null,
        val message: String? = null
    )
}

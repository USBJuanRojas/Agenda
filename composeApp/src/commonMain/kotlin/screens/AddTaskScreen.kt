package screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bottombar.BottomBarScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import modelo.Objlogin // Usa tu modelo actual de sesión

@Serializable
data class Clase(
    @SerialName("id_clase") val id: Int,
    @SerialName("nombre_clase") val nombre: String
)

class AddTaskScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        // --- Campos de tarea ---
        var asunto by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        var fechaInicio by remember { mutableStateOf("") }
        var fechaFin by remember { mutableStateOf("") }
        var observaciones by remember { mutableStateOf("") }

        // --- Clases asociadas al profesor ---
        var clases by remember { mutableStateOf<List<Clase>>(emptyList()) }
        var selectedClase by remember { mutableStateOf<Clase?>(null) }
        var expanded by remember { mutableStateOf(false) }

        var responseStatus by remember { mutableStateOf("") }

        // Cargar clases al iniciar
        LaunchedEffect(Unit) {
            scope.launch {
                try {
                    val client = HttpClient(CIO)
                    val listarUrl = "http://10.0.2.2/API/obtenerClasesUsuario.php?id_usuario=${Objlogin.idUsu}"
                    val responseText = client.get(listarUrl).bodyAsText()
                    val json = Json { ignoreUnknownKeys = true }
                    val parsed = json.decodeFromString<ClasesResponse>(responseText)
                    if (parsed.success) {
                        clases = parsed.clases ?: emptyList()
                    } else {
                        responseStatus = parsed.message ?: "Error al cargar clases"
                    }
                    client.close()
                } catch (e: Exception) {
                    responseStatus = "Error de conexión: ${e.message}"
                }
            }
        }

        fun crearTarea() {
            scope.launch {
                try {
                    val client = HttpClient(CIO)
                    val response = client.post("http://10.0.2.2/API/crearTarea.php") {
                        contentType(ContentType.Application.FormUrlEncoded)
                        setBody(
                            Parameters.build {
                                append("id_clase", selectedClase?.id.toString())
                                append("asunto", asunto)
                                append("descripcion", descripcion)
                                append("fecha_inicio", fechaInicio)
                                append("fecha_fin", fechaFin)
                                append("observaciones", observaciones)
                            }.formUrlEncode()
                        )
                    }.bodyAsText()

                    responseStatus = "Respuesta: $response"
                    client.close()
                    navigator.push(BottomBarScreen())

                } catch (e: Exception) {
                    responseStatus = "Error: ${e.message}"
                }
            }
        }

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

                // --- Selección de clase ---
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedClase?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Clase") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        clases.forEach { clase ->
                            DropdownMenuItem(
                                text = { Text(clase.nombre) },
                                onClick = {
                                    selectedClase = clase
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = asunto, onValueChange = { asunto = it }, label = { Text("Asunto") })
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
                OutlinedTextField(value = fechaInicio, onValueChange = { fechaInicio = it }, label = { Text("Fecha Inicio (ej. 8-11)") })
                OutlinedTextField(value = fechaFin, onValueChange = { fechaFin = it }, label = { Text("Fecha Fin (ej. 11-11)") })
                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") })

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { crearTarea() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar")
                }

                if (responseStatus.isNotEmpty()) {
                    Text(responseStatus, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Serializable
data class ClasesResponse(
    val success: Boolean,
    val clases: List<Clase>? = null,
    val message: String? = null
)

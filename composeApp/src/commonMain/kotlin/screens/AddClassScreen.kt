package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
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
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

@Serializable
data class Profesor(
    @SerialName("id_usuario") val id: String,
    @SerialName("nombre_completo") val nombre: String,
    val correo: String
)

class AddClassScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        var className by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var startTime by remember { mutableStateOf("") }
        var endTime by remember { mutableStateOf("") }
        var place by remember { mutableStateOf("") }

        var profesores by remember { mutableStateOf<List<Profesor>>(emptyList()) }
        var selectedProfesor by remember { mutableStateOf<Profesor?>(null) }
        var expanded by remember { mutableStateOf(false) }

        // ✅ Días seleccionados
        val diasSemana = listOf(
            "L" to "Lun",
            "M" to "Mar",
            "X" to "Mie",
            "J" to "Jue",
            "V" to "Vie",
            "S" to "Sáb",
            "D" to "Dom"
        )

        val seleccionDias = remember { mutableStateMapOf<String, Boolean>() }
        diasSemana.forEach { if (seleccionDias[it.first] == null) seleccionDias[it.first] = false }

        var responseStatus by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        // Cargar profesores
        LaunchedEffect(Unit) {
            scope.launch {
                try {
                    val client = HttpClient(CIO)
                    val response = client.get("http://10.0.2.2/API/listarProfesor.php")
                    val json = response.bodyAsText()
                    profesores = Json.decodeFromString(json)
                    client.close()
                } catch (e: Exception) {
                    responseStatus = "Error cargando profesores: ${e.message}"
                }
            }
        }

        fun createClass() {
            scope.launch {
                try {
                    val client = HttpClient(CIO)

                    // --- 1️⃣ Crear clase ---
                    val responseClase = client.post("http://10.0.2.2/API/crearClase.php") {
                        contentType(ContentType.Application.FormUrlEncoded)
                        setBody(
                            Parameters.build {
                                append("nombre_clase", className)
                                append("descripcion", description)
                                append("hora_inicio", startTime)
                                append("hora_fin", endTime)
                                append("lugar", place)
                                append("id_profesor", selectedProfesor!!.id)
                            }.formUrlEncode()
                        )
                    }.bodyAsText()

                    // Parsear respuesta
                    val jsonResponse = Json.parseToJsonElement(responseClase).jsonObject
                    val success = jsonResponse["success"]?.toString()?.toBoolean() ?: false

                    if (!success) {
                        responseStatus = "Error al crear clase: ${jsonResponse["message"]}"
                        client.close()
                        return@launch
                    }

                    // Obtener id_clase de la respuesta
                    val idClase = jsonResponse["id_clase"]?.toString()?.toIntOrNull()
                    if (idClase == null) {
                        responseStatus = "No se recibió el ID de la clase."
                        client.close()
                        return@launch
                    }

                    // --- 2️⃣ Crear horario (con los días seleccionados) ---
                    val diasSeleccionados = seleccionDias.filterValues { it }.keys.joinToString("")

                    val responseHorario = client.post("http://10.0.2.2/API/crearHorario.php") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"id_clase": $idClase, "dias_semana": "$diasSeleccionados"}""")
                    }.bodyAsText()

                    responseStatus = "✅ Clase y horario creados exitosamente.\n$responseHorario"
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
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Nombre Clase") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") }
                )
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Tiempo Inicio") }
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Tiempo Fin") }
                )
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = { Text("Lugar") }
                )

                // --- Lista desplegable de profesores ---
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedProfesor?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Profesor") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        profesores.forEach { prof ->
                            DropdownMenuItem(
                                text = { Text(prof.nombre) },
                                onClick = {
                                    selectedProfesor = prof
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // ✅ Sección de selección de días
                Text("Días de clase:", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    diasSemana.forEach { (clave, label) ->
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            modifier = Modifier.toggleable(
                                value = seleccionDias[clave] ?: false,
                                onValueChange = { seleccionDias[clave] = it }
                            )
                        ) {
                            Checkbox(
                                checked = seleccionDias[clave] ?: false,
                                onCheckedChange = { seleccionDias[clave] = it }
                            )
                            Text(label)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { createClass() },
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

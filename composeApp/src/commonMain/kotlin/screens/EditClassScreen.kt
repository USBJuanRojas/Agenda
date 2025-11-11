package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bottombar.BottomBarScreen
import bottombar.HomeTab
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import modelo.Clase

class EditClassScreen(private val clase: Clase) : Screen {

    @Serializable
    data class Profesor(
        @SerialName("id_usuario") val id: String,
        @SerialName("nombre_completo") val nombre: String,
        val correo: String? = null
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        // 🔹 Datos iniciales de la clase
        var id by remember { mutableStateOf(clase.id_clase.toString()) }
        var className by remember { mutableStateOf(clase.nombre_clase) }
        var description by remember { mutableStateOf(clase.descripcion) }
        var startTime by remember { mutableStateOf(clase.hora_inicio) }
        var endTime by remember { mutableStateOf(clase.hora_fin) }
        var place by remember { mutableStateOf(clase.lugar) }

        // 🔹 Profesores
        var profesores by remember { mutableStateOf<List<Profesor>>(emptyList()) }
        var selectedProfesor by remember { mutableStateOf<Profesor?>(null) }
        var expanded by remember { mutableStateOf(false) }

        // 🔹 Horario
        var idHorario by remember { mutableStateOf<Int?>(null) }

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

        var cargando by remember { mutableStateOf(false) }
        var mensaje by remember { mutableStateOf<String?>(null) }

        // 🔹 Cargar profesores y horario de la clase
        LaunchedEffect(Unit) {
            val client = HttpClient(CIO)
            try {
                // Profesores
                val profesoresResponse = client.get("http://10.0.2.2/API/listarProfesor.php")
                val parsedProfesores = Json { ignoreUnknownKeys = true }
                    .decodeFromString<List<Profesor>>(profesoresResponse.bodyAsText())
                profesores = parsedProfesores
                selectedProfesor = parsedProfesores.find { it.id == clase.id_profesor.toString() }

                // Horario
                val horarioResponse = client.get("http://10.0.2.2/API/obtenerHorarioClase.php?id_clase=${clase.id_clase}")
                val horarios = Json { ignoreUnknownKeys = true }
                    .parseToJsonElement(horarioResponse.bodyAsText())
                    .jsonArray

                if (horarios.isNotEmpty()) {
                    val horario = horarios[0].jsonObject
                    idHorario = horario["id_horario"]?.toString()?.replace("\"", "")?.toIntOrNull()
                    val dias = horario["dias_semana"]?.toString()?.replace("\"", "") ?: ""
                    dias.forEach { letra ->
                        if (seleccionDias.containsKey(letra.toString())) {
                            seleccionDias[letra.toString()] = true
                        }
                    }
                }
            } catch (e: Exception) {
                mensaje = "Error al cargar datos: ${e.message}"
            } finally {
                client.close()
            }
        }

        // 🔹 Función para editar clase y horario
        fun editarClase() {
            scope.launch {
                if (selectedProfesor == null) {
                    mensaje = "Por favor selecciona un profesor antes de guardar."
                    return@launch
                }

                cargando = true
                mensaje = null
                val client = HttpClient(CIO)

                try {
                    // --- 1️⃣ Editar datos principales de la clase ---
                    val responseClase = client.submitForm(
                        url = "http://10.0.2.2/API/modificarClase.php",
                        formParameters = Parameters.build {
                            append("id_clase", id)
                            append("nombre_clase", className)
                            append("descripcion", description)
                            append("hora_inicio", startTime)
                            append("hora_fin", endTime)
                            append("lugar", place)
                            append("id_profesor", selectedProfesor!!.id)
                        }
                    ).bodyAsText()

                    val jsonClase = Json.parseToJsonElement(responseClase).jsonObject
                    val successClase = jsonClase["success"]?.toString()?.toBoolean() ?: false
                    if (!successClase) {
                        mensaje = "Error al editar clase: ${jsonClase["message"]}"
                        cargando = false
                        client.close()
                        return@launch
                    }

                    // --- 2️⃣ Editar horario ---
                    val diasSeleccionados = seleccionDias.filterValues { it }.keys.joinToString("")
                    if (diasSeleccionados.isNotEmpty() && idHorario != null) {
                        val responseHorario = client.post("http://10.0.2.2/API/editarHorario.php") {
                            contentType(ContentType.Application.Json)
                            setBody("""{"id_horario": $idHorario, "dias_semana": "$diasSeleccionados"}""")
                        }.bodyAsText()

                        val jsonHorario = Json.parseToJsonElement(responseHorario).jsonObject
                        val successHorario = jsonHorario["success"]?.toString()?.toBoolean() ?: false

                        if (!successHorario) {
                            mensaje = "Error al actualizar horario: ${jsonHorario["message"]}"
                            cargando = false
                            client.close()
                            return@launch
                        }
                    }

                    mensaje = "✅ Clase y horario actualizados correctamente."
                    navigator.push(BottomBarScreen(initialTab = HomeTab))

                } catch (e: Exception) {
                    mensaje = "Error de red: ${e.message}"
                } finally {
                    client.close()
                    cargando = false
                }
            }
        }

        // 🔹 UI
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar Clase") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(BottomBarScreen(initialTab = HomeTab)) }) {
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
                    label = { Text("Nombre Clase") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Hora Inicio") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Hora Fin") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = { Text("Lugar") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 🔹 Dropdown Profesores
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedProfesor?.nombre ?: "Seleccionar profesor",
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
                        profesores.forEach { profesor ->
                            DropdownMenuItem(
                                text = { Text(profesor.nombre) },
                                onClick = {
                                    selectedProfesor = profesor
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // 🔹 Días de clase (interfaz legible)
                Text("Días de clase:", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    diasSemana.forEach { (key, label) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.toggleable(
                                value = seleccionDias[key] ?: false,
                                onValueChange = { seleccionDias[key] = it }
                            )
                        ) {
                            Checkbox(
                                checked = seleccionDias[key] ?: false,
                                onCheckedChange = { seleccionDias[key] = it }
                            )
                            Text(label)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = { editarClase() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar cambios")
                    }
                }

                mensaje?.let {
                    Text(
                        text = it,
                        color = if (it.contains("correctamente", true))
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

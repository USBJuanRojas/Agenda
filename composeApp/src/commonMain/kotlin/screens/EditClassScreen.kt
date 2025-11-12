package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import modelo.Clase
import modelo.SimulatedTimePicker

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

        // =============================
        // 🔹 Estados
        // =============================
        var className by remember { mutableStateOf(clase.nombre_clase) }
        var description by remember { mutableStateOf(clase.descripcion) }
        var place by remember { mutableStateOf(clase.lugar) }
        var startTime by remember { mutableStateOf(clase.hora_inicio) }
        var endTime by remember { mutableStateOf(clase.hora_fin) }

        var profesores by remember { mutableStateOf<List<Profesor>>(emptyList()) }
        var selectedProfesor by remember { mutableStateOf<Profesor?>(null) }
        var expanded by remember { mutableStateOf(false) }

        var idHorario by remember { mutableStateOf<Int?>(null) }

        val diasSemana = listOf(
            "L" to "Lun", "M" to "Mar", "X" to "Mié",
            "J" to "Jue", "V" to "Vie", "S" to "Sáb", "D" to "Dom"
        )

        val seleccionDias = remember { mutableStateMapOf<String, Boolean>() }
        diasSemana.forEach { if (seleccionDias[it.first] == null) seleccionDias[it.first] = false }

        var cargando by remember { mutableStateOf(false) }
        var mensaje by remember { mutableStateOf<String?>(null) }

        // =============================
        // 🔹 Cargar profesores y horario
        // =============================
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
                    idHorario = horario["id_horario"]?.jsonPrimitive?.intOrNull
                    val dias = horario["dias_semana"]?.jsonPrimitive?.content ?: ""

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

        // ======================================================
        // 🔹 Función: Editar clase con validación de horario
        // ======================================================
        fun editarClase() {
            val diasSeleccionados = seleccionDias.filter { it.value }.keys.joinToString("")
            if (selectedProfesor == null) {
                mensaje = "⚠️ Debes seleccionar un profesor"
                return
            }
            if (diasSeleccionados.isEmpty()) {
                mensaje = "⚠️ Debes seleccionar al menos un día"
                return
            }

            cargando = true
            mensaje = null

            CoroutineScope(Dispatchers.IO).launch {
                val client = HttpClient(CIO)
                var responseStatus = ""

                try {
                    // 1️⃣ Validar horario del profesor
                    val responseValProf = client.post("http://10.0.2.2/API/validarHorarioClaseProfesor.php") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            """
                            {
                                "id_profesor": ${selectedProfesor!!.id},
                                "dias_semana": "$diasSeleccionados",
                                "hora_inicio": "$startTime",
                                "hora_fin": "$endTime",
                                "id_clase": ${clase.id_clase}
                            }
                            """.trimIndent()
                        )
                    }.bodyAsText()

                    val resValProf = Json.parseToJsonElement(responseValProf).jsonObject
                    if (resValProf["success"]?.jsonPrimitive?.boolean == false) {
                        val mensajeError = resValProf["message"]?.jsonPrimitive?.content ?: "Conflicto detectado"
                        val conflictosArray = resValProf["conflictos"]?.jsonArray

                        val diasMap = mapOf(
                            'L' to "Lunes", 'M' to "Martes", 'X' to "Miércoles",
                            'J' to "Jueves", 'V' to "Viernes", 'S' to "Sábado", 'D' to "Domingo"
                        )

                        var detalleConflictos = ""
                        if (conflictosArray != null && conflictosArray.isNotEmpty()) {
                            for (conflicto in conflictosArray) {
                                val obj = conflicto.jsonObject
                                val nombreClase = obj["nombre_clase"]?.jsonPrimitive?.content ?: "Sin nombre"
                                val dias = obj["dias_semana"]?.jsonPrimitive?.content ?: "N/A"
                                val horaInicio = obj["hora_inicio"]?.jsonPrimitive?.content ?: "N/A"
                                val horaFin = obj["hora_fin"]?.jsonPrimitive?.content ?: "N/A"

                                val diasLegibles = dias.mapNotNull { diasMap[it] }.joinToString(" - ")
                                detalleConflictos += "\n📘 $nombreClase ($diasLegibles) $horaInicio - $horaFin"
                            }
                        }

                        responseStatus = "❌ $mensajeError$detalleConflictos"
                        withContext(Dispatchers.Main) {
                            mensaje = responseStatus
                            cargando = false
                        }
                        client.close()
                        return@launch
                    }

                    // 2️⃣ Editar clase
                    val responseClase = client.submitForm(
                        url = "http://10.0.2.2/API/modificarClase.php",
                        formParameters = Parameters.build {
                            append("id_clase", clase.id_clase.toString())
                            append("nombre_clase", className)
                            append("descripcion", description)
                            append("lugar", place)
                            append("hora_inicio", startTime)
                            append("hora_fin", endTime)
                            append("id_profesor", selectedProfesor!!.id.toString())
                        }
                    ).bodyAsText()

                    // 3️⃣ Actualizar horario
                    val responseHorario = client.submitForm(
                        url = "http://10.0.2.2/API/editarHorario.php",
                        formParameters = Parameters.build {
                            append("id_horario", idHorario.toString())
                            append("dias_semana", diasSeleccionados)
                        }
                    ).bodyAsText()

                    responseStatus =
                        if ("success" in responseClase.lowercase() && "success" in responseHorario.lowercase()) {
                            "✅ Clase actualizada correctamente"
                        } else "⚠️ Hubo un problema al actualizar la clase"

                } catch (e: Exception) {
                    responseStatus = "⚠️ Error: ${e.message}"
                } finally {
                    client.close()
                }

                withContext(Dispatchers.Main) {
                    mensaje = responseStatus
                    cargando = false
                }
            }
        }

        // =============================
        // 🔹 Interfaz (UI)
        // =============================
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
                    label = { Text("Nombre de la clase") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = { Text("Lugar") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 🔹 Dropdown de profesores
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedProfesor?.nombre ?: "Seleccionar profesor",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Profesor") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

                Text("Días de clase:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    diasSemana.forEach { (key, label) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Checkbox(
                                checked = seleccionDias[key] ?: false,
                                onCheckedChange = { seleccionDias[key] = it }
                            )
                            Text(label)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    SimulatedTimePicker(
                        label = "Inicio",
                        horaActual = startTime,
                        onTimeSelected = { startTime = it },
                        modifier = Modifier.weight(1f)
                    )
                    SimulatedTimePicker(
                        label = "Fin",
                        horaActual = endTime,
                        onTimeSelected = { endTime = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (startTime >= endTime) {
                    Text("⚠️ La hora de fin debe ser posterior", color = MaterialTheme.colorScheme.error)
                }

                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(onClick = { editarClase() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Guardar cambios")
                    }
                }

                mensaje?.let {
                    Text(
                        text = it,
                        color = if (it.contains("✅")) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
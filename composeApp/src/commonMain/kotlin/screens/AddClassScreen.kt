package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import modelo.SimulatedTimePicker

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
        val diasMap = mapOf(
            'L' to "Lunes",
            'M' to "Martes",
            'X' to "Miércoles",
            'J' to "Jueves",
            'V' to "Viernes",
            'S' to "Sábado",
            'D' to "Domingo"
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

                    val diasSeleccionados = diasSemana
                        .map { it.first }
                        .filter { seleccionDias[it] == true }
                        .joinToString("")

                    // --- 1️⃣ Validar conflicto de horarios (profesor) ---
                    val responseValProf =
                        client.post("http://10.0.2.2/API/validarHorarioClaseProfesor.php") {
                            contentType(ContentType.Application.Json)
                            setBody(
                                """
                    {
                        "id_profesor": ${selectedProfesor!!.id},
                        "dias_semana": "$diasSeleccionados",
                        "hora_inicio": "$startTime",
                        "hora_fin": "$endTime"
                    }
                    """.trimIndent()
                            )
                        }.bodyAsText()

                    val resValProf = Json.parseToJsonElement(responseValProf).jsonObject

                    if (resValProf["success"]?.jsonPrimitive?.boolean == false) {
                        val mensaje =
                            resValProf["message"]?.jsonPrimitive?.content ?: "Conflicto detectado"
                        val conflictosArray = resValProf["conflictos"]?.jsonArray

                        var detalleConflictos = ""

                        if (conflictosArray != null && conflictosArray.isNotEmpty()) {
                            for (conflicto in conflictosArray) {
                                val obj = conflicto.jsonObject
                                val nombreClase =
                                    obj["nombre_clase"]?.jsonPrimitive?.content ?: "Sin nombre"
                                val dias = obj["dias_semana"]?.jsonPrimitive?.content ?: "N/A"
                                val inicio = obj["hora_inicio"]?.jsonPrimitive?.content ?: "N/A"
                                val fin = obj["hora_fin"]?.jsonPrimitive?.content ?: "N/A"

                                val diasLegibles =
                                    dias.mapNotNull { diasMap[it] }.joinToString(" - ")

                                detalleConflictos += "\n📘 $nombreClase ($diasLegibles) $inicio - $fin"
                            }
                        }

                        responseStatus = "❌ $mensaje$detalleConflictos"
                        client.close()
                        return@launch
                    }

                    // --- 2️⃣ Crear clase ---
                    val responseClase = client.post("http://10.0.2.2/API/crearClase.php") {
                        contentType(ContentType.Application.FormUrlEncoded)
                        setBody(
                            Parameters.build {
                                append("nombre_clase", className)
                                append("descripcion", description)
                                append("hora_inicio", startTime)
                                append("hora_fin", endTime)
                                append("lugar", place)
                                append("id_profesor", selectedProfesor!!.id.toString())
                            }.formUrlEncode()
                        )
                    }.bodyAsText()

                    val jsonResponse = Json.parseToJsonElement(responseClase).jsonObject
                    val success = jsonResponse["success"]?.jsonPrimitive?.boolean ?: false

                    if (!success) {
                        responseStatus = "❌ Error al crear clase: ${jsonResponse["message"]}"
                        client.close()
                        return@launch
                    }

                    val idClase = jsonResponse["id_clase"]?.jsonPrimitive?.int
                    if (idClase == null) {
                        responseStatus = "⚠️ No se recibió el ID de la clase."
                        client.close()
                        return@launch
                    }

                    // --- 3️⃣ Crear horario ---
                    val bodyHorario = buildJsonObject {
                        put("id_clase", JsonPrimitive(idClase))
                        put("dias_semana", JsonPrimitive(diasSeleccionados))
                    }

                    val responseHorario = client.post("http://10.0.2.2/API/crearHorario.php") {
                        contentType(ContentType.Application.Json)
                        setBody(bodyHorario.toString())
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFF751F), // Naranja
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(BottomBarScreen()) }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
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

                // --- Selectores de hora ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SimulatedTimePicker(
                        label = "Hora de inicio",
                        horaActual = startTime,
                        onTimeSelected = { startTime = it },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    SimulatedTimePicker(
                        label = "Hora de fin",
                        horaActual = endTime,
                        onTimeSelected = { endTime = it },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
                if (startTime.isNotEmpty() && endTime.isNotEmpty() && startTime >= endTime) {
                    Text(
                        "⚠️ La hora de fin debe ser posterior a la de inicio",
                        color = MaterialTheme.colorScheme.error
                    )
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
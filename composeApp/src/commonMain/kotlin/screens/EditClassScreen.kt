package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import bottombar.BottomBarScreen
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

        val scope = rememberCoroutineScope()
        var cargando by remember { mutableStateOf(false) }
        var mensaje by remember { mutableStateOf<String?>(null) }

        // 🔹 Cargar profesores
        LaunchedEffect(Unit) {
            try {
                val client = HttpClient(CIO)
                val response = client.get("http://10.0.2.2/API/listarProfesor.php")
                val json = response.bodyAsText()

                val parsed = Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<List<Profesor>>(json)

                profesores = parsed
                selectedProfesor = parsed.find { it.id == clase.id_profesor.toString() }
                client.close()
            } catch (e: Exception) {
                mensaje = "Error al cargar profesores: ${e.message}"
            }
        }

        // 🔹 Editar clase
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
                    val response = client.submitForm(
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

                    if (response.contains("true", ignoreCase = true)) {
                        mensaje = "Clase actualizada correctamente."
                        navigator.push(BottomBarScreen())
                    } else {
                        mensaje = "Error al actualizar: $response"
                    }
                } catch (e: Exception) {
                    mensaje = "Error de red: ${e.message}"
                } finally {
                    client.close()
                    cargando = false
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar Clase") },
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

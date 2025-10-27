package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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

        ///lista de profesores
        var profesores by remember { mutableStateOf<List<Profesor>>(emptyList()) }
        var selectedProfesor by remember { mutableStateOf<Profesor?>(null) }
        var expanded by remember { mutableStateOf(false) }

        var responseStatus by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        // Cargar los profesores al iniciar
        LaunchedEffect(Unit) {
            scope.launch {
                try {
                    val client = HttpClient(CIO)
                    val response = client.get("http://10.0.2.2/API/listarProfesor.php")
                    val json = response.bodyAsText()
                    val parsed = Json.decodeFromString<List<Profesor>>(json)
                    profesores = parsed
                    client.close()
                } catch (e: Exception) {
                    responseStatus = "Error cargando profesores: ${e.message}"
                }
            }
        }

        fun createClass(){
            scope.launch {
                try {
                    val client = HttpClient(CIO)
                    val response: String =
                        client.post("http://10.0.2.2/API/crearClase.php") {
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
                    responseStatus = "Respuesta: $response"
                    client.close()
                } catch (e: Exception) {
                    responseStatus = "Error: ${e.message}"
                }
            }
            navigator.push(BottomBarScreen())
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
                    label = { Text("Nombre Clase") })
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción")})
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Tiempo Inicio") })
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Tiempo Fin") })
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = { Text("Lugar") })

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

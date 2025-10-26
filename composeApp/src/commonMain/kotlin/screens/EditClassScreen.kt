package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bottombar.BottomBarScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import modelo.Clase
import modelo.User

class EditClassScreen(private val clase: Clase) : Screen {
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
        var teacherId by remember { mutableStateOf(clase.id_profesor.toString()) }
        val scope = rememberCoroutineScope()
        val url = "http://10.0.2.2/API/modificarClase.php"

        var cargando by remember { mutableStateOf(false) }
        var mensaje by remember { mutableStateOf<String?>(null) }

        fun editarClase() {
            scope.launch {
                cargando = true
                mensaje = null
                val client = HttpClient()
                try {
                    val response = client.submitForm(
                        url = url,
                        formParameters = Parameters.build {
                            append("id_clase", id)
                            append("nombre_clase", className)
                            append("descripcion", description)
                            append("hora_inicio", startTime)
                            append("hora_fin", endTime)
                            append("lugar", place)
                            append("id_profesor", teacherId) //falta validar de alguna forma el profesor
                        }
                    ).bodyAsText()

                    if (response.contains("exito", ignoreCase = true)) {
                        mensaje = "Clase actualizada correctamente."
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
                        IconButton(onClick = { navigator.push(BottomBarScreen())}) {
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
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción")},
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Tiempo Inicio") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Tiempo Fin") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = { Text("Lugar") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = teacherId,//falta validar de alguna forma el profesor
                    onValueChange = { teacherId = it },
                    label = { Text("Profesor") },//falta validar de alguna forma el profesor
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth())

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
                        color = if (it.contains("exito", true))
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }


                TextButton(onClick = { navigator.push(BottomBarScreen()) }) { Text("Cancelar") } //Por ahora se simula
            }
        }
    }
}
package screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bottombar.BottomBarScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.*
import io.ktor.http.Parameters
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import modelo.User


class ManageStudentsClassScreen(private val idClase: Int) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val json = Json { ignoreUnknownKeys = true; isLenient = true }

        // Estados
        var disponibles by remember { mutableStateOf<List<User>>(emptyList()) }
        var inscritos by remember { mutableStateOf<List<User>>(emptyList()) }
        var cargando by remember { mutableStateOf(false) }
        var mensaje by remember { mutableStateOf<String?>(null) }

        // URLs
        val baseUrl = "http://10.0.2.2/API"
        val listarUsuariosUrl = "$baseUrl/listarUsuario.php"
        val listarInscritosUrl = "$baseUrl/listarEstudiantesClase.php?id_clase=$idClase"
        val agregarUrl = "$baseUrl/agregarEstudianteClase.php"
        val eliminarUrl = "$baseUrl/eliminarEstudianteClase.php"

        // Función: cargar estudiantes
        fun cargarListas() {
            scope.launch {
                cargando = true
                val client = HttpClient()
                try {
                    val todosTxt = client.get(listarUsuariosUrl).bodyAsText()
                    val inscritosTxt = client.get(listarInscritosUrl).bodyAsText()

                    val todos = json.decodeFromString<List<User>>(todosTxt)
                        .filter { it.id_rol == 3 }
                    val enClase = json.decodeFromString<List<User>>(inscritosTxt)

                    val enClaseIds = enClase.map { it.id_usuario } // o it.id_usuario según tu modelo
                    val disponiblesList = todos.filterNot { it.id_usuario in enClaseIds }

                    disponibles = disponiblesList
                    inscritos = enClase
                    mensaje = null
                } catch (e: Exception) {
                    mensaje = "Error al cargar: ${e.message}"
                } finally {
                    cargando = false
                    client.close()
                }
            }
        }

        // Función: agregar estudiante
        fun agregar(user: User) {
            scope.launch {
                cargando = true
                val client = HttpClient()
                try {
                    val response = client.submitForm(
                        url = agregarUrl,
                        formParameters = Parameters.build {
                            append("id_usuario", user.id_usuario.toString())
                            append("id_clase", idClase.toString())
                        }
                    ).bodyAsText()

                    val obj = json.parseToJsonElement(response).jsonObject
                    if (obj["status"]?.jsonPrimitive?.content == "success") {
                        mensaje = "Estudiante agregado"
                        cargarListas()
                    } else {
                        mensaje = obj["message"]?.jsonPrimitive?.content ?: "Error desconocido"
                    }
                } catch (e: Exception) {
                    mensaje = "Error al agregar: ${e.message}"
                } finally {
                    cargando = false
                    client.close()
                }
            }
        }

        // Función: eliminar estudiante
        fun eliminar(user: User) {
            scope.launch {
                cargando = true
                val client = HttpClient()
                try {
                    val response = client.submitForm(
                        url = eliminarUrl,
                        formParameters = Parameters.build {
                            append("id_usuario", user.id_usuario.toString())
                            append("id_clase", idClase.toString())
                        }
                    ).bodyAsText()

                    val obj = json.parseToJsonElement(response).jsonObject
                    if (obj["status"]?.jsonPrimitive?.content == "success") {
                        mensaje = "Estudiante eliminado"
                        cargarListas()
                    } else {
                        mensaje = obj["message"]?.jsonPrimitive?.content ?: "Error desconocido"
                    }
                } catch (e: Exception) {
                    mensaje = "Error al eliminar: ${e.message}"
                } finally {
                    cargando = false
                    client.close()
                }
            }
        }

        // Cargar datos al iniciar
        LaunchedEffect(Unit) { cargarListas() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Gestión de estudiantes de clase") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(BottomBarScreen()) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    mensaje?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }

                    // --- Estudiantes disponibles ---
                    Text(
                        text = "Estudiantes disponibles",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (disponibles.isEmpty()) {
                        Text("No hay estudiantes disponibles para agregar.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(disponibles) { est ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${est.nombre} ${est.apellido}")
                                            Text(est.correo, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Button(onClick = { agregar(est) }) {
                                            Text("Agregar")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // --- Estudiantes inscritos ---
                    Text(
                        text = "Estudiantes en la clase",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (inscritos.isEmpty()) {
                        Text("Aún no hay estudiantes inscritos en esta clase.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(inscritos) { est ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${est.nombre} ${est.apellido}")
                                            Text(est.correo, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Button(
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            onClick = { eliminar(est) }
                                        ) {
                                            Text("Eliminar", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
    }

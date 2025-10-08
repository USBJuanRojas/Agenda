package bottombar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import modelo.Clase
import modelo.Objlogin
import screens.LoginScreen

object HomeTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember {
                TabOptions(
                    index = 0u,
                    title = "Horario de Clases",
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
        val listarUrl = "http://10.0.2.2/API/obtenerClasesUsuario.php?id_usuario=${Objlogin.idUsu}"

        fun cargarClases() {
            scope.launch {
                cargando = true
                error = null
                val client = HttpClient()
                try {
                    val responseText: String = client.get(listarUrl).bodyAsText()
                    val response = json.decodeFromString<ClasesResponse>(responseText)
                    if (response.success && response.clases != null) {
                        clases = response.clases
                    } else {
                        clases = emptyList()
                        error = response.message ?: "No se encontraron clases"
                    }
                } catch (e: Exception) {
                    error = "Error de conexión: ${e.message}"
                } finally {
                    client.close()
                    cargando = false
                }
            }
        }

        // Llamamos al cargar al inicio
        LaunchedEffect(Unit) { cargarClases() }

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
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    cargando -> {
                        item {
                            CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentWidth())
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
                                modifier = Modifier.fillMaxWidth(),
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
                                    Text(text = clase.nombre_clase, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Hora: ${clase.hora_inicio} - ${clase.hora_fin}")
                                    Text(text = "Lugar: ${clase.lugar}")
                                    Text(text = "Profesor: ${clase.profesor_nombre} ${clase.profesor_apellido}")

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(onClick = { /* Editar clase */ }) {
                                            Text("Editar")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = {
                                            claseDelete = clase
                                            showDialog = true
                                        }) {
                                            Text("Eliminar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Diálogo de confirmación de eliminación
            if (showDialog && claseDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Seguro que deseas eliminar la clase ${claseDelete?.nombre_clase}?") },
                    confirmButton = {
                        TextButton(onClick = {
                            // Aquí llamarías al endpoint de eliminación
                            clases = clases.toMutableList().apply { remove(claseDelete) }
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

package screens

import androidx.compose.foundation.layout.*
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
import bottombar.TaskTab
import modelo.Tarea
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import modelo.Objlogin
import modelo.SimulatedDatePicker
import modelo.SimulatedTimePicker

class EditTaskScreen(private val tarea: Tarea) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        // Campos iniciales
        var idTarea by remember { mutableStateOf(tarea.idTarea.toString()) }
        var idClase by remember { mutableStateOf(tarea.idClase.toString()) }
        var asunto by remember { mutableStateOf(tarea.asunto) }
        var descripcion by remember { mutableStateOf(tarea.descripcion) }

        var fechaInicio by remember { mutableStateOf(tarea.fechaInicio) }
        var fechaFin by remember { mutableStateOf(tarea.fechaFin) }

        // Separar fecha y hora del formato "DD/MM/YYYY-HH:MM"
        val partesInicio = tarea.fechaInicio.split("-")
        val partesFin = tarea.fechaFin.split("-")
        var observaciones by remember { mutableStateOf(tarea.observaciones ?: "") }

        //Tiempo
        var startDate by remember { mutableStateOf(partesInicio.getOrNull(0) ?: "") }
        var startTime by remember { mutableStateOf(partesInicio.getOrNull(1) ?: "") }
        var endDate by remember { mutableStateOf(partesFin.getOrNull(0) ?: "") }
        var endTime by remember { mutableStateOf(partesFin.getOrNull(1) ?: "") }


        var cargando by remember { mutableStateOf(false) }
        var mensaje by remember { mutableStateOf<String?>(null) }

        fun editarTarea() {
            scope.launch {
                cargando = true
                mensaje = null
                val client = HttpClient(CIO)

                try {
                    val response = client.submitForm(
                        url = "http://10.0.2.2/API/modificarTarea.php",
                        formParameters = Parameters.build {
                            append("id_tarea", idTarea)
                            append("id_clase", idClase)
                            append("asunto", asunto)
                            append("descripcion", descripcion)
                            append("fecha_inicio", fechaInicio)
                            append("fecha_fin", fechaFin)
                            append("observaciones", observaciones)
                        }
                    ).bodyAsText()

                    if (response.contains("true", ignoreCase = true)) {
                        mensaje = "Tarea actualizada correctamente."
                        navigator.push(BottomBarScreen(initialTab = TaskTab))
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
                    title = { Text("Editar Tarea") },
                    navigationIcon = {
                        IconButton(onClick = {
                            navigator.push(BottomBarScreen(initialTab = TaskTab))
                        }) {
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
                    value = asunto,
                    onValueChange = { asunto = it },
                    label = { Text("Asunto") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth()
                )

                // --- Selectores de inicio ---
                Text("Fecha de inicio")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SimulatedDatePicker(
                        label = "Dia de inicio",
                        fechaActual = startDate,
                        onDateSelected = { startDate = it },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    SimulatedTimePicker(
                        label = "Hora de inicio",
                        horaActual = startTime,
                        onTimeSelected = { startTime = it },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }

                // --- Selectores de fin ---
                Text("Fecha de fin")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SimulatedDatePicker(
                        label = "Dia de fin",
                        fechaActual = endDate,
                        onDateSelected = { endDate = it },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    SimulatedTimePicker(
                        label = "Hora de fin",
                        horaActual = endTime,
                        onTimeSelected = { endTime = it },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }


                fechaInicio = "${startDate}-${startTime}"
                fechaFin = "${endDate}-${endTime}"

                val startMinutes = startTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
                val endMinutes = endTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }

                val isSameDay = startDate == endDate
                val isInvalid =
                    (isSameDay && startMinutes >= endMinutes) || // Mismo día → validar horas
                            (!isSameDay && startDate > endDate)          // Diferente día → validar fecha

                if (isInvalid) {
                    Text(
                        text = "⚠️ La fecha/hora de fin debe ser posterior a la de inicio",
                        color = MaterialTheme.colorScheme.error
                    )
                }



                Spacer(modifier = Modifier.height(16.dp))

                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = { editarTarea() },
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

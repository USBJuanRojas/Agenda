package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bottombar.BottomBarScreen
import bottombar.TaskTab
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.coroutines.launch
import modelo.SimulatedDatePicker
import modelo.SimulatedTimePicker
import modelo.Tarea

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
                    title = { Text("Editar Tarea") }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFF751F), // Naranja
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            navigator.push(BottomBarScreen(initialTab = TaskTab))
                        }) {
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
                    value = asunto,
                    onValueChange = { asunto = it },
                    label = { Text("Asunto") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        cursorColor = Color.DarkGray,
                        focusedIndicatorColor = Color(0xFFFF751F),
                        focusedLabelColor = Color(0xFFFF751F)
                    )
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        cursorColor = Color.DarkGray,
                        focusedIndicatorColor = Color(0xFFFF751F),
                        focusedLabelColor = Color(0xFFFF751F)
                    )
                )

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        cursorColor = Color.DarkGray,
                        focusedIndicatorColor = Color(0xFFFF751F),
                        focusedLabelColor = Color(0xFFFF751F)
                    )
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFFFF751F),)
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

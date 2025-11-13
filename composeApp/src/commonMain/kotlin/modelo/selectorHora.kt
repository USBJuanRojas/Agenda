package modelo

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SimulatedTimePicker(
    label: String,
    horaActual: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val horaInicial = remember(horaActual) {
        if (horaActual.isNotEmpty() && horaActual.contains(":")) {
            val partes = horaActual.split(":")
            val hora = partes.getOrNull(0)?.toIntOrNull() ?: 0
            val minuto = partes.getOrNull(1)?.toIntOrNull() ?: 0
            Pair(hora, minuto)
        } else {
            Pair(0, 0)
        }
    }
    var showDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(horaInicial.first) }
    var selectedMinute by remember { mutableStateOf(horaInicial.second) }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier.height(56.dp) // altura estándar y ancho forzado por fillMaxWidth
    ) {
        Text(
            "$label: ${if (horaActual.isEmpty()) "--:--" else horaActual}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Seleccionar hora") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DropdownMenuSelector(
                            label = "Hora",
                            range = (0..23).toList(),
                            selected = selectedHour,
                            onSelect = { selectedHour = it }
                        )
                        Spacer(Modifier.width(12.dp))
                        DropdownMenuSelector(
                            label = "Minutos",
                            range = listOf(0, 15, 30, 45),
                            selected = selectedMinute,
                            onSelect = { selectedMinute = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val horaFormateada = "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}"
                    onTimeSelected(horaFormateada)
                    showDialog = false
                }) {
                    Text("Aceptar")
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

@Composable
fun DropdownMenuSelector(
    label: String,
    range: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: ${selected.toString().padStart(2, '0')}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            range.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.toString().padStart(2, '0')) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

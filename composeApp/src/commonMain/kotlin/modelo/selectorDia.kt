package modelo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SimulatedDatePicker(
    label: String,
    fechaActual: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    // 🔹 Obtener fecha inicial (desde la DB o actual)
    val fechaInicial = remember(fechaActual) {
        if (fechaActual.isNotEmpty() && fechaActual.contains("/")) {
            val partes = fechaActual.split("/")
            val dia = partes.getOrNull(0)?.toIntOrNull() ?: 1
            val mes = partes.getOrNull(1)?.toIntOrNull() ?: 1
            val anio = partes.getOrNull(2)?.toIntOrNull() ?: 2024
            Triple(dia, mes, anio)
        } else {
            val dia = 1
            val mes = 1
            val anio = 2025
            Triple(dia, mes, anio)
        }
    }

    // 🔹 Estados internos
    var selectedYear by remember { mutableStateOf(fechaInicial.third) }
    var selectedMonth by remember { mutableStateOf(fechaInicial.second) } // Enero = 1
    var selectedDay by remember { mutableStateOf(fechaInicial.first) }

    val diasDisponibles = remember(selectedMonth, selectedYear) {
        getDaysInMonth(selectedMonth, selectedYear)
    }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = "$label: ${if (fechaActual.isEmpty()) "--/--/----" else fechaActual}",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val dia = selectedDay.toString().padStart(2, '0')
                    val mes = selectedMonth.toString().padStart(2, '0')
                    val anio = selectedYear.toString().padStart(4, '0')
                    val fecha = "$dia/$mes/$anio"

                    onDateSelected(fecha)
                    showDialog = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Seleccionar fecha") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DropdownSelector(
                        label = "Año",
                        options = (2020..2035).toList(),
                        selectedValue = selectedYear,
                        onValueChange = { selectedYear = it }
                    )
                    DropdownSelector(
                        label = "Mes",
                        options = (1..12).toList(),
                        selectedValue = selectedMonth,
                        onValueChange = { selectedMonth = it }
                    )
                    DropdownSelector(
                        label = "Día",
                        options = diasDisponibles,
                        selectedValue = selectedDay,
                        onValueChange = { selectedDay = it }
                    )
                }
            }
        )
    }
}

/**
 * Calcula los días válidos de un mes según el año (incluye años bisiestos)
 */
fun getDaysInMonth(month: Int, year: Int): List<Int> {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> (1..31).toList()
        4, 6, 9, 11 -> (1..30).toList()
        2 -> {
            val isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
            if (isLeap) (1..29).toList() else (1..28).toList()
        }
        else -> emptyList()
    }
}

/**
 * Selector genérico tipo dropdown (reutilizable)
 */
@Composable
fun DropdownSelector(
    label: String,
    options: List<Int>,
    selectedValue: Int,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedValue.toString())
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.toString()) },
                    onClick = {
                        onValueChange(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

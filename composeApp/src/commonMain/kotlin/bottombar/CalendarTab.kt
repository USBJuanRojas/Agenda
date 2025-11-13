package bottombar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import modelo.Clase
import modelo.Objlogin
import kotlin.random.Random

object CalendarTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.CalendarMonth)
            return remember {
                TabOptions(
                    index = 2u,
                    title = "Horario Semanal",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        CalendarScreen()
    }

    private fun randomColor(): Color {
        val hue = Random.nextFloat()
        val saturation = 0.6f + Random.nextFloat() * 0.4f
        val brightness = 0.7f + Random.nextFloat() * 0.3f
        return Color.hsv(hue * 360f, saturation, brightness)
    }

    // --------------------------------------------------------
    // 📅 Pantalla principal con Scaffold
    // --------------------------------------------------------
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CalendarScreen() {
        val scope = rememberCoroutineScope()
        var clases by remember { mutableStateOf<List<Clase>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        val json = Json { ignoreUnknownKeys = true; isLenient = true }

        LaunchedEffect(Unit) {
            scope.launch {
                cargando = true
                val client = HttpClient()
                try {
                    val url = "http://10.0.2.2/API/obtenerClasesUsuario.php?id_usuario=${Objlogin.idUsu}"
                    val responseText = client.get(url).bodyAsText()
                    val jsonResponse = json.parseToJsonElement(responseText).jsonObject

                    if (jsonResponse["success"]?.jsonPrimitive?.booleanOrNull == true) {
                        val clasesArray = jsonResponse["clases"]?.jsonArray ?: JsonArray(emptyList())
                        val lista = mutableListOf<Clase>()

                        for (element in clasesArray) {
                            val obj = element.jsonObject
                            val clase = Clase(
                                id_clase = obj["id_clase"]?.jsonPrimitive?.int ?: 0,
                                nombre_clase = obj["nombre_clase"]?.jsonPrimitive?.content ?: "",
                                descripcion = obj["descripcion"]?.jsonPrimitive?.contentOrNull ?: "",
                                hora_inicio = obj["hora_inicio"]?.jsonPrimitive?.content ?: "",
                                hora_fin = obj["hora_fin"]?.jsonPrimitive?.content ?: "",
                                lugar = obj["lugar"]?.jsonPrimitive?.content ?: "",
                                id_profesor = obj["id_profesor"]?.jsonPrimitive?.intOrNull,
                                profesor_nombre = obj["profesor_nombre"]?.jsonPrimitive?.contentOrNull,
                                profesor_apellido = obj["profesor_apellido"]?.jsonPrimitive?.contentOrNull,
                                dias_semana = null
                            )

                            try {
                                val diasResponse = client.get("http://10.0.2.2/API/obtenerHorarioClase.php?id_clase=${clase.id_clase}")
                                    .bodyAsText()
                                val diasArray = json.parseToJsonElement(diasResponse).jsonArray

                                val diasString = diasArray.joinToString("") {
                                    it.jsonObject["dias_semana"]?.jsonPrimitive?.content ?: ""
                                }

                                lista.add(clase.copy(dias_semana = diasString))
                            } catch (e: Exception) {
                                println("Error obteniendo horario de clase ${clase.id_clase}: ${e.message}")
                                lista.add(clase.copy(dias_semana = ""))
                            }
                        }
                        clases = lista
                    }

                } catch (e: Exception) {
                    println("❌ Error cargando clases: ${e.message}")
                } finally {
                    client.close()
                    cargando = false
                }
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Calendario Semanal", fontWeight = FontWeight.Bold) }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        label = { Text("Calendario") }
                    )
                }
            }
        ) { innerPadding ->
            if (cargando) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFF751F),)
                }
            } else {
                WeeklySchedule(
                    clases = clases,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    // --------------------------------------------------------
    // 🗓️ Vista semanal con scroll horizontal + vertical
    // --------------------------------------------------------
    @Composable
    fun WeeklySchedule(clases: List<Clase>, modifier: Modifier = Modifier) {
        val dias = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        val horas = (6..18).toList()
        val cellHeight = 60.dp
        val dayWidth = 120.dp

        val scrollHorizontal = rememberScrollState()
        val scrollVertical = rememberScrollState()

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                Modifier
                    .verticalScroll(scrollVertical)
                    .horizontalScroll(scrollHorizontal)
            ) {
                // Encabezado de días
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Spacer(modifier = Modifier.width(48.dp))
                    dias.forEach { dia ->
                        Text(
                            text = dia,
                            modifier = Modifier.width(dayWidth),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Divider(Modifier.padding(vertical = 4.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(cellHeight * horas.size)
                ) {
                    // 1️⃣ Fondo de líneas usando Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        val startX = 0f // empieza desde el borde izquierdo
                        val totalWidth = 48.dp.toPx() + dayWidth.toPx() * dias.size
                        horas.forEachIndexed { index, _ ->
                            val y = index * cellHeight.toPx()
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.6f),
                                start = androidx.compose.ui.geometry.Offset(startX, y),
                                end = androidx.compose.ui.geometry.Offset(totalWidth, y),
                                strokeWidth = 2f
                            )
                        }
                    }

                    // 2️⃣ Texto de horas a la izquierda
                    Column {
                        horas.forEach { hora ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.height(cellHeight)
                            ) {
                                Text(
                                    text = "$hora:00",
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(48.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    // 3️⃣ Clases encima
                    clases.forEach { clase ->
                        ClaseItem(clase, cellHeight)
                    }
                }
            }
        }
    }

    // --------------------------------------------------------
    // 🎒 Render de cada clase
    // --------------------------------------------------------
    private fun horaADecimal(hora: String): Float {
        val partes = hora.split(":")
        val h = partes.getOrNull(0)?.toFloatOrNull() ?: 0f
        val m = partes.getOrNull(1)?.toFloatOrNull() ?: 0f
        return h + (m / 60f)
    }
    @Composable
    fun ClaseItem(clase: Clase, cellHeight: Dp) {
        val color = remember { randomColor() }
        val totalDias = 7 // L-D
        val dayWidth = 120.dp

        val dias = mutableListOf<Int>()
        clase.dias_semana?.forEach { c ->
            when (c.uppercaseChar()) {
                'L' -> dias.add(0)
                'M' -> dias.add(1)
                'X' -> dias.add(2)
                'J' -> dias.add(3)
                'V' -> dias.add(4)
                'S' -> dias.add(5)
                'D' -> dias.add(6)
            }
        }

        val inicio = horaADecimal(clase.hora_inicio)
        val fin = horaADecimal(clase.hora_fin)
        val duracion = fin - inicio

        dias.forEach { diaIndex ->
            Box(
                modifier = Modifier
                    .offset(
                        x = 58.dp + (dayWidth * diaIndex),
                        y = ((inicio - 6.1f) * cellHeight) + (cellHeight * 0.1f)
                    )
                    .size(width = dayWidth - 2.dp, height = duracion * cellHeight /*- 8.dp*/)
                    .background(color.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        clase.nombre_clase,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        clase.lugar,
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (Objlogin.perfil == "Estudiante") {
                        Text(
                            "${clase.profesor_nombre ?: ""} ${clase.profesor_apellido ?: ""}",
                            fontSize = 10.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

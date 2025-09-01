package bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object CalendarTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.CalendarMonth)
            return remember {
                TabOptions(
                    index = 2u,
                    title = "Calendar",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        Box(Modifier.fillMaxSize().background(Color.Gray), contentAlignment = Alignment.Center) {
            Text("CalendarScreen", fontSize = 22.sp, color = Color.White)
        }
    }
}


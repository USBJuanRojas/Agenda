package bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.*
import bottombar.*
import screens.HomeScreen

class BottomBarScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        TabNavigator(HomeTab) { // HomeTab es la tab por defecto
            val tabNavigator = LocalTabNavigator.current

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(tabNavigator.current.options.title) },
                        navigationIcon = {
                            IconButton(onClick = {
                                navigator.push(HomeScreen()) // Vuelve al menú principal
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        // Task
                        NavigationBarItem(
                            selected = tabNavigator.current == TaskTab,
                            onClick = { tabNavigator.current = TaskTab },
                            label = { Text(TaskTab.options.title) },
                            icon = {
                                TaskTab.options.icon?.let { Icon(it, contentDescription = null) }
                            }
                        )

                        // Home
                        NavigationBarItem(
                            selected = tabNavigator.current == HomeTab,
                            onClick = { tabNavigator.current = HomeTab },
                            label = { Text(HomeTab.options.title) },
                            icon = {
                                HomeTab.options.icon?.let { Icon(it, contentDescription = null) }
                            }
                        )

                        // Calendar
                        NavigationBarItem(
                            selected = tabNavigator.current == CalendarTab,
                            onClick = { tabNavigator.current = CalendarTab },
                            label = { Text(CalendarTab.options.title) },
                            icon = {
                                CalendarTab.options.icon?.let { Icon(it, contentDescription = null) }
                            }
                        )
                    }
                },
                content = { CurrentTab() }
            )
        }
    }
}
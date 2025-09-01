package bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.*
import bottombar.*
import screens.AddClassScreen
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
                        actions = {
                            IconButton(onClick = {
                                // Por ahora no hace nada
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú")
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
                floatingActionButton = {
                    if (tabNavigator.current == HomeTab) {
                        FloatingActionButton(onClick = {
                            navigator.push(AddClassScreen())
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar")
                        }
                    }
                },
                content = { CurrentTab() }
            )
        }
    }
}
package bottombar

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.*
import kotlinx.coroutines.launch
import screens.AddClassScreen
import screens.AddTaskScreen
import screens.LoginScreen

class BottomBarScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        TabNavigator(HomeTab) { // HomeTab por defecto
            val tabNavigator = LocalTabNavigator.current

            // Truco: forzamos RTL SOLO para que el drawer "start" sea el lado derecho
            androidx.compose.runtime.CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        // Volvemos a LTR dentro del contenido del drawer para que el texto sea normal
                        androidx.compose.runtime.CompositionLocalProvider(
                            LocalLayoutDirection provides LayoutDirection.Ltr
                        ) {
                            ModalDrawerSheet {
                                Text(
                                    "Menú",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                                Divider()
                                NavigationDrawerItem(
                                    label = { Text("Perfil") },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Perfil"
                                        )
                                    }
                                )
                                NavigationDrawerItem(
                                    label = { Text("Información") },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Información"
                                        )
                                    }
                                )
                                NavigationDrawerItem(
                                    label = { Text("Cerrar sesión") },
                                    selected = false,
                                    onClick = {
                                        navigator.push(LoginScreen())
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = "Cerrar sesión"
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) {
                    // Y también LTR para el contenido de la app
                    androidx.compose.runtime.CompositionLocalProvider(
                        LocalLayoutDirection provides LayoutDirection.Ltr
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text(tabNavigator.current.options.title) },
                                    actions = {
                                        IconButton(onClick = {
                                            scope.launch { drawerState.open() }
                                        }) {
                                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                                        }
                                    }
                                )
                            },
                            bottomBar = {
                                NavigationBar {
                                    NavigationBarItem(
                                        selected = tabNavigator.current == TaskTab,
                                        onClick = { tabNavigator.current = TaskTab },
                                        label = { Text(TaskTab.options.title) },
                                        icon = {
                                            TaskTab.options.icon?.let { Icon(it, null) }
                                        }
                                    )
                                    NavigationBarItem(
                                        selected = tabNavigator.current == HomeTab,
                                        onClick = { tabNavigator.current = HomeTab },
                                        label = { Text(HomeTab.options.title) },
                                        icon = {
                                            HomeTab.options.icon?.let { Icon(it, null) }
                                        }
                                    )
                                    NavigationBarItem(
                                        selected = tabNavigator.current == CalendarTab,
                                        onClick = { tabNavigator.current = CalendarTab },
                                        label = { Text(CalendarTab.options.title) },
                                        icon = {
                                            CalendarTab.options.icon?.let { Icon(it, null) }
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
                                } else if (tabNavigator.current == TaskTab) {
                                    FloatingActionButton(onClick = {
                                        navigator.push(AddTaskScreen())
                                    }) {
                                        Icon(Icons.Default.Add, contentDescription = "Agregar")
                                    }
                                }
                            },
                            content = { _ ->
                                CurrentTab()
                            }
                        )
                    }
                }
            }
        }
    }
}

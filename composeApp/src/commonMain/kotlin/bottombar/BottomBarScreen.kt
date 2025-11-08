package bottombar

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import kotlinx.coroutines.launch
import screens.AddClassScreen
import screens.AddTaskScreen
import screens.LoginScreen
import modelo.Objlogin
import screens.ListUsers

class BottomBarScreen(private val initialTab: Tab = HomeTab) : Screen { // por defecto será HomeTab

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        //mostrar los diálogos del menú
        val showProfileDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        val showInfoDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

        TabNavigator(initialTab) { // HomeTab por defecto
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
                                        showProfileDialog.value = true
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
                                        showInfoDialog.value = true
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Información"
                                        )
                                    }
                                )
                                /*if (Objlogin.perfil == "Administrador"){
                                    NavigationDrawerItem(
                                        label = { Text("Listar Usuarios") },
                                        selected = false,
                                        onClick = {
                                            navigator.push(ListUsers())
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Listar Usuarios"
                                            )
                                        }
                                    )
                                }*/
                                NavigationDrawerItem(
                                    label = { Text("Cerrar sesión") },
                                    selected = false,
                                    onClick = {
                                        Objlogin.idUsu = ""
                                        Objlogin.nomUsu = ""
                                        Objlogin.apeUsu = ""
                                        Objlogin.perfil = ""
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
                                    if (Objlogin.perfil == "Administrador") {
                                        NavigationBarItem(
                                            selected = tabNavigator.current == UsersTab,
                                            onClick = {
                                                navigator.push(ListUsers())
                                            },
                                            label = { Text("Usuarios") },
                                            icon = { Icon(Icons.Default.People, contentDescription = null) }
                                        )
                                    }
                                    else {
                                        NavigationBarItem(
                                            selected = tabNavigator.current == TaskTab,
                                            onClick = { tabNavigator.current = TaskTab },
                                            label = { Text(TaskTab.options.title) },
                                            icon = {
                                                TaskTab.options.icon?.let { Icon(it, null) }
                                            }
                                        )
                                    }
                                    NavigationBarItem(
                                        selected = tabNavigator.current == HomeTab,
                                        onClick = { tabNavigator.current = HomeTab },
                                        label = { Text(HomeTab.options.title) },
                                        icon = {
                                            HomeTab.options.icon?.let { Icon(it, null) }
                                        }
                                    )
                                    if (Objlogin.perfil != "Administrador") {
                                        NavigationBarItem(
                                            selected = tabNavigator.current == CalendarTab,
                                            onClick = { tabNavigator.current = CalendarTab },
                                            label = { Text(CalendarTab.options.title) },
                                            icon = {
                                                CalendarTab.options.icon?.let { Icon(it, null) }
                                            }
                                        )
                                    }
                                }
                            },
                            floatingActionButton = {
                                if (tabNavigator.current == HomeTab && Objlogin.perfil == "Administrador") {
                                    FloatingActionButton(onClick = {
                                        navigator.push(AddClassScreen())
                                    }) {
                                        Icon(Icons.Default.Add, contentDescription = "Agregar")
                                    }
                                } else if (tabNavigator.current == TaskTab && Objlogin.perfil == "Profesor") {
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

                        if (showProfileDialog.value) {
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { showProfileDialog.value = false },
                                confirmButton = {
                                    androidx.compose.material3.TextButton(onClick = { showProfileDialog.value = false }) {
                                        Text("Cerrar")
                                    }
                                },
                                title = { Text("Perfil del usuario") },
                                text = {
                                    Text(
                                        "Nombre: ${Objlogin.nomUsu}\n" +
                                                "Apellido: ${Objlogin.apeUsu}\n" +
                                                "Perfil: ${Objlogin.perfil}"
                                    )
                                }
                            )
                        }

                        if (showInfoDialog.value) {
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { showInfoDialog.value = false },
                                confirmButton = {
                                    androidx.compose.material3.TextButton(onClick = { showInfoDialog.value = false }) {
                                        Text("Cerrar")
                                    }
                                },
                                title = { Text("Información de la app") },
                                text = {
                                    Text(
                                        "Versión: 2.0.5\n" +
                                                "Desarrollado por: \n" +
                                                "Nicolas Alvarado Soriano\n" +
                                                "Juan Jose Rojas Nieto"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
package org.jjrn.Agenda

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import screens.LoginScreen

@Composable
fun App() {
    MaterialTheme {
        Navigator(LoginScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

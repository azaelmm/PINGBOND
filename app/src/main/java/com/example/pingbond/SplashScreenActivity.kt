package com.example.pingbond

import android.os.Bundle
import android.window.SplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PINGBONDTheme {
                // Inicia el sistema de navegación
                Navigation()
            }
        }
    }
}

@Composable
fun Navigation() {
    // Crear un NavController
    val navController = rememberNavController()

    // Crear el NavHost que albergará las pantallas
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen {
                // Al finalizar el splash, navega al login
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }  // Eliminamos splash de la pila de navegación
                }
            }
        }

        composable("login") {
            LoginScreen()
        }
    }
}

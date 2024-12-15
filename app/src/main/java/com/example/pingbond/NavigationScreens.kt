package com.example.pingbond

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pingbond.ui.theme.PINGBONDTheme

class NavigationScreens : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PINGBONDTheme {
                Navigation()
            }
        }
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "C:\\Users\\AZAEL\\AndroidStudioProjects\\PINGBOND\\app\\src\\main\\java\\com\\example\\pingbond\\InicioAPP.kt"
    ) {
        composable("C:\\Users\\AZAEL\\AndroidStudioProjects\\PINGBOND\\app\\src\\main\\java\\com\\example\\pingbond\\InicioAPP.kt") {
            SplashScreen {
                // Al terminar el splash, navega al login
                navController.navigate("C:\\Users\\AZAEL\\AndroidStudioProjects\\PINGBOND\\app\\src\\main\\java\\com\\example\\pingbond\\LoginActivity.kt") {
                    popUpTo("C:\\Users\\AZAEL\\AndroidStudioProjects\\PINGBOND\\app\\src\\main\\java\\com\\example\\pingbond\\InicioAPP.kt") { inclusive = true }
                }
            }
        }

        composable("C:\\Users\\AZAEL\\AndroidStudioProjects\\PINGBOND\\app\\src\\main\\java\\com\\example\\pingbond\\LoginActivity.kt") {
            LoginScreen(
                onLoginSuccess  = {
                    // Cuando el login es exitoso, navega al dashboard
                    navController.navigate("dashboard") {
                        // Elimina la pantalla de login del stack para evitar que el usuario pueda regresar al login
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("C:\\Users\\AZAEL\\AndroidStudioProjects\\PINGBOND\\app\\src\\main\\java\\com\\example\\pingbond\\DashboardActivity.kt"){
            DashboardScreen()
        }
    }
}

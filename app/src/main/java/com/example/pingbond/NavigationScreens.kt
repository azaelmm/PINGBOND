package com.example.pingbond

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pingbond.ui.theme.PINGBONDTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class NavigationScreens : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            PINGBONDTheme {
                val navController = rememberNavController()

                Navigation(navController, auth)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = auth.currentUser

        if(currentUser!=null){
            Log.i("Estado del log previo al inicio de sesion", "LOGIN OK")
            // navegar a la home
        }
    }
}

@Composable
fun Navigation(navController: NavHostController, auth: FirebaseAuth) {

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
            LoginScreen(auth,
                onLoginSuccess  = {
                    // Cuando el login es exitoso, navega al dashboard
                    navController.navigate("C:\\Users\\AZAEL\\AndroidStudioProjects\\PINGBOND\\app\\src\\main\\java\\com\\example\\pingbond\\DashboardActivity.kt") {
                        // Elimina la pantalla de login del stack para evitar que el usuario pueda regresar al login
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("C:\\Users\\AZAEL\\AndroidStudioProjects\\PINGBOND\\app\\src\\main\\java\\com\\example\\pingbond\\DashboardActivity.kt"){
            //DashboardScreen()
        }
    }
}

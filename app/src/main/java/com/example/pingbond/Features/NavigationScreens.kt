package com.example.pingbond.Features

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
//            navController.navigate("dashboard") {
//                popUpTo("splash") { inclusive = true }
//            }
            // navegar a la home
        }
    }
}

@Composable
fun Navigation(navController: NavHostController, auth: FirebaseAuth) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }

        composable("login") {
            LoginScreen(auth = auth, navController = navController)
        }

        composable("dashboard") {
            DashboardScreen(onNavigate = { /* Implementa navegación interna del Dashboard */ })
        }
    }
}
package com.example.pingbond.Features

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pingbond.Features.DashboardScreens.CreatePostScreenEnhanced
import com.example.pingbond.Features.DashboardScreens.ProfileScreenContentWithAnimation
import com.example.pingbond.ui.theme.PINGBONDTheme

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PINGBONDTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "inicio"
                ) {
                    composable("inicio") {
                        DashboardScreen(navController = navController)
                    }
                    composable("buscar") {
                        PlaceholderScreen("Buscar")
                    }
                    composable("publicar") {
                        CreatePostScreenEnhanced(navController)
                    }
                    composable("notificaciones") {
                        PlaceholderScreen("Notificaciones")
                    }
                    composable("perfil") {
                        ProfileScreenContentWithAnimation(navController)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)) // Fondo más suave
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderSection()

            // Contenido principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Explora contenido aquí",
                    color = Color(0xFF757575),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            BottomNavigationBar(navController)
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF1E88E5), // Sombra azulada
                spotColor = Color(0xFF1E88E5) // Sombra azulada
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Avatar del usuario
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)) // Color de fondo del avatar
        )

        Spacer(modifier = Modifier.width(16.dp)) // Separación entre avatar y texto

        // Nombre del usuario y descripción
        Column {
            Text(
                text = "username",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF333333) // Texto oscuro
            )
            Text(
                text = "324 posts | 4348 followers",
                fontSize = 14.sp,
                color = Color(0xFF757575) // Texto gris
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Empuja el ícono de ajustes al extremo derecho

        // Icono de ajustes
        IconButton(
            onClick = { /* Acción de ajustes */ },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)) // Fondo circular para el ícono
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ajustes",
                tint = Color(0xFF4A4A4A),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                ambientColor = Color(0xFF1E88E5), // Sombra azulada
                spotColor = Color(0xFF1E88E5) // Sombra azulada
            ),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val options = listOf(
            Pair(Icons.Default.Home, "inicio"),
            Pair(Icons.Default.Search, "buscar"),
            Pair(Icons.Default.AddCircleOutline, "publicar"),
            Pair(Icons.Default.Person, "perfil")
        )

        options.forEach { (icon, route) ->
            val isSelected = route == currentRoute

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = route,
                        tint = if (isSelected) Color(0xFF6200EE) else Color(0xFF757575),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = route,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF6200EE) else Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)), // Fondo suave
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pantalla: $name",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF333333), // Texto oscuro
                fontWeight = FontWeight.Bold
            )
        )
    }
}
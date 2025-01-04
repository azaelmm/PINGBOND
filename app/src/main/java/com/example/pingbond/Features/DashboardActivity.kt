package com.example.pingbond.Features

import android.os.Bundle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
                        DashboardScreen(onNavigate = { route ->
                            navController.navigate(route)
                        })
                    }
                    composable("buscar") {
                        PlaceholderScreen("Buscar")
                    }
                    composable("publicar") {
                        CreatePostScreenEnhanced()
                    }
                    composable("notificaciones") {
                        PlaceholderScreen("Notificaciones")
                    }
                    composable("perfil") {
                        ProfileScreenContentWithAnimation()
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8)) // Fondo limpio
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
                    fontSize = 18.sp
                )
            }

            BottomNavigationBar(onNavigate)
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5)) // Fondo claro
            .padding(16.dp)
            .shadow(4.dp, shape = RoundedCornerShape(12.dp), ambientColor = Color.Blue, spotColor = Color.Blue),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Avatar del usuario
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color(0xFFE0E0E0), shape = CircleShape)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.width(16.dp)) // Separación entre avatar y texto

        // Nombre del usuario y descripción
        Column {
            Text(
                text = "username",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
            Text(
                text = "324 posts | 4348 followers",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Empuja el ícono de ajustes al extremo derecho

        // Icono de ajustes
        IconButton(onClick = { /* Acción de ajustes */ }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ajustes",
                tint = Color(0xFF4A4A4A),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(onNavigate: (String) -> Unit) {
    val selectedOption = remember { mutableStateOf("inicio") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)
            .shadow(4.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), ambientColor = Color.Blue,
                spotColor = Color.Blue),

        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val options = listOf(
            Pair(Icons.Default.Home, "inicio"),
            Pair(Icons.Default.Search, "buscar"),
            Pair(Icons.Default.AddCircleOutline, "publicar"),
            Pair(Icons.Default.FavoriteBorder, "notificaciones"),
            Pair(Icons.Default.Person, "perfil")
        )

        options.forEach { (icon, route) ->
            val isSelected = route == selectedOption.value

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    selectedOption.value = route
                    onNavigate(route)
                }) {
                    Icon(
                        icon,
                        contentDescription = route,
                        tint = if (isSelected) Color(0xFF6200EE) else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = route,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color(0xFF6200EE) else Color.Gray
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
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pantalla: $name",
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
        )
    }
}

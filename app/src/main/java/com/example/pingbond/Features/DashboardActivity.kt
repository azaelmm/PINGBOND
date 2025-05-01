package com.example.pingbond.Features

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.pingbond.Features.Components.PostItem
import com.example.pingbond.Features.DashboardScreens.BuscarActivity
import com.example.pingbond.Features.DashboardScreens.BuscarScreen
import com.example.pingbond.Features.DashboardScreens.CreatePostScreenEnhanced
import com.example.pingbond.Features.DashboardScreens.PerfilDeUsuarioScreen
import com.example.pingbond.Features.DashboardScreens.ProfileScreenContentWithAnimation
import com.example.pingbond.Features.ViewModels.PostViewModel
import com.example.pingbond.R
import com.example.pingbond.ui.theme.PINGBONDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


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
                        BuscarScreen(navController)
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
                    composable("perfilUsuario/{uid}") { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("uid") ?: ""
                        PerfilDeUsuarioScreen(userId = uid)
                    }
                }
            }
        }
    }
}


@Composable
fun DashboardScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var username by remember { mutableStateOf("Cargando...") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var postCount by remember { mutableStateOf(0) } // ✅ contador de publicaciones
    val postViewModel = remember { PostViewModel() }
    val posts by postViewModel.posts.collectAsState()

    // Cargar datos del usuario
    LaunchedEffect(userId) {
        userId?.let { id ->
            db.collection("users").document(id)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        username = "Error al cargar"
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        username = document.getString("username") ?: "Sin Nombre"
                        profileImageUrl = document.getString("profilePic")
                        println("✅ Imagen de perfil cargada: $profileImageUrl")
                    }
                }

            // Cargar número de publicaciones del usuario
            db.collection("posts")
                .whereEqualTo("userId", id)
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        println("❌ Error contando posts: ${error.message}")
                        return@addSnapshotListener
                    }
                    postCount = querySnapshot?.size() ?: 0
                    println("✅ Total posts: $postCount")
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderSection(username, profileImageUrl, postCount)  // ✅ ahora pasamos también postCount
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(posts.size) { index ->
                    val post = posts[index]
                    PostItem(post) { postId ->
                        userId?.let { postViewModel.toggleLike(postId, it) }
                    }

                    if (index == posts.lastIndex) {
                        postViewModel.fetchPosts()
                    }
                }
            }

            BottomNavigationBar(navController)
        }
    }
}

@Composable
fun HeaderSection(username: String, profileImageUrl: String?, postCount: Int) {

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF1E88E5),
                spotColor = Color(0xFF1E88E5)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Foto de perfil
        AsyncImage(
            model = profileImageUrl,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = username,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = "Nº de publicaciones: $postCount ",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.PhotoCamera, // 📸 Icono de "foto"
                    contentDescription = "Icono publicaciones",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = Color(0xFF4A4A4A),
                    modifier = Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Cerrar sesión") },
                    onClick = {
                        expanded = false
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Modo claro/oscuro") },
                    onClick = {
                        expanded = false
                        // Lógica de cambio de tema futura
                        Toast.makeText(context, "Tema: pendiente de implementar", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Versión: 1.0.0") },
                    onClick = {
                        expanded = false
                        Toast.makeText(context, "Versión 1.0.0", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}



@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val context = LocalContext.current

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
                        if (route == "buscar") {
                            // ✅ Usamos el contexto para lanzar BuscarActivity
                            context.startActivity(Intent(context, BuscarActivity::class.java))
                        } else if (currentRoute != route) {
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
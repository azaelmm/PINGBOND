package com.example.pingbond.DashboardScreens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pingbond.R
import com.example.pingbond.ui.theme.PINGBONDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PINGBONDTheme {
                ProfileScreenContent()
            }
        }
    }
}

@Composable
fun ProfileScreenContent() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var username by remember { mutableStateOf("Cargando...") }
    var email by remember { mutableStateOf("Cargando...") }
    var profilePicUrl by remember { mutableStateOf("") }
    var posts by remember { mutableStateOf(listOf("Publicación 1", "Publicación 2", "Publicación 3")) }

    // Cargar datos del usuario desde Firestore
    LaunchedEffect(Unit) {
        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    username = document.getString("username") ?: "Sin Nombre"
                    email = document.getString("email") ?: "Sin Correo"
                    profilePicUrl = document.getString("profilePic") ?: ""
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF64B5F6), Color(0xFF1E88E5))
                )
            )
            .padding(16.dp)
    ) {
        ProfileHeader(username = username, email = email, profilePicUrl = profilePicUrl)
        Spacer(modifier = Modifier.height(16.dp))
        PostsSection(posts = posts)
    }
}

@Composable
fun ProfileHeader(username: String, email: String, profilePicUrl: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen de perfil
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            if (profilePicUrl.isNotEmpty()) {
                // Aquí podrías cargar la imagen de perfil con Coil o Glide
                Image(
                    painter = painterResource(id = R.drawable.ic_profile_placeholder_foreground),
                    contentDescription = "Imagen de Perfil",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Información del usuario
        Column {
            Text(
                text = username,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = email,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón de configuración
        IconButton(onClick = { /* Acción de configuración */ }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configuración",
                tint = Color.White
            )
        }
    }
}

@Composable
fun PostsSection(posts: List<String>) {
    Text(
        text = "Tus publicaciones",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(posts.size) { index ->
            PostCard(postContent = posts[index])
        }
    }
}

@Composable
fun PostCard(postContent: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = postContent,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    PINGBONDTheme {
        ProfileScreenContent() }
}

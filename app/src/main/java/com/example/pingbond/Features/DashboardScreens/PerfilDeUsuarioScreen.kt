package com.example.pingbond.Features.DashboardScreens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pingbond.ui.theme.PINGBONDTheme
import com.google.firebase.firestore.FirebaseFirestore

class PerfilUsuarioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("uid") ?: ""

        setContent {
            PINGBONDTheme {
                PerfilDeUsuarioScreen(userId = userId)
            }
        }
    }
}

data class Post(
    val id: String = "",
    val imageUrl: String = "",
    val content: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilDeUsuarioScreen(userId: String) {
    val db = FirebaseFirestore.getInstance()
    var username by remember { mutableStateOf("Cargando...") }
    var profilePic by remember { mutableStateOf<String?>(null) }
    var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }

    // Cargar datos del usuario
    LaunchedEffect(userId) {
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            username = doc.getString("username") ?: "Sin nombre"
            profilePic = doc.getString("profilePic")
        }

        // Cargar posts de este usuario
        db.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                userPosts = result.documents.map { doc ->
                    Post(
                        id = doc.id,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        content = doc.getString("content") ?: ""
                    )
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3D5AFE), Color(0xFF1E88E5)) // Gradiente azul
                )
            )
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Foto de perfil y nombre
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(profilePic),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = username,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Text(
                    text = "Publicaciones",
                    fontSize = 22.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(userPosts) { post ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (post.imageUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(post.imageUrl),
                                contentDescription = "Imagen del post",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = post.content,
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

package com.example.pingbond.Features.DashboardScreens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun PerfilDeUsuarioScreen(userId: String) {
    val db = FirebaseFirestore.getInstance()
    var username by remember { mutableStateOf("Cargando...") }
    var profilePic by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            username = doc.getString("username") ?: "Sin nombre"
            profilePic = doc.getString("profilePic")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(profilePic),
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = username, fontSize = 24.sp)
    }
}

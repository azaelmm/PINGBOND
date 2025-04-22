package com.example.pingbond.Features.DashboardScreens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.example.pingbond.ui.theme.PINGBONDTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BuscarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PINGBONDTheme {
                val navController = rememberNavController()
                BuscarScreen(navController)
            }
        }
    }
}

data class UserProfile(
    val uid: String,
    val username: String,
    val profilePic: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var searchText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = searchText,
            onValueChange = { text ->
                searchText = text

                coroutineScope.launch {
                    if (text.length > 1) {
                        val result = db.collection("users")
                            .orderBy("username")
                            .startAt(text)
                            .endAt(text + "\uf8ff")
                            .get()
                            .await()

                        searchResults = result.documents.map {
                            UserProfile(
                                uid = it.id,
                                username = it.getString("username") ?: "Desconocido",
                                profilePic = it.getString("profilePic")
                            )
                        }
                    } else {
                        searchResults = emptyList()
                    }
                }
            },
            placeholder = { Text("Buscar usuarios...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(searchResults.size) { index ->
                val user = searchResults[index]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(context, PerfilUsuarioActivity::class.java)
                            intent.putExtra("uid", user.uid)
                            context.startActivity(intent)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(user.profilePic),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(user.username, fontSize = 18.sp)
                }
            }
        }
    }
}

package com.example.pingbond.Features.DashboardScreens

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.pingbond.R
import com.example.pingbond.network.ImgurApiService
import com.example.pingbond.ui.theme.PINGBONDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class ProfileScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PINGBONDTheme {
                val navController = rememberNavController()
                ProfileScreenContentWithAnimation(navController = navController)
            }
        }
    }
}

@Composable
fun ProfileScreenContentWithAnimation(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val context = LocalContext.current

    var username by remember { mutableStateOf("Cargando...") }
    var email by remember { mutableStateOf("Cargando...") }
    var profilePicUrl by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }

    // 🔹 Cargar datos del usuario desde Firestore
    LaunchedEffect(userId) {
        userId?.let { id ->
            db.collection("users").document(id)
                .addSnapshotListener { document, error ->
                    if (error != null) return@addSnapshotListener
                    if (document != null && document.exists()) {
                        username = document.getString("username") ?: "Sin Nombre"
                        email = document.getString("email") ?: "Sin Correo"
                        profilePicUrl = document.getString("profilePic") ?: ""
                    }
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground()
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ProfileHeader(username, email, profilePicUrl, navController) {
                showEditDialog = true
            }
            if (showEditDialog) {
                EditProfileDialog(
                    currentUsername = username,
                    currentProfilePic = profilePicUrl,
                    onDismiss = { showEditDialog = false },
                    onSave = { newUsername, newProfilePicUri ->
                        updateProfile(context, db, userId, newUsername, newProfilePicUri) { updatedUrl ->
                            profilePicUrl = updatedUrl // 🔹 Actualizar la UI con la nueva imagen
                        }
                        showEditDialog = false
                    }
                )
            }
        }
    }
}

fun uriToFile(context: Context, uri: Uri): File? {
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg") // 🔹 Nombre único
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        return file
    } catch (e: Exception) {
        println("❌ Error al convertir Uri a File: ${e.message}")
        return null
    }
}



@Composable
fun PostsSection(posts: List<String>) {
    Column {
        Text(
            text = "Publicaciones recientes",
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
}

@Composable
fun AnimatedBackground() {
    val colors = listOf(Color(0xFF3D5AFE), Color(0xFF1E88E5))
    val animatedColor = remember { Animatable(colors.first()) }

    LaunchedEffect(Unit) {
        while (true) {
            animatedColor.animateTo(
                targetValue = colors.random(),
                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(animatedColor.value, colors.last())
                )
            )
    )
}


@Composable
fun PostCard(postContent: String) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text(text = postContent, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        }
    }
}

// 🔹 Subir imagen a Imgur y actualizar Firestore
fun updateProfile(
    context: Context,
    db: FirebaseFirestore,
    userId: String?,
    newUsername: String,
    newProfilePicUri: Uri?,
    onProfileUpdated: (String) -> Unit
) {
    if (userId == null) return

    val userUpdates = mutableMapOf<String, Any>()
    userUpdates["username"] = newUsername

    if (newProfilePicUri != null) {
        val imgurApi = ImgurApiService.create()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = uriToFile(context, newProfilePicUri)
                if (file == null) return@launch

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

                val response = imgurApi.uploadImage("Client-ID ba770e159d9cd8e", body, null)

                if (response.success && response.data.link.isNotEmpty()) {
                    val imgurUrl = response.data.link
                    println("✅ Imagen subida a Imgur: $imgurUrl")

                    userUpdates["profilePic"] = imgurUrl

                    withContext(Dispatchers.Main) {
                        db.collection("users").document(userId).update(userUpdates)
                            .addOnSuccessListener {
                                println("✅ Imagen actualizada en Firestore.")
                                onProfileUpdated(imgurUrl) // 🔹 Reflejar cambios en la UI
                            }
                            .addOnFailureListener { e -> println("❌ Error al actualizar Firestore: ${e.message}") }
                    }
                } else {
                    println("❌ Error en la respuesta de Imgur: ${response.status}")
                }

            } catch (e: Exception) {
                println("❌ Error subiendo a Imgur: ${e.message}")
            }
        }
    } else {
        db.collection("users").document(userId).update(userUpdates)
            .addOnSuccessListener { println("✅ Nombre actualizado en Firestore.") }
            .addOnFailureListener { e -> println("❌ Error al actualizar Firestore: ${e.message}") }
    }
}



@Composable
fun EditProfileDialog(
    currentUsername: String,
    currentProfilePic: String,
    onDismiss: () -> Unit,
    onSave: (String, Uri?) -> Unit
) {
    val context = LocalContext.current
    var newUsername by remember { mutableStateOf(TextFieldValue(currentUsername)) }
    var newProfilePicUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> newProfilePicUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil") },
        text = {
            Column {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Nuevo nombre") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Seleccionar Nueva Imagen")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(newUsername.text, newProfilePicUri) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ProfileHeader(username: String, email: String, profilePicUrl: String, navController: NavController, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(text = username, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable { onEditClick() }, // Permite cambiar la imagen al hacer clic
            contentAlignment = Alignment.Center
        ) {
            if (profilePicUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(profilePicUrl),
                    contentDescription = "Imagen de Perfil",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile_placeholder_foreground),
                    contentDescription = "Imagen de Perfil",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Text(text = email, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onEditClick) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
            }
            IconButton(onClick = { /* Acción de configuración */ }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Configuración", tint = Color.White)
            }
        }
    }
}




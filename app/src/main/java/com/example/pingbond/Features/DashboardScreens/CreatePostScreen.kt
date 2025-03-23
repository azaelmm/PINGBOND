package com.example.pingbond.Features.DashboardScreens

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.pingbond.ui.theme.PINGBONDTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import androidx.compose.ui.platform.LocalContext
import com.example.pingbond.network.ImgurApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class CreatePostScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PINGBONDTheme {
                val navController = rememberNavController()
                CreatePostScreenEnhanced(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreenEnhanced(navController: NavController) {
    var postContent by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Firebase references
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance().reference
    val context = LocalContext.current


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedImageUri = uri }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3D5AFE), Color(0xFF1E88E5))
                ),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Nueva Publicación",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Text Field for Post
                            TextField(
                                value = postContent,
                                onValueChange = { postContent = it },
                                placeholder = { Text("Escribe tu publicación aquí...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = Color(0xFFF0F0F0),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Image Preview or Add Button
                            if (selectedImageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedImageUri),
                                    contentDescription = "Selected Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                OutlinedButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Add Image",
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Agregar Imagen")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Publish Button
                    Button(
                        onClick = {
                            val auth = FirebaseAuth.getInstance()
                            val userId = auth.currentUser?.uid
                            val currentUserId = userId ?: ""

                            if (userId != null && selectedImageUri != null && postContent.isNotBlank()) {
                                isLoading = true
                                db.collection("users").document(userId).get()
                                    .addOnSuccessListener { document ->
                                        val username = document.getString("username") ?: "Anónimo"

                                        uploadPostUsingImgur(
                                            postContent = postContent,
                                            imageUri = selectedImageUri!!,
                                            db = db,
                                            currentUserId = currentUserId,
                                            username = username,
                                            context = context,
                                            onComplete = {
                                                isLoading = false
                                                println("✅ Publicación completa.")
                                            }
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        println("❌ Error al obtener usuario: ${e.message}")
                                        isLoading = false
                                    }
                            } else {
                                println("❌ Completa todos los campos antes de publicar.")
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Publish",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Publicar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
}

fun uriToFileImgur(context: Context, uri: Uri): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_${UUID.randomUUID()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        println("❌ Error al convertir Uri a File: ${e.message}")
        null
    }
}



fun uploadPostUsingImgur(
    context: Context,
    postContent: String,
    imageUri: Uri,
    db: FirebaseFirestore,
    currentUserId: String,
    username: String,
    onComplete: () -> Unit
) {
    val postId = UUID.randomUUID().toString()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Convertir Uri a File
            val file = uriToFileImgur(context, imageUri)
            if (file == null) {
                println("\uD83D\uDD34 Error al convertir URI a archivo")
                onComplete()
                return@launch
            }

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val imgurApi = ImgurApiService.create()
            val response = imgurApi.uploadImage("Client-ID ba770e159d9cd8e", body, null)

            if (response.success && response.data.link.isNotEmpty()) {
                val imgurUrl = response.data.link
                println("✅ Imagen subida a Imgur: $imgurUrl")

                val post = hashMapOf(
                    "content" to postContent,
                    "imageUrl" to imgurUrl,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "userId" to currentUserId,
                    "username" to username
                )

                db.collection("posts").document(postId).set(post)
                    .addOnSuccessListener {
                        println("✅ Post guardado con ID: $postId")
                        onComplete()
                    }
                    .addOnFailureListener {
                        println("❌ Error guardando el post: ${it.message}")
                        onComplete()
                    }
            } else {
                println("❌ Fallo en respuesta Imgur: ${response.status}")
                onComplete()
            }
        } catch (e: Exception) {
            println("❌ Error subiendo imagen a Imgur: ${e.message}")
            onComplete()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CreatePostScreenEnhancedPreview() {
    val navController = rememberNavController()
    PINGBONDTheme {
        CreatePostScreenEnhanced(navController = navController)
    }
}
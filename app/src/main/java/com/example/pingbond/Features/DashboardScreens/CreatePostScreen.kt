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

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedImageUri = uri }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3D5AFE), Color(0xFF1E88E5))
                    )
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
                            text = "Nuevo PingBond",
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

                        if (selectedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Imagen seleccionada",
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
                                    contentDescription = "Agregar Imagen",
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Agregar Imagen")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                                        context = context,
                                        postContent = postContent,
                                        imageUri = selectedImageUri!!,
                                        db = db,
                                        currentUserId = currentUserId,
                                        username = username,
                                        onComplete = { success ->
                                            isLoading = false
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (success) "✅ Publicación realizada con éxito"
                                                    else "❌ Error al publicar"
                                                )
                                            }
                                            if (success) {
                                                postContent = ""
                                                selectedImageUri = null
                                                // Opcional: redirigir al dashboard
                                                navController.popBackStack()
                                            }
                                        }
                                    )
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("❌ Error obteniendo el usuario")
                                    }
                                }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("⚠️ Completa todos los campos antes de publicar")
                            }
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
                        Icon(Icons.Default.Check, contentDescription = "Publicar", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publicar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
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
    onComplete: (Boolean) -> Unit
) {
    val postId = UUID.randomUUID().toString()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val file = uriToFileImgur(context, imageUri)
            if (file == null) {
                onComplete(false)
                return@launch
            }

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val imgurApi = ImgurApiService.create()
            val response = imgurApi.uploadImage("Client-ID ba770e159d9cd8e", body, null)

            if (response.success && response.data.link.isNotEmpty()) {
                val imgurUrl = response.data.link
                val post = hashMapOf(
                    "content" to postContent,
                    "imageUrl" to imgurUrl,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "userId" to currentUserId,
                    "username" to username
                )

                db.collection("posts").document(postId).set(post)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            } else {
                onComplete(false)
            }
        } catch (e: Exception) {
            onComplete(false)
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
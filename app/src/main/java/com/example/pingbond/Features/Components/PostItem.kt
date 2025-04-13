package com.example.pingbond.Features.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pingbond.Features.ViewModels.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

data class Comment(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)

@Composable
fun PostItem(post: Post, onLikeClick: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser?.uid

    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(post.likesCount) }
    var isProcessingLike by remember { mutableStateOf(false) }

    var commentText by remember { mutableStateOf(TextFieldValue("")) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }

    // Verificar si el usuario ya dio like
    LaunchedEffect(post.id, currentUser) {
        if (currentUser != null) {
            db.collection("posts").document(post.id)
                .collection("likes").document(currentUser)
                .get()
                .addOnSuccessListener { document ->
                    isLiked = document.exists()
                }

            // Escuchar comentarios en tiempo real
            db.collection("posts").document(post.id)
                .collection("comments")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    comments = snapshot.documents.mapNotNull { doc ->
                        val content = doc.getString("content") ?: return@mapNotNull null
                        val userId = doc.getString("userId") ?: return@mapNotNull null
                        val username = doc.getString("username") ?: "Anónimo"
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        Comment(doc.id, userId, username, content, timestamp)
                    }
                }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Usuario
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.profilePic,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(post.username, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Imagen
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Imagen del post",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(post.content)

            Spacer(modifier = Modifier.height(8.dp))

            // Likes
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(enabled = !isProcessingLike) {
                            if (currentUser != null && !isProcessingLike) {
                                isProcessingLike = true

                                val postRef = db.collection("posts").document(post.id)
                                val likeRef = postRef.collection("likes").document(currentUser)

                                db.runTransaction { transaction ->
                                    val snapshot = transaction.get(postRef)
                                    val currentLikes = snapshot.getLong("likesCount") ?: 0L

                                    if (isLiked) {
                                        transaction.delete(likeRef)
                                        transaction.update(postRef, "likesCount", (currentLikes - 1).coerceAtLeast(0))
                                        likesCount -= 1
                                    } else {
                                        transaction.set(likeRef, mapOf("likedAt" to System.currentTimeMillis()))
                                        transaction.update(postRef, "likesCount", currentLikes + 1)
                                        likesCount += 1
                                    }
                                }.addOnSuccessListener {
                                    isLiked = !isLiked
                                    isProcessingLike = false
                                }.addOnFailureListener {
                                    isProcessingLike = false
                                }
                            }
                        }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "$likesCount likes", fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de comentarios
            comments.forEach { comment ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "${comment.username}: ${comment.content}",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(Date(comment.timestamp)),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input de nuevo comentario
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un comentario...") },
                    singleLine = true
                )
                IconButton(onClick = {
                    val text = commentText.text.trim()
                    if (text.isNotEmpty() && currentUser != null) {
                        auth.currentUser?.let { user ->
                            db.collection("users").document(user.uid).get()
                                .addOnSuccessListener { userDoc ->
                                    val username = userDoc.getString("username") ?: "Anónimo"
                                    val comment = mapOf(
                                        "userId" to user.uid,
                                        "username" to username,
                                        "content" to text,
                                        "timestamp" to System.currentTimeMillis()
                                    )

                                    db.collection("posts").document(post.id)
                                        .collection("comments")
                                        .add(comment)
                                        .addOnSuccessListener {
                                            commentText = TextFieldValue("")
                                        }
                                }
                        }
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar comentario")
                }
            }
        }
    }
}

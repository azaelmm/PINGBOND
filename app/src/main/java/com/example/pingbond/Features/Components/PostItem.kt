package com.example.pingbond.Features.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pingbond.Features.ViewModels.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PostItem(post: Post, onLikeClick: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser?.uid

    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(post.likesCount) }

    // Verificar si el usuario ya dio like
    LaunchedEffect(post.id, currentUser) {
        if (currentUser != null) {
            db.collection("posts").document(post.id)
                .collection("likes").document(currentUser)
                .get()
                .addOnSuccessListener { document ->
                    isLiked = document.exists()
                }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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

            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Publicación",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(post.content)

            Spacer(modifier = Modifier.height(8.dp))

            // Sección de Likes
            Row(verticalAlignment = Alignment.CenterVertically) {
                var isProcessingLike by remember { mutableStateOf(false) }

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
                Text(text = "${post.likesCount} likes", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

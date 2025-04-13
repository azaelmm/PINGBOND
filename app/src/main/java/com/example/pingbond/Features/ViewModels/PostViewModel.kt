package com.example.pingbond.Features.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val profilePic: String? = null,
    val imageUrl: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val likesCount: Int = 0,
    val isLikedByCurrentUser: Boolean = false
)

class PostViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var lastVisiblePost: DocumentSnapshot? = null
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts
    private var isLoading = false

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        if (isLoading) return
        isLoading = true

        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUser == null) {
            Log.e("FETCH_POSTS", "Usuario no logueado")
            isLoading = false
            return
        }

        var query = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)

        if (lastVisiblePost != null) {
            query = query.startAfter(lastVisiblePost)
        }

        query.get().addOnSuccessListener { snapshot ->
            val docs = snapshot.documents
            if (docs.isEmpty()) {
                Log.d("FETCH_POSTS", "No hay más posts")
                isLoading = false
                return@addOnSuccessListener
            }

            val newPosts = mutableListOf<Post>()

            for (doc in docs) {
                val postId = doc.id
                val userId = doc.getString("userId") ?: continue

                db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                    val profilePicUrl = userDoc.getString("profilePic")

                    val basePost = Post(
                        id = postId,
                        userId = userId,
                        username = doc.getString("username") ?: "Desconocido",
                        profilePic = profilePicUrl, // ✅ desde users
                        imageUrl = doc.getString("imageUrl") ?: "",
                        content = doc.getString("content") ?: "",
                        timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L,
                        likesCount = (doc.getLong("likesCount") ?: 0).toInt()
                    )

                    db.collection("posts").document(postId)
                        .collection("likes").document(currentUser)
                        .get()
                        .addOnSuccessListener { likeDoc ->
                            val liked = likeDoc.exists()
                            newPosts.add(basePost.copy(isLikedByCurrentUser = liked))
                            Log.d("FETCH_POSTS", "Post $postId liked: $liked")

                            if (newPosts.size == docs.size) {
                                _posts.value = _posts.value + newPosts
                                lastVisiblePost = docs.lastOrNull()
                                isLoading = false
                                Log.d("FETCH_POSTS", "Posts cargados: ${_posts.value.size}")
                            }
                        }.addOnFailureListener { e ->
                            Log.e("FETCH_POSTS", "Error al obtener like de $postId", e)
                            isLoading = false
                        }

                }.addOnFailureListener { e ->
                    Log.e("FETCH_POSTS", "Error al obtener datos del usuario $userId", e)
                    isLoading = false
                }
            }

        }.addOnFailureListener { e ->
            Log.e("FETCH_POSTS", "Error al obtener posts", e)
            isLoading = false
        }
    }




    fun toggleLike(postId: String, userId: String) {
        val postRef = db.collection("posts").document(postId)
        val likeRef = postRef.collection("likes").document(userId)

        db.runTransaction { transaction ->
            val postSnapshot = transaction.get(postRef)
            val likeSnapshot = transaction.get(likeRef)

            val currentLikes = postSnapshot.getLong("likesCount") ?: 0

            if (likeSnapshot.exists()) {
                // Si el usuario ya ha dado like, lo eliminamos
                transaction.delete(likeRef)
                transaction.update(postRef, "likesCount", (currentLikes - 1).coerceAtLeast(0))
            } else {
                // Si el usuario no ha dado like, lo agregamos
                transaction.set(likeRef, mapOf("likedAt" to System.currentTimeMillis()))
                transaction.update(postRef, "likesCount", currentLikes + 1)
            }
        }.addOnSuccessListener {
            Log.d("LIKE", "Like actualizado correctamente")
        }.addOnFailureListener { e ->
            Log.e("LIKE", "Error al actualizar like", e)
        }
    }
}

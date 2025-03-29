package com.example.pingbond.Features.ViewModels

import androidx.lifecycle.ViewModel
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
    val timestamp: Long = 0L
)

class PostViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var lastVisiblePost: DocumentSnapshot? = null  // 🔥 Cambio aquí (antes era Query?)
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts
    private var isLoading = false

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        if (isLoading) return
        isLoading = true

        var query = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)

        if (lastVisiblePost != null) {
            query = query.startAfter(lastVisiblePost)  // 🔥 Esto ahora funcionará correctamente
        }

        query.get().addOnSuccessListener { snapshot ->
            val newPosts = snapshot.documents.map { doc ->
                Post(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    username = doc.getString("username") ?: "Desconocido",
                    profilePic = doc.getString("profilePic"),
                    imageUrl = doc.getString("imageUrl") ?: "",
                    content = doc.getString("content") ?: "",
                    timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L  // 🔥 Conversión segura
                )
            }
            _posts.value = _posts.value + newPosts
            lastVisiblePost = snapshot.documents.lastOrNull()
            isLoading = false
        }.addOnFailureListener {
            isLoading = false
        }

    }
}

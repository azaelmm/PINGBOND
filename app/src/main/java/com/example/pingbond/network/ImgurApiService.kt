package com.example.pingbond.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ImgurApiService {
    @Multipart
    @POST("image")
    suspend fun uploadImage(
        @Header("Authorization") authorization: String,
        @Part image: MultipartBody.Part,
        @Part("title") title: RequestBody?
    ): ImgurResponse

    companion object {
        fun create(): ImgurApiService {
            return Retrofit.Builder()
                .baseUrl("https://api.imgur.com/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ImgurApiService::class.java)
        }
    }
}

data class ImgurResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: ImgurData
)

data class ImgurData(
    @SerializedName("link") val link: String
)

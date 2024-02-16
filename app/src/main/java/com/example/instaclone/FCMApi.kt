package com.example.instaclone

import com.example.instaclone.data.SendMessageDto
import com.example.instaclone.util.API_KEY
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FCMApi {

    @Headers("Authorization: key=${API_KEY}")
    @POST("fcm/send")
    suspend fun sendMessage(
        @Body body: SendMessageDto
    )
}
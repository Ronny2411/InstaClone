package com.example.instaclone.data

data class ChatData(
    val chatId: String? = "",
    val userOne: ChatUser = ChatUser(),
    val userTwo: ChatUser = ChatUser()
)

data class ChatUser(
    val userId: String? = "",
    val username: String? = "",
    val name: String? = "",
    val imageUrl: String? = "",
)

data class Message(
    val sentBy: String? = "",
    val message: String? = "",
    val timestamp: String? = ""
)
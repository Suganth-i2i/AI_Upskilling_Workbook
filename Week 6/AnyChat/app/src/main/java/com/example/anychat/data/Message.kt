package com.example.anychat.data

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val read: Boolean = false
) 
package com.example.anychat.data

data class User(
    val uid: String = "",
    val email: String = "",
    val online: Boolean = false,
    val lastSeen: Long = 0L,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0
) 
package com.example.anychat.data

data class MergedUserChat(
    val uid: String = "",
    val email: String = "",
    val online: Boolean = false,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0
) 
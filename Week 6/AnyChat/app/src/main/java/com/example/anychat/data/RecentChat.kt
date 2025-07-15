package com.example.anychat.data

data class RecentChat(
    val chatId: String = "",
    val otherUserId: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0
) 
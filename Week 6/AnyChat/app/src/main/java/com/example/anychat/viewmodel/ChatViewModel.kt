package com.example.anychat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.anychat.data.ChatRepository

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    fun getMessagesQuery(chatId: String) = repository.getMessagesQuery(chatId)
} 
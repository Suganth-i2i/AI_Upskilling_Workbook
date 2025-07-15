package com.example.anychat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.anychat.data.ChatRepository
import com.example.anychat.data.RecentChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class RecentChatsViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _recentChats = MutableLiveData<List<RecentChat>>()
    val recentChats: LiveData<List<RecentChat>> = _recentChats
    private var listenerRegistration: ListenerRegistration? = null

    fun listenForRecentChats(userId: String) {
        listenerRegistration?.remove()
        listenerRegistration = repository.listenForRecentChats(userId) { chats ->
            _recentChats.postValue(chats)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
} 
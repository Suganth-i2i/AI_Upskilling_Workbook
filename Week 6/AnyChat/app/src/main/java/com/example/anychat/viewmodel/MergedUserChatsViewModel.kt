package com.example.anychat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.anychat.data.ChatRepository
import com.example.anychat.data.MergedUserChat
import com.google.firebase.firestore.ListenerRegistration

class MergedUserChatsViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _mergedChats = MutableLiveData<List<MergedUserChat>>()
    val mergedChats: LiveData<List<MergedUserChat>> = _mergedChats
    private var listenerRegistration: ListenerRegistration? = null

    fun listenForMergedUserChats(currentUid: String) {
        listenerRegistration?.remove()
        repository.listenForMergedUserChats(currentUid) { mergedList ->
            _mergedChats.postValue(mergedList)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
} 
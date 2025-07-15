package com.example.anychat.viewmodel

import androidx.lifecycle.*
import com.example.anychat.data.User
import com.example.anychat.data.UserRepository
import com.google.firebase.firestore.ListenerRegistration

class UsersViewModel(private val repository: UserRepository) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    private var listenerRegistration: ListenerRegistration? = null

    fun listenForUsers(currentUid: String) {
        listenerRegistration?.remove()
        listenerRegistration = repository.listenForUsersWithLastMessage(currentUid) { userList ->
            _users.postValue(userList)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
} 
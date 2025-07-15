package com.example.anychat.viewmodel

import androidx.lifecycle.*
import com.example.anychat.data.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authResult = MutableLiveData<Result<FirebaseUser?>>()
    val authResult: LiveData<Result<FirebaseUser?>> = _authResult

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authResult.value = repository.register(email, password)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authResult.value = repository.login(email, password)
        }
    }

    fun logout() = repository.logout()
} 
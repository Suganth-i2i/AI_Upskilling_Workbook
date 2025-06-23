package com.example.week3aitasks.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.week3aitasks.data.UserProfile
import com.example.week3aitasks.data.UserProfileRepository
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class UserProfileViewModel(private val repository: UserProfileRepository) : ViewModel() {
    
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile
    
    private val _userProfiles = MutableLiveData<List<UserProfile>>()
    val userProfiles: LiveData<List<UserProfile>> = _userProfiles
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _validationErrors = MutableLiveData<Map<String, String>>()
    val validationErrors: LiveData<Map<String, String>> = _validationErrors
    
    fun loadUserProfile(userId: Long) {
        viewModelScope.launch {
            try {
                setLoading(true)
                clearError()
                
                val profile = repository.getUserProfile(userId)
                if (profile != null) {
                    _userProfile.value = profile
                } else {
                    setError("User profile not found")
                }
            } catch (e: Exception) {
                setError("Failed to load user profile: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun saveUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            try {
                setLoading(true)
                clearError()
                
                val savedId = repository.saveUserProfile(userProfile)
                val savedProfile = userProfile.copy(id = savedId)
                _userProfile.value = savedProfile
            } catch (e: Exception) {
                setError("Failed to save user profile: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun loadAllUserProfiles() {
        viewModelScope.launch {
            try {
                setLoading(true)
                clearError()
                
                val profiles = repository.getAllUserProfiles()
                _userProfiles.value = profiles
            } catch (e: Exception) {
                setError("Failed to load user profiles: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun deleteUserProfile(userId: Long) {
        viewModelScope.launch {
            try {
                setLoading(true)
                clearError()
                
                val success = repository.deleteUserProfile(userId)
                if (!success) {
                    setError("Failed to delete user profile")
                }
            } catch (e: Exception) {
                setError("Failed to delete user profile: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun searchProfiles(query: String) {
        viewModelScope.launch {
            try {
                val results = repository.searchUserProfiles(query)
                _userProfiles.value = results
            } catch (e: Exception) {
                setError("Failed to search profiles: ${e.message}")
            }
        }
    }
    
    fun validateProfile(userProfile: UserProfile): Boolean {
        val errors = getValidationErrors(userProfile)
        _validationErrors.value = errors
        return errors.isEmpty()
    }
    
    fun getValidationErrors(userProfile: UserProfile): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        if (userProfile.firstName.isBlank()) {
            errors["firstName"] = "First name is required"
        }
        
        if (userProfile.lastName.isBlank()) {
            errors["lastName"] = "Last name is required"
        }
        
        if (userProfile.email.isBlank()) {
            errors["email"] = "Email is required"
        } else if (!isValidEmail(userProfile.email)) {
            errors["email"] = "Invalid email format"
        }
        
        userProfile.phone?.let { phone ->
            if (phone.isNotBlank() && !repository.isValidPhone(phone)) {
                errors["phone"] = "Invalid phone number format"
            }
        }
        
        userProfile.dateOfBirth?.let { dateOfBirth ->
            if (!repository.isValidDateOfBirth(dateOfBirth)) {
                errors["dateOfBirth"] = "Invalid date of birth"
            }
        }
        
        return errors
    }
    
    fun isValidEmail(email: String): Boolean {
        return repository.isValidEmail(email)
    }
    
    fun loadProfileStatistics() {
        viewModelScope.launch {
            try {
                repository.getProfileCompletionStatistics()
                // Statistics could be stored in LiveData if needed for UI
            } catch (e: Exception) {
                setError("Failed to load profile statistics: ${e.message}")
            }
        }
    }
    
    fun setError(message: String) {
        _error.value = message
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    fun updateCurrentProfile(profile: UserProfile) {
        _userProfile.value = profile
    }
} 
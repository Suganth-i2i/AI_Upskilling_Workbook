package com.example.week3aitasks.data

import java.time.LocalDate
import java.util.regex.Pattern

data class ProfileCompletionStatistics(
    val totalProfiles: Int,
    val completedProfiles: Int,
    val incompleteProfiles: Int,
    val completionPercentage: Double
)

class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    
    suspend fun getUserProfile(userId: Long): UserProfile? {
        return userProfileDao.getUserProfile(userId)
    }
    
    suspend fun getAllUserProfiles(): List<UserProfile> {
        return userProfileDao.getAllUserProfiles()
    }
    
    suspend fun saveUserProfile(userProfile: UserProfile): Long {
        return if (userProfile.id == 0L) {
            // New profile
            val result = userProfileDao.insertUserProfile(userProfile)
            if (result == -1L) {
                throw RuntimeException("Failed to save user profile")
            }
            result
        } else {
            // Update existing profile
            val result = userProfileDao.updateUserProfile(userProfile)
            if (result == 0) {
                throw RuntimeException("Failed to update user profile")
            }
            userProfile.id
        }
    }
    
    suspend fun deleteUserProfile(userId: Long): Boolean {
        val result = userProfileDao.deleteUserProfile(userId)
        return result > 0
    }
    
    suspend fun searchUserProfiles(query: String): List<UserProfile> {
        return if (query.isBlank()) {
            getAllUserProfiles()
        } else {
            userProfileDao.searchUserProfiles("%$query%")
        }
    }
    
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }
    
    fun isValidPhone(phone: String): Boolean {
        if (phone.isBlank()) return false
        // Basic phone validation - at least 10 digits
        val phonePattern = Pattern.compile("^[+]?[0-9]{10,}$")
        return phonePattern.matcher(phone.replace("\\s".toRegex(), "")).matches()
    }
    
    fun isValidDateOfBirth(dateOfBirth: LocalDate): Boolean {
        val today = LocalDate.now()
        return dateOfBirth.isBefore(today) && dateOfBirth.isAfter(today.minusYears(150))
    }
    
    suspend fun getProfileCompletionStatistics(): ProfileCompletionStatistics {
        val totalProfiles = userProfileDao.getProfileCount()
        val completedProfiles = userProfileDao.getCompletedProfileCount()
        val incompleteProfiles = totalProfiles - completedProfiles
        val completionPercentage = if (totalProfiles > 0) {
            (completedProfiles.toDouble() / totalProfiles.toDouble()) * 100.0
        } else {
            0.0
        }
        
        return ProfileCompletionStatistics(
            totalProfiles = totalProfiles,
            completedProfiles = completedProfiles,
            incompleteProfiles = incompleteProfiles,
            completionPercentage = completionPercentage
        )
    }
} 
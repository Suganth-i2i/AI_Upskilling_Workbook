package com.example.week3aitasks.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Period

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String?,
    val email: String,
    val phone: String? = null,
    val dateOfBirth: LocalDate? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
) {
    
    /**
     * Returns the full name combining first and last name
     */
    fun getFullName(): String {
        return if (lastName != null) {
            "$firstName $lastName"
        } else {
            firstName
        }
    }
    
    /**
     * Returns the display name (same as full name for now)
     */
    fun getDisplayName(): String {
        return getFullName()
    }
    
    /**
     * Calculates and returns the age based on date of birth
     */
    fun getAge(): Int? {
        return dateOfBirth?.let { birthDate ->
            Period.between(birthDate, LocalDate.now()).years
        }
    }
    
    /**
     * Returns initials from first and last name
     */
    fun getInitials(): String {
        val firstInitial = firstName.takeIf { it.isNotEmpty() }?.first()?.uppercase() ?: ""
        val lastInitial = lastName?.takeIf { it.isNotEmpty() }?.first()?.uppercase() ?: ""
        return firstInitial + lastInitial
    }
    
    /**
     * Checks if the profile is complete with all required fields
     */
    fun isProfileComplete(): Boolean {
        return firstName.isNotEmpty() &&
                lastName?.isNotEmpty() == true &&
                email.isNotEmpty() &&
                phone?.isNotEmpty() == true &&
                dateOfBirth != null &&
                bio?.isNotEmpty() == true
    }
} 
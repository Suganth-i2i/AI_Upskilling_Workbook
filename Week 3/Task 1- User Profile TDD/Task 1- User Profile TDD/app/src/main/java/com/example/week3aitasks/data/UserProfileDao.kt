package com.example.week3aitasks.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    
    @Query("SELECT * FROM user_profiles WHERE id = :userId")
    suspend fun getUserProfile(userId: Long): UserProfile?
    
    @Query("SELECT * FROM user_profiles ORDER BY firstName ASC")
    suspend fun getAllUserProfiles(): List<UserProfile>
    
    @Query("SELECT * FROM user_profiles WHERE firstName LIKE :searchQuery OR lastName LIKE :searchQuery ORDER BY firstName ASC")
    suspend fun searchUserProfiles(searchQuery: String): List<UserProfile>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile): Long
    
    @Update
    suspend fun updateUserProfile(userProfile: UserProfile): Int
    
    @Query("DELETE FROM user_profiles WHERE id = :userId")
    suspend fun deleteUserProfile(userId: Long): Int
    
    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getProfileCount(): Int
    
    @Query("SELECT COUNT(*) FROM user_profiles WHERE firstName IS NOT NULL AND firstName != '' AND lastName IS NOT NULL AND lastName != '' AND email IS NOT NULL AND email != '' AND phone IS NOT NULL AND phone != '' AND dateOfBirth IS NOT NULL AND bio IS NOT NULL AND bio != ''")
    suspend fun getCompletedProfileCount(): Int
} 
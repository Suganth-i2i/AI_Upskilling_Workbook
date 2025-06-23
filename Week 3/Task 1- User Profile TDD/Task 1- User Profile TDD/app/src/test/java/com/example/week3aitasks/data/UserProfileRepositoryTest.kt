package com.example.week3aitasks.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.*
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("UserProfileRepository Tests")
class UserProfileRepositoryTest {

    private lateinit var repository: UserProfileRepository
    private lateinit var userProfileDao: UserProfileDao

    @BeforeEach
    fun setUp() {
        userProfileDao = mock()
        repository = UserProfileRepository(userProfileDao)
    }

    @Nested
    @DisplayName("Get User Profile Tests")
    inner class GetUserProfileTests {

        @Test
        @DisplayName("Should return user profile when exists")
        fun shouldReturnUserProfileWhenExists() = runTest {
            // Given
            val userId = 1L
            val expectedProfile = UserProfile(
                id = userId,
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )
            whenever(userProfileDao.getUserProfile(userId)).thenReturn(expectedProfile)

            // When
            val result = repository.getUserProfile(userId)

            // Then
            assertEquals(expectedProfile, result)
            verify(userProfileDao).getUserProfile(userId)
        }

        @Test
        @DisplayName("Should return null when user profile does not exist")
        fun shouldReturnNullWhenUserProfileDoesNotExist() = runTest {
            // Given
            val userId = 999L
            whenever(userProfileDao.getUserProfile(userId)).thenReturn(null)

            // When
            val result = repository.getUserProfile(userId)

            // Then
            assertNull(result)
            verify(userProfileDao).getUserProfile(userId)
        }

        @Test
        @DisplayName("Should return all user profiles")
        fun shouldReturnAllUserProfiles() = runTest {
            // Given
            val expectedProfiles = listOf(
                UserProfile(
                    id = 1L,
                    firstName = "John",
                    lastName = "Doe",
                    email = "john.doe@example.com"
                ),
                UserProfile(
                    id = 2L,
                    firstName = "Jane",
                    lastName = "Smith",
                    email = "jane.smith@example.com"
                )
            )
            whenever(userProfileDao.getAllUserProfiles()).thenReturn(expectedProfiles)

            // When
            val result = repository.getAllUserProfiles()

            // Then
            assertEquals(expectedProfiles, result)
            verify(userProfileDao).getAllUserProfiles()
        }

        @Test
        @DisplayName("Should return empty list when no user profiles exist")
        fun shouldReturnEmptyListWhenNoUserProfilesExist() = runTest {
            // Given
            whenever(userProfileDao.getAllUserProfiles()).thenReturn(emptyList())

            // When
            val result = repository.getAllUserProfiles()

            // Then
            assertTrue(result.isEmpty())
            verify(userProfileDao).getAllUserProfiles()
        }
    }

    @Nested
    @DisplayName("Save User Profile Tests")
    inner class SaveUserProfileTests {

        @Test
        @DisplayName("Should save new user profile successfully")
        fun shouldSaveNewUserProfileSuccessfully() = runTest {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )
            val savedId = 1L
            whenever(userProfileDao.insertUserProfile(userProfile)).thenReturn(savedId)

            // When
            val result = repository.saveUserProfile(userProfile)

            // Then
            assertEquals(savedId, result)
            verify(userProfileDao).insertUserProfile(userProfile)
        }

        @Test
        @DisplayName("Should update existing user profile successfully")
        fun shouldUpdateExistingUserProfileSuccessfully() = runTest {
            // Given
            val userProfile = UserProfile(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )
            whenever(userProfileDao.updateUserProfile(userProfile)).thenReturn(1)

            // When
            val result = repository.saveUserProfile(userProfile)

            // Then
            assertEquals(userProfile.id, result)
            verify(userProfileDao).updateUserProfile(userProfile)
            verify(userProfileDao, never()).insertUserProfile(any())
        }

        @Test
        @DisplayName("Should handle save failure gracefully")
        fun shouldHandleSaveFailureGracefully() = runTest {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )
            whenever(userProfileDao.insertUserProfile(userProfile)).thenReturn(-1L)

            // When & Then
            assertThrows(RuntimeException::class.java) {
                repository.saveUserProfile(userProfile)
            }
            verify(userProfileDao).insertUserProfile(userProfile)
        }
    }

    @Nested
    @DisplayName("Delete User Profile Tests")
    inner class DeleteUserProfileTests {

        @Test
        @DisplayName("Should delete user profile successfully")
        fun shouldDeleteUserProfileSuccessfully() = runTest {
            // Given
            val userId = 1L
            whenever(userProfileDao.deleteUserProfile(userId)).thenReturn(1)

            // When
            val result = repository.deleteUserProfile(userId)

            // Then
            assertTrue(result)
            verify(userProfileDao).deleteUserProfile(userId)
        }

        @Test
        @DisplayName("Should return false when user profile does not exist for deletion")
        fun shouldReturnFalseWhenUserProfileDoesNotExistForDeletion() = runTest {
            // Given
            val userId = 999L
            whenever(userProfileDao.deleteUserProfile(userId)).thenReturn(0)

            // When
            val result = repository.deleteUserProfile(userId)

            // Then
            assertFalse(result)
            verify(userProfileDao).deleteUserProfile(userId)
        }
    }

    @Nested
    @DisplayName("Search User Profiles Tests")
    inner class SearchUserProfilesTests {

        @Test
        @DisplayName("Should search user profiles by name")
        fun shouldSearchUserProfilesByName() = runTest {
            // Given
            val searchQuery = "John"
            val expectedProfiles = listOf(
                UserProfile(
                    id = 1L,
                    firstName = "John",
                    lastName = "Doe",
                    email = "john.doe@example.com"
                )
            )
            whenever(userProfileDao.searchUserProfiles("%$searchQuery%")).thenReturn(expectedProfiles)

            // When
            val result = repository.searchUserProfiles(searchQuery)

            // Then
            assertEquals(expectedProfiles, result)
            verify(userProfileDao).searchUserProfiles("%$searchQuery%")
        }

        @Test
        @DisplayName("Should return empty list when no profiles match search")
        fun shouldReturnEmptyListWhenNoProfilesMatchSearch() = runTest {
            // Given
            val searchQuery = "NonExistent"
            whenever(userProfileDao.searchUserProfiles("%$searchQuery%")).thenReturn(emptyList())

            // When
            val result = repository.searchUserProfiles(searchQuery)

            // Then
            assertTrue(result.isEmpty())
            verify(userProfileDao).searchUserProfiles("%$searchQuery%")
        }

        @Test
        @DisplayName("Should handle empty search query")
        fun shouldHandleEmptySearchQuery() = runTest {
            // Given
            val searchQuery = ""
            val allProfiles = listOf(
                UserProfile(
                    id = 1L,
                    firstName = "John",
                    lastName = "Doe",
                    email = "john.doe@example.com"
                )
            )
            whenever(userProfileDao.getAllUserProfiles()).thenReturn(allProfiles)

            // When
            val result = repository.searchUserProfiles(searchQuery)

            // Then
            assertEquals(allProfiles, result)
            verify(userProfileDao).getAllUserProfiles()
            verify(userProfileDao, never()).searchUserProfiles(any())
        }
    }

    @Nested
    @DisplayName("Profile Validation Tests")
    inner class ProfileValidationTests {

        @Test
        @DisplayName("Should validate email format correctly")
        fun shouldValidateEmailFormatCorrectly() = runTest {
            // Given
            val validEmail = "john.doe@example.com"
            val invalidEmail = "invalid-email"

            // When & Then
            assertTrue(repository.isValidEmail(validEmail))
            assertFalse(repository.isValidEmail(invalidEmail))
        }

        @Test
        @DisplayName("Should validate phone format correctly")
        fun shouldValidatePhoneFormatCorrectly() = runTest {
            // Given
            val validPhone = "+1234567890"
            val invalidPhone = "123"

            // When & Then
            assertTrue(repository.isValidPhone(validPhone))
            assertFalse(repository.isValidPhone(invalidPhone))
        }

        @Test
        @DisplayName("Should validate date of birth correctly")
        fun shouldValidateDateOfBirthCorrectly() = runTest {
            // Given
            val validDate = LocalDate.now().minusYears(18)
            val invalidDate = LocalDate.now().plusYears(1)

            // When & Then
            assertTrue(repository.isValidDateOfBirth(validDate))
            assertFalse(repository.isValidDateOfBirth(invalidDate))
        }
    }

    @Nested
    @DisplayName("Profile Statistics Tests")
    inner class ProfileStatisticsTests {

        @Test
        @DisplayName("Should get profile completion statistics")
        fun shouldGetProfileCompletionStatistics() = runTest {
            // Given
            val profiles = listOf(
                UserProfile(
                    id = 1L,
                    firstName = "John",
                    lastName = "Doe",
                    email = "john.doe@example.com",
                    phone = "+1234567890",
                    dateOfBirth = LocalDate.of(1990, 1, 1),
                    bio = "Software developer"
                ),
                UserProfile(
                    id = 2L,
                    firstName = "Jane",
                    lastName = "Smith",
                    email = "jane.smith@example.com"
                )
            )
            whenever(userProfileDao.getAllUserProfiles()).thenReturn(profiles)

            // When
            val stats = repository.getProfileCompletionStatistics()

            // Then
            assertEquals(2, stats.totalProfiles)
            assertEquals(1, stats.completedProfiles)
            assertEquals(1, stats.incompleteProfiles)
            assertEquals(50.0, stats.completionPercentage, 0.01)
        }

        @Test
        @DisplayName("Should handle empty profiles for statistics")
        fun shouldHandleEmptyProfilesForStatistics() = runTest {
            // Given
            whenever(userProfileDao.getAllUserProfiles()).thenReturn(emptyList())

            // When
            val stats = repository.getProfileCompletionStatistics()

            // Then
            assertEquals(0, stats.totalProfiles)
            assertEquals(0, stats.completedProfiles)
            assertEquals(0, stats.incompleteProfiles)
            assertEquals(0.0, stats.completionPercentage, 0.01)
        }
    }
} 
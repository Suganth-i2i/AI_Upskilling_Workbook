package com.example.week3aitasks.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.week3aitasks.data.UserProfile
import com.example.week3aitasks.data.UserProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.*
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("UserProfileViewModel Tests")
class UserProfileViewModelTest {

    @JvmField
    @RegisterExtension
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: UserProfileViewModel
    private lateinit var repository: UserProfileRepository
    private lateinit var profileObserver: Observer<UserProfile?>
    private lateinit var loadingObserver: Observer<Boolean>
    private lateinit var errorObserver: Observer<String?>
    private lateinit var profilesObserver: Observer<List<UserProfile>>
    private lateinit var validationObserver: Observer<Map<String, String>>

    @BeforeEach
    fun setUp() {
        repository = mock()
        viewModel = UserProfileViewModel(repository)
        
        profileObserver = mock()
        loadingObserver = mock()
        errorObserver = mock()
        profilesObserver = mock()
        validationObserver = mock()
        
        viewModel.userProfile.observeForever(profileObserver)
        viewModel.isLoading.observeForever(loadingObserver)
        viewModel.error.observeForever(errorObserver)
        viewModel.userProfiles.observeForever(profilesObserver)
        viewModel.validationErrors.observeForever(validationObserver)
    }

    @Nested
    @DisplayName("Load User Profile Tests")
    inner class LoadUserProfileTests {

        @Test
        @DisplayName("Should load user profile successfully")
        fun shouldLoadUserProfileSuccessfully() = runTest {
            // Given
            val userId = 1L
            val expectedProfile = UserProfile(
                id = userId,
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )
            whenever(repository.getUserProfile(userId)).thenReturn(expectedProfile)

            // When
            viewModel.loadUserProfile(userId)

            // Then
            verify(loadingObserver).onChanged(true)
            verify(loadingObserver).onChanged(false)
            verify(profileObserver).onChanged(expectedProfile)
            verify(errorObserver).onChanged(null)
            verify(repository).getUserProfile(userId)
        }

        @Test
        @DisplayName("Should handle profile not found")
        fun shouldHandleProfileNotFound() = runTest {
            // Given
            val userId = 999L
            whenever(repository.getUserProfile(userId)).thenReturn(null)

            // When
            viewModel.loadUserProfile(userId)

            // Then
            verify(loadingObserver).onChanged(true)
            verify(loadingObserver).onChanged(false)
            verify(profileObserver).onChanged(null)
            verify(errorObserver).onChanged("User profile not found")
            verify(repository).getUserProfile(userId)
        }

        @Test
        @DisplayName("Should handle repository exception")
        fun shouldHandleRepositoryException() = runTest {
            // Given
            val userId = 1L
            val exception = RuntimeException("Database error")
            whenever(repository.getUserProfile(userId)).thenThrow(exception)

            // When
            viewModel.loadUserProfile(userId)

            // Then
            verify(loadingObserver).onChanged(true)
            verify(loadingObserver).onChanged(false)
            verify(errorObserver).onChanged("Failed to load user profile: Database error")
            verify(repository).getUserProfile(userId)
        }
    }

    @Nested
    @DisplayName("Save User Profile Tests")
    inner class SaveUserProfileTests {

        @Test
        @DisplayName("Should save user profile successfully")
        fun shouldSaveUserProfileSuccessfully() = runTest {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )
            val savedId = 1L
            whenever(repository.saveUserProfile(userProfile)).thenReturn(savedId)

            // When
            viewModel.saveUserProfile(userProfile)

            // Then
            verify(loadingObserver).onChanged(true)
            verify(loadingObserver).onChanged(false)
            verify(profileObserver).onChanged(userProfile.copy(id = savedId))
            verify(errorObserver).onChanged(null)
            verify(repository).saveUserProfile(userProfile)
        }

        @Test
        @DisplayName("Should handle save failure")
        fun shouldHandleSaveFailure() = runTest {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )
            val exception = RuntimeException("Save failed")
            whenever(repository.saveUserProfile(userProfile)).thenThrow(exception)

            // When
            viewModel.saveUserProfile(userProfile)

            // Then
            verify(loadingObserver).onChanged(true)
            verify(loadingObserver).onChanged(false)
            verify(errorObserver).onChanged("Failed to save user profile: Save failed")
            verify(repository).saveUserProfile(userProfile)
        }
    }

    @Nested
    @DisplayName("Load All Profiles Tests")
    inner class LoadAllProfilesTests {

        @Test
        @DisplayName("Should load all user profiles successfully")
        fun shouldLoadAllUserProfilesSuccessfully() = runTest {
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
            whenever(repository.getAllUserProfiles()).thenReturn(expectedProfiles)

            // When
            viewModel.loadAllUserProfiles()

            // Then
            verify(loadingObserver).onChanged(true)
            verify(loadingObserver).onChanged(false)
            verify(profilesObserver).onChanged(expectedProfiles)
            verify(errorObserver).onChanged(null)
            verify(repository).getAllUserProfiles()
        }

        @Test
        @DisplayName("Should handle empty profiles list")
        fun shouldHandleEmptyProfilesList() = runTest {
            // Given
            whenever(repository.getAllUserProfiles()).thenReturn(emptyList())

            // When
            viewModel.loadAllUserProfiles()

            // Then
            verify(loadingObserver).onChanged(true)
            verify(loadingObserver).onChanged(false)
            verify(profilesObserver).onChanged(emptyList())
            verify(errorObserver).onChanged(null)
            verify(repository).getAllUserProfiles()
        }
    }

    @Nested
    @DisplayName("Delete Profile Tests")
    inner class DeleteProfileTests {

        @Test
        @DisplayName("Should delete user profile successfully")
        fun shouldDeleteUserProfileSuccessfully() = runTest {
            // Given
            val userId = 1L
            whenever(repository.deleteUserProfile(userId)).thenReturn(true)

            // When
            viewModel.deleteUserProfile(userId)

            // Then
            verify(loadingObserver).onChanged(true)
            verify(loadingObserver).onChanged(false)
            verify(errorObserver).onChanged(null)
            verify(repository).deleteUserProfile(userId)
        }

        @Test
        @DisplayName("Should handle delete failure")
        fun shouldHandleDeleteFailure() = runTest {
            // Given
            val userId = 999L
            whenever(repository.deleteUserProfile(userId)).thenReturn(false)

            // When
            viewModel.deleteUserProfile(userId)

            // Then
            verify(loadingObserver).onChanged(true)
            verify(loadingObserver).onChanged(false)
            verify(errorObserver).onChanged("Failed to delete user profile")
            verify(repository).deleteUserProfile(userId)
        }
    }

    @Nested
    @DisplayName("Search Profiles Tests")
    inner class SearchProfilesTests {

        @Test
        @DisplayName("Should search profiles successfully")
        fun shouldSearchProfilesSuccessfully() = runTest {
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
            whenever(repository.searchUserProfiles(searchQuery)).thenReturn(expectedProfiles)

            // When
            viewModel.searchProfiles(searchQuery)

            // Then
            verify(profilesObserver).onChanged(expectedProfiles)
            verify(repository).searchUserProfiles(searchQuery)
        }

        @Test
        @DisplayName("Should handle empty search results")
        fun shouldHandleEmptySearchResults() = runTest {
            // Given
            val searchQuery = "NonExistent"
            whenever(repository.searchUserProfiles(searchQuery)).thenReturn(emptyList())

            // When
            viewModel.searchProfiles(searchQuery)

            // Then
            verify(profilesObserver).onChanged(emptyList())
            verify(repository).searchUserProfiles(searchQuery)
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    inner class ValidationTests {

        @Test
        @DisplayName("Should validate profile data correctly")
        fun shouldValidateProfileDataCorrectly() = runTest {
            // Given
            val userProfile = UserProfile(
                firstName = "",
                lastName = "",
                email = "invalid-email"
            )

            // When
            val isValid = viewModel.validateProfile(userProfile)

            // Then
            assertFalse(isValid)
            verify(validationObserver).onChanged(any())
        }

        @Test
        @DisplayName("Should pass validation with valid data")
        fun shouldPassValidationWithValidData() = runTest {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )

            // When
            val isValid = viewModel.validateProfile(userProfile)

            // Then
            assertTrue(isValid)
            verify(validationObserver).onChanged(emptyMap())
        }

        @Test
        @DisplayName("Should validate email format")
        fun shouldValidateEmailFormat() = runTest {
            // Given
            val validEmail = "john.doe@example.com"
            val invalidEmail = "invalid-email"

            // When & Then
            assertTrue(viewModel.isValidEmail(validEmail))
            assertFalse(viewModel.isValidEmail(invalidEmail))
        }

        @Test
        @DisplayName("Should validate required fields")
        fun shouldValidateRequiredFields() = runTest {
            // Given
            val userProfile = UserProfile(
                firstName = "",
                lastName = "",
                email = ""
            )

            // When
            val errors = viewModel.getValidationErrors(userProfile)

            // Then
            assertTrue(errors.containsKey("firstName"))
            assertTrue(errors.containsKey("lastName"))
            assertTrue(errors.containsKey("email"))
        }
    }

    @Nested
    @DisplayName("Profile Statistics Tests")
    inner class ProfileStatisticsTests {

        @Test
        @DisplayName("Should get profile statistics")
        fun shouldGetProfileStatistics() = runTest {
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
            whenever(repository.getAllUserProfiles()).thenReturn(profiles)

            // When
            viewModel.loadProfileStatistics()

            // Then
            verify(repository).getAllUserProfiles()
        }
    }

    @Nested
    @DisplayName("UI State Management Tests")
    inner class UIStateManagementTests {

        @Test
        @DisplayName("Should clear error when starting new operation")
        fun shouldClearErrorWhenStartingNewOperation() = runTest {
            // Given
            viewModel.setError("Previous error")

            // When
            viewModel.clearError()

            // Then
            verify(errorObserver).onChanged(null)
        }

        @Test
        @DisplayName("Should set loading state correctly")
        fun shouldSetLoadingStateCorrectly() = runTest {
            // When
            viewModel.setLoading(true)

            // Then
            verify(loadingObserver).onChanged(true)

            // When
            viewModel.setLoading(false)

            // Then
            verify(loadingObserver).onChanged(false)
        }

        @Test
        @DisplayName("Should update current profile")
        fun shouldUpdateCurrentProfile() = runTest {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )

            // When
            viewModel.updateCurrentProfile(userProfile)

            // Then
            verify(profileObserver).onChanged(userProfile)
        }
    }
} 
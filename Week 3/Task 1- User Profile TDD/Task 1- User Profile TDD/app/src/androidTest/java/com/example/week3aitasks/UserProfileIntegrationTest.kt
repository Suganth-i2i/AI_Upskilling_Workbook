package com.example.week3aitasks

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.week3aitasks.data.AppDatabase
import com.example.week3aitasks.data.UserProfile
import com.example.week3aitasks.data.UserProfileDao
import com.example.week3aitasks.data.UserProfileRepository
import com.example.week3aitasks.ui.UserProfileViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class UserProfileIntegrationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var database: AppDatabase
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var repository: UserProfileRepository
    private lateinit var viewModel: UserProfileViewModel

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        userProfileDao = database.userProfileDao()
        repository = UserProfileRepository(userProfileDao)
        viewModel = UserProfileViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCompleteUserProfileFlow() = runBlocking {
        // Given
        val userProfile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "+1234567890",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            bio = "Software developer"
        )

        // When - Save profile
        val savedId = repository.saveUserProfile(userProfile)

        // Then - Verify save
        assert(savedId > 0)

        // When - Load profile
        val loadedProfile = repository.getUserProfile(savedId)

        // Then - Verify load
        assertNotNull(loadedProfile)
        assertEquals(userProfile.firstName, loadedProfile.firstName)
        assertEquals(userProfile.lastName, loadedProfile.lastName)
        assertEquals(userProfile.email, loadedProfile.email)
        assertEquals(userProfile.phone, loadedProfile.phone)
        assertEquals(userProfile.dateOfBirth, loadedProfile.dateOfBirth)
        assertEquals(userProfile.bio, loadedProfile.bio)
    }

    @Test
    fun testProfileValidationFlow() = runBlocking {
        // Given
        val invalidProfile = UserProfile(
            firstName = "",
            lastName = "",
            email = "invalid-email"
        )

        // When & Then - Validate invalid profile
        val isValid = viewModel.validateProfile(invalidProfile)
        assertFalse(isValid)

        // Given
        val validProfile = UserProfile(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com"
        )

        // When & Then - Validate valid profile
        val isValidValid = viewModel.validateProfile(validProfile)
        assertTrue(isValidValid)
    }

    @Test
    fun testProfileSearchFlow() = runBlocking {
        // Given
        val profiles = listOf(
            UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            ),
            UserProfile(
                firstName = "Jane",
                lastName = "Smith",
                email = "jane.smith@example.com"
            ),
            UserProfile(
                firstName = "Bob",
                lastName = "Johnson",
                email = "bob.johnson@example.com"
            )
        )

        // Save profiles
        profiles.forEach { repository.saveUserProfile(it) }

        // When - Search for "John"
        val searchResults = repository.searchUserProfiles("John")

        // Then - Verify search results
        assertEquals(1, searchResults.size)
        assertEquals("John", searchResults[0].firstName)

        // When - Search for "Smith"
        val smithResults = repository.searchUserProfiles("Smith")

        // Then - Verify search results
        assertEquals(1, smithResults.size)
        assertEquals("Smith", smithResults[0].lastName)
    }

    @Test
    fun testProfileStatisticsFlow() = runBlocking {
        // Given
        val completeProfile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "+1234567890",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            bio = "Software developer"
        )

        val incompleteProfile = UserProfile(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com"
        )

        // Save profiles
        repository.saveUserProfile(completeProfile)
        repository.saveUserProfile(incompleteProfile)

        // When
        val stats = repository.getProfileCompletionStatistics()

        // Then
        assertEquals(2, stats.totalProfiles)
        assertEquals(1, stats.completedProfiles)
        assertEquals(1, stats.incompleteProfiles)
        assertEquals(50.0, stats.completionPercentage, 0.01)
    }

    @Test
    fun testProfileUpdateFlow() = runBlocking {
        // Given
        val originalProfile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com"
        )

        // Save original profile
        val savedId = repository.saveUserProfile(originalProfile)

        // When - Update profile
        val updatedProfile = originalProfile.copy(
            id = savedId,
            firstName = "Jonathan",
            bio = "Updated bio"
        )
        val updateResult = repository.saveUserProfile(updatedProfile)

        // Then - Verify update
        assertEquals(savedId, updateResult)

        // When - Load updated profile
        val loadedProfile = repository.getUserProfile(savedId)

        // Then - Verify updated data
        assertNotNull(loadedProfile)
        assertEquals("Jonathan", loadedProfile.firstName)
        assertEquals("Updated bio", loadedProfile.bio)
    }

    @Test
    fun testProfileDeleteFlow() = runBlocking {
        // Given
        val userProfile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com"
        )

        // Save profile
        val savedId = repository.saveUserProfile(userProfile)

        // Verify profile exists
        assertNotNull(repository.getUserProfile(savedId))

        // When - Delete profile
        val deleteResult = repository.deleteUserProfile(savedId)

        // Then - Verify delete
        assertTrue(deleteResult)

        // Verify profile no longer exists
        assertNull(repository.getUserProfile(savedId))
    }

    @Test
    fun testProfileValidationRules() = runBlocking {
        // Test email validation
        assertTrue(repository.isValidEmail("john.doe@example.com"))
        assertFalse(repository.isValidEmail("invalid-email"))
        assertFalse(repository.isValidEmail(""))

        // Test phone validation
        assertTrue(repository.isValidPhone("+1234567890"))
        assertTrue(repository.isValidPhone("1234567890"))
        assertFalse(repository.isValidPhone("123"))
        assertFalse(repository.isValidPhone(""))

        // Test date of birth validation
        val validDate = LocalDate.now().minusYears(18)
        val invalidDate = LocalDate.now().plusYears(1)
        assertTrue(repository.isValidDateOfBirth(validDate))
        assertFalse(repository.isValidDateOfBirth(invalidDate))
    }

    @Test
    fun testProfileDataIntegrity() = runBlocking {
        // Given
        val profile1 = UserProfile(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com"
        )

        val profile2 = UserProfile(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com"
        )

        // Save profiles
        val id1 = repository.saveUserProfile(profile1)
        val id2 = repository.saveUserProfile(profile2)

        // When - Load all profiles
        val allProfiles = repository.getAllUserProfiles()

        // Then - Verify data integrity
        assertEquals(2, allProfiles.size)
        assertTrue(allProfiles.any { it.id == id1 && it.firstName == "John" })
        assertTrue(allProfiles.any { it.id == id2 && it.firstName == "Jane" })
    }

    @Test
    fun testProfileBusinessLogic() = runBlocking {
        // Given
        val userProfile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            dateOfBirth = LocalDate.of(1990, 1, 1)
        )

        // When & Then - Test business logic methods
        assertEquals("John Doe", userProfile.getFullName())
        assertEquals("John Doe", userProfile.getDisplayName())
        assertEquals("JD", userProfile.getInitials())
        assertEquals(33, userProfile.getAge()) // Assuming current year is 2023
        assertFalse(userProfile.isProfileComplete()) // Missing phone and bio

        // Given - Complete profile
        val completeProfile = userProfile.copy(
            phone = "+1234567890",
            bio = "Software developer"
        )

        // When & Then
        assertTrue(completeProfile.isProfileComplete())
    }

    @Test
    fun testProfileErrorHandling() = runBlocking {
        // Test handling of invalid data
        val invalidProfile = UserProfile(
            firstName = "",
            lastName = "",
            email = "invalid-email"
        )

        // Should not throw exception but return validation errors
        val errors = viewModel.getValidationErrors(invalidProfile)
        assertTrue(errors.isNotEmpty())
        assertTrue(errors.containsKey("firstName"))
        assertTrue(errors.containsKey("lastName"))
        assertTrue(errors.containsKey("email"))
    }
} 
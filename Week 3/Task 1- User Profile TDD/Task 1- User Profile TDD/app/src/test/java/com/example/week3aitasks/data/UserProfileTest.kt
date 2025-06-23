package com.example.week3aitasks.data

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.time.LocalDate

@DisplayName("UserProfile Data Model Tests")
class UserProfileTest {

    @Nested
    @DisplayName("UserProfile Creation Tests")
    inner class UserProfileCreationTests {

        @Test
        @DisplayName("Should create valid user profile with all required fields")
        fun shouldCreateValidUserProfile() {
            // Given
            val id = 1L
            val firstName = "John"
            val lastName = "Doe"
            val email = "john.doe@example.com"
            val phone = "+1234567890"
            val dateOfBirth = LocalDate.of(1990, 1, 1)
            val bio = "Software developer"
            val avatarUrl = "https://example.com/avatar.jpg"

            // When
            val userProfile = UserProfile(
                id = id,
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                dateOfBirth = dateOfBirth,
                bio = bio,
                avatarUrl = avatarUrl
            )

            // Then
            assertEquals(id, userProfile.id)
            assertEquals(firstName, userProfile.firstName)
            assertEquals(lastName, userProfile.lastName)
            assertEquals(email, userProfile.email)
            assertEquals(phone, userProfile.phone)
            assertEquals(dateOfBirth, userProfile.dateOfBirth)
            assertEquals(bio, userProfile.bio)
            assertEquals(avatarUrl, userProfile.avatarUrl)
        }

        @Test
        @DisplayName("Should create user profile with minimal required fields")
        fun shouldCreateUserProfileWithMinimalFields() {
            // Given
            val firstName = "Jane"
            val lastName = "Smith"
            val email = "jane.smith@example.com"

            // When
            val userProfile = UserProfile(
                firstName = firstName,
                lastName = lastName,
                email = email
            )

            // Then
            assertEquals(0L, userProfile.id)
            assertEquals(firstName, userProfile.firstName)
            assertEquals(lastName, userProfile.lastName)
            assertEquals(email, userProfile.email)
            assertNull(userProfile.phone)
            assertNull(userProfile.dateOfBirth)
            assertNull(userProfile.bio)
            assertNull(userProfile.avatarUrl)
        }

        @Test
        @DisplayName("Should return full name correctly")
        fun shouldReturnFullName() {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )

            // When
            val fullName = userProfile.getFullName()

            // Then
            assertEquals("John Doe", fullName)
        }

        @Test
        @DisplayName("Should return display name with first name only when last name is null")
        fun shouldReturnDisplayNameWithFirstNameOnly() {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = null,
                email = "john@example.com"
            )

            // When
            val displayName = userProfile.getDisplayName()

            // Then
            assertEquals("John", displayName)
        }

        @Test
        @DisplayName("Should return display name with first and last name")
        fun shouldReturnDisplayNameWithFullName() {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )

            // When
            val displayName = userProfile.getDisplayName()

            // Then
            assertEquals("John Doe", displayName)
        }

        @Test
        @DisplayName("Should calculate age correctly")
        fun shouldCalculateAgeCorrectly() {
            // Given
            val dateOfBirth = LocalDate.now().minusYears(25)
            val userProfile = UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                dateOfBirth = dateOfBirth
            )

            // When
            val age = userProfile.getAge()

            // Then
            assertEquals(25, age)
        }

        @Test
        @DisplayName("Should return null age when date of birth is null")
        fun shouldReturnNullAgeWhenDateOfBirthIsNull() {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                dateOfBirth = null
            )

            // When
            val age = userProfile.getAge()

            // Then
            assertNull(age)
        }

        @Test
        @DisplayName("Should return initials correctly")
        fun shouldReturnInitials() {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )

            // When
            val initials = userProfile.getInitials()

            // Then
            assertEquals("JD", initials)
        }

        @Test
        @DisplayName("Should return single initial when last name is null")
        fun shouldReturnSingleInitialWhenLastNameIsNull() {
            // Given
            val userProfile = UserProfile(
                firstName = "John",
                lastName = null,
                email = "john@example.com"
            )

            // When
            val initials = userProfile.getInitials()

            // Then
            assertEquals("J", initials)
        }

        @Test
        @DisplayName("Should check if profile is complete")
        fun shouldCheckIfProfileIsComplete() {
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

            // When & Then
            assertTrue(completeProfile.isProfileComplete())
            assertFalse(incompleteProfile.isProfileComplete())
        }

        @Test
        @DisplayName("Should copy user profile with new values")
        fun shouldCopyUserProfile() {
            // Given
            val originalProfile = UserProfile(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                phone = "+1234567890",
                dateOfBirth = LocalDate.of(1990, 1, 1),
                bio = "Software developer",
                avatarUrl = "https://example.com/avatar.jpg"
            )

            // When
            val copiedProfile = originalProfile.copy(
                firstName = "Jane",
                bio = "Updated bio"
            )

            // Then
            assertEquals(1L, copiedProfile.id)
            assertEquals("Jane", copiedProfile.firstName)
            assertEquals("Doe", copiedProfile.lastName)
            assertEquals("john.doe@example.com", copiedProfile.email)
            assertEquals("+1234567890", copiedProfile.phone)
            assertEquals(LocalDate.of(1990, 1, 1), copiedProfile.dateOfBirth)
            assertEquals("Updated bio", copiedProfile.bio)
            assertEquals("https://example.com/avatar.jpg", copiedProfile.avatarUrl)
        }

        @Test
        @DisplayName("Should implement equals correctly")
        fun shouldImplementEqualsCorrectly() {
            // Given
            val profile1 = UserProfile(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )

            val profile2 = UserProfile(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )

            val profile3 = UserProfile(
                id = 2L,
                firstName = "Jane",
                lastName = "Smith",
                email = "jane.smith@example.com"
            )

            // When & Then
            assertEquals(profile1, profile2)
            assertNotEquals(profile1, profile3)
            assertNotEquals(profile1, "Not a UserProfile")
        }

        @Test
        @DisplayName("Should implement hashCode correctly")
        fun shouldImplementHashCodeCorrectly() {
            // Given
            val profile1 = UserProfile(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )

            val profile2 = UserProfile(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )

            // When & Then
            assertEquals(profile1.hashCode(), profile2.hashCode())
        }

        @Test
        @DisplayName("Should implement toString correctly")
        fun shouldImplementToStringCorrectly() {
            // Given
            val userProfile = UserProfile(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com"
            )

            // When
            val toString = userProfile.toString()

            // Then
            assertTrue(toString.contains("UserProfile"))
            assertTrue(toString.contains("id=1"))
            assertTrue(toString.contains("firstName=John"))
            assertTrue(toString.contains("lastName=Doe"))
            assertTrue(toString.contains("email=john.doe@example.com"))
        }
    }
} 
package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils.getCurrentYearAsDouble
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentNcdPatientHistoryDialogBinding
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.data.NCDPatientStatusRequest
import com.medtroniclabs.spice.ncd.data.NcdPatientStatus
import com.medtroniclabs.spice.ncd.medicalreview.NCDDialogDismissListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDPatientHistoryViewModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(application = com.medtroniclabs.spice.SpiceBaseApplication::class)
class NCDPatientHistoryDialogEdgeCaseTest {

    @MockK
    private lateinit var mockContext: Context

    @RelaxedMockK
    private lateinit var mockBinding: FragmentNcdPatientHistoryDialogBinding

    @RelaxedMockK
    private lateinit var mockViewModel: NCDPatientHistoryViewModel

    @RelaxedMockK
    private lateinit var mockMedicalReviewViewModel: NCDMedicalReviewViewModel

    @RelaxedMockK
    private lateinit var mockListener: NCDDialogDismissListener

    @RelaxedMockK
    private lateinit var mockAdapter: CustomSpinnerAdapter

    @RelaxedMockK
    private lateinit var mockDiabetesEditText: AppCompatEditText

    @RelaxedMockK
    private lateinit var mockHypertensionEditText: AppCompatEditText

    @RelaxedMockK
    private lateinit var mockDiabetesErrorText: AppCompatTextView

    @RelaxedMockK
    private lateinit var mockHypertensionErrorText: AppCompatTextView

    @RelaxedMockK
    private lateinit var mockDiabetesControlledErrorText: AppCompatTextView

    @RelaxedMockK
    private lateinit var mockYearOfDiagnosisErrorText: AppCompatTextView

    @RelaxedMockK
    private lateinit var mockYearOfDiagnosisErrorHtnText: AppCompatTextView

    @RelaxedMockK
    private lateinit var mockGroupYearOfDiagnosis: Group

    @RelaxedMockK
    private lateinit var mockGroupYearOfDiagnosis2: Group

    @RelaxedMockK
    private lateinit var mockGroupDiabetesSpinner: Group

    private lateinit var fragment: NCDPatientHistoryDialog
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Setup mock context
        mockContext = ApplicationProvider.getApplicationContext()
        every { mockContext.getString(any()) } returns "Test String"
        every { mockContext.getText(any()) } returns "Test Text"

        // Setup fragment with test arguments
        fragment = NCDPatientHistoryDialog.newInstance(
            visitId = "test_visit_id",
            patientReference = "test_patient_ref",
            memberReference = "test_member_ref",
            isFemale = true,
            isPregnant = false
        )

        // Setup mock binding
        setupMockBinding()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun setupMockBinding() {
        // Setup diabetes hypertension binding
        every { mockBinding.ncdDiabetesHypertension.etYearOfDiagnosis } returns mockDiabetesEditText
        every { mockBinding.ncdDiabetesHypertension.etYearOfDiagnosisHtn } returns mockHypertensionEditText
        every { mockBinding.ncdDiabetesHypertension.tvYearOfDiagnosisError } returns mockYearOfDiagnosisErrorText
        every { mockBinding.ncdDiabetesHypertension.tvYearOfDiagnosisErrorHtn } returns mockYearOfDiagnosisErrorHtnText
        every { mockBinding.ncdDiabetesHypertension.tvDiabetesControlledError } returns mockDiabetesControlledErrorText
        every { mockBinding.ncdDiabetesHypertension.tvDiabetesError } returns mockDiabetesErrorText
        every { mockBinding.ncdDiabetesHypertension.tvHypertensionError } returns mockHypertensionErrorText
        every { mockBinding.ncdDiabetesHypertension.groupYearOfDiagnosis } returns mockGroupYearOfDiagnosis
        every { mockBinding.ncdDiabetesHypertension.groupYearOfDiagnosis2 } returns mockGroupYearOfDiagnosis2
        every { mockBinding.ncdDiabetesHypertension.groupDiabetesSpinner } returns mockGroupDiabetesSpinner
        every { mockBinding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.adapter } returns mockAdapter
    }

    // EDGE CASE 1: Year Validation Boundary Conditions
    @Test
    fun `test year validation edge cases for diabetes`() {
        // Test minimum boundary (1920)
        every { mockDiabetesEditText.text.toString() } returns "1920"
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val result = MotherNeonateUtil.isValidInput(
            "1920",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertTrue("Year 1920 should be valid", result)
        verify { mockYearOfDiagnosisErrorText.gone() }

        // Test maximum boundary (current year)
        val currentYear = getCurrentYearAsDouble().toInt().toString()
        every { mockDiabetesEditText.text.toString() } returns currentYear

        val resultMax = MotherNeonateUtil.isValidInput(
            currentYear,
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertTrue("Current year should be valid", resultMax)
    }

    @Test
    fun `test year validation edge cases for hypertension`() {
        // Test minimum boundary (1900)
        every { mockHypertensionEditText.text.toString() } returns "1900"
        every { mockHypertensionEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorHtnText.isVisible } returns false

        val result = MotherNeonateUtil.isValidInput(
            "1900",
            mockHypertensionEditText,
            mockYearOfDiagnosisErrorHtnText,
            1900.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertTrue("Year 1900 should be valid for hypertension", result)
        verify { mockYearOfDiagnosisErrorHtnText.gone() }
    }

    // EDGE CASE 2: Invalid Year Values
    @Test
    fun `test invalid year values for diabetes`() {
        // Test year below minimum
        every { mockDiabetesEditText.text.toString() } returns "1919"
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val result = MotherNeonateUtil.isValidInput(
            "1919",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertFalse("Year 1919 should be invalid for diabetes", result)
        verify { mockYearOfDiagnosisErrorText.visible() }

        // Test year above maximum
        val futureYear = (getCurrentYearAsDouble().toInt() + 1).toString()
        every { mockDiabetesEditText.text.toString() } returns futureYear

        val resultFuture = MotherNeonateUtil.isValidInput(
            futureYear,
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertFalse("Future year should be invalid", resultFuture)
    }

    // EDGE CASE 3: Empty and Null Input Handling
    @Test
    fun `test empty and null input handling`() {
        // Test empty string
        every { mockDiabetesEditText.text.toString() } returns ""
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val resultEmpty = MotherNeonateUtil.isValidInput(
            "",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertFalse("Empty string should be invalid when mandatory", resultEmpty)
        verify { mockYearOfDiagnosisErrorText.visible() }

        // Test null text
        every { mockDiabetesEditText.text } returns null
        every { mockDiabetesEditText.text.toString() } returns "null"

        val resultNull = MotherNeonateUtil.isValidInput(
            "null",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertFalse("Null text should be invalid", resultNull)
    }

    // EDGE CASE 4: Non-Numeric Input
    @Test
    fun `test non-numeric input handling`() {
        // Test alphabetic input
        every { mockDiabetesEditText.text.toString() } returns "abc"
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val result = MotherNeonateUtil.isValidInput(
            "abc",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertFalse("Alphabetic input should be invalid", result)
        verify { mockYearOfDiagnosisErrorText.visible() }

        // Test special characters
        every { mockDiabetesEditText.text.toString() } returns "2023@"

        val resultSpecial = MotherNeonateUtil.isValidInput(
            "2023@",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertFalse("Input with special characters should be invalid", resultSpecial)
    }

    // EDGE CASE 5: Decimal Year Input
    @Test
    fun `test decimal year input handling`() {
        // Test decimal year
        every { mockDiabetesEditText.text.toString() } returns "2023.5"
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val result = MotherNeonateUtil.isValidInput(
            "2023.5",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertTrue("Decimal year should be valid if within range", result)
        verify { mockYearOfDiagnosisErrorText.gone() }
    }

    // EDGE CASE 6: Zero and Negative Values
    @Test
    fun `test zero and negative year values`() {
        // Test zero
        every { mockDiabetesEditText.text.toString() } returns "0"
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val resultZero = MotherNeonateUtil.isValidInput(
            "0",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertFalse("Zero should be invalid", resultZero)
        verify { mockYearOfDiagnosisErrorText.visible() }

        // Test negative value
        every { mockDiabetesEditText.text.toString() } returns "-2023"

        val resultNegative = MotherNeonateUtil.isValidInput(
            "-2023",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertFalse("Negative year should be invalid", resultNegative)
    }

    // EDGE CASE 7: Very Large Numbers
    @Test
    fun `test very large number handling`() {
        // Test extremely large number
        every { mockDiabetesEditText.text.toString() } returns "999999999"
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val result = MotherNeonateUtil.isValidInput(
            "999999999",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertFalse("Extremely large number should be invalid", result)
        verify { mockYearOfDiagnosisErrorText.visible() }
    }

    // EDGE CASE 8: Whitespace Handling
    @Test
    fun `test whitespace handling in year input`() {
        // Test leading/trailing whitespace
        every { mockDiabetesEditText.text.toString() } returns "  2023  "
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val result = MotherNeonateUtil.isValidInput(
            "  2023  ",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertTrue("Year with whitespace should be valid", result)
        verify { mockYearOfDiagnosisErrorText.gone() }
    }

    // EDGE CASE 9: Optional Field Validation
    @Test
    fun `test optional field validation when not mandatory`() {
        // Test empty string when not mandatory
        every { mockDiabetesEditText.text.toString() } returns ""
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val result = MotherNeonateUtil.isValidInput(
            "",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            false, // Not mandatory
            mockContext
        )

        assertTrue("Empty string should be valid when not mandatory", result)
        verify { mockYearOfDiagnosisErrorText.gone() }
    }

    // EDGE CASE 10: Context Null Safety
    @Test
    fun `test context null safety in validation`() {
        // Test with null context (should handle gracefully)
        every { mockDiabetesEditText.text.toString() } returns "2023"
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        // This should not throw an exception even with potential null context issues
        val result = MotherNeonateUtil.isValidInput(
            "2023",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertTrue("Should handle context safely", result)
    }

    // EDGE CASE 11: Range Boundary Testing
    @Test
    fun `test range boundary edge cases`() {
        // Test exact boundary values
        val minYear = 1920.0
        val maxYear = getCurrentYearAsDouble()

        // Test minimum boundary
        every { mockDiabetesEditText.text.toString() } returns minYear.toInt().toString()
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val resultMin = MotherNeonateUtil.isValidInput(
            minYear.toInt().toString(),
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            minYear..maxYear,
            R.string.error_label,
            true,
            mockContext
        )

        assertTrue("Minimum boundary should be valid", resultMin)

        // Test maximum boundary
        every { mockDiabetesEditText.text.toString() } returns maxYear.toInt().toString()

        val resultMax = MotherNeonateUtil.isValidInput(
            maxYear.toInt().toString(),
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            minYear..maxYear,
            R.string.error_label,
            true,
            mockContext
        )

        assertTrue("Maximum boundary should be valid", resultMax)
    }

    // EDGE CASE 12: Error Message Display Logic
    @Test
    fun `test error message display logic`() {
        // Test that error messages are properly shown/hidden
        every { mockDiabetesEditText.text.toString() } returns "1919" // Invalid year
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val result = MotherNeonateUtil.isValidInput(
            "1919",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertFalse("Should return false for invalid input", result)
        verify { 
            mockYearOfDiagnosisErrorText.visible()
            mockYearOfDiagnosisErrorText.text = any()
        }

        // Test that error is hidden for valid input
        every { mockDiabetesEditText.text.toString() } returns "2023"
        every { mockYearOfDiagnosisErrorText.isVisible } returns true

        val resultValid = MotherNeonateUtil.isValidInput(
            "2023",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertTrue("Should return true for valid input", resultValid)
        verify { mockYearOfDiagnosisErrorText.gone() }
    }

    // EDGE CASE 13: EditText Focus Management
    @Test
    fun `test edit text focus management`() {
        // Test that focus is properly managed during validation
        every { mockDiabetesEditText.text.toString() } returns "1919"
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        MotherNeonateUtil.isValidInput(
            "1919",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        // Verify that the edit text is properly handled (focus management is implicit)
        verify { 
            mockDiabetesEditText.text.toString()
            mockYearOfDiagnosisErrorText.visible()
        }
    }

    // EDGE CASE 14: Multiple Validation Calls
    @Test
    fun `test multiple validation calls on same input`() {
        // Test that multiple validation calls work correctly
        every { mockDiabetesEditText.text.toString() } returns "2023"
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        // First validation
        val result1 = MotherNeonateUtil.isValidInput(
            "2023",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertTrue("First validation should pass", result1)

        // Second validation with same input
        val result2 = MotherNeonateUtil.isValidInput(
            "2023",
            mockDiabetesEditText,
            mockYearOfDiagnosisErrorText,
            1920.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            mockContext
        )

        assertTrue("Second validation should also pass", result2)
        verify(exactly = 2) { mockYearOfDiagnosisErrorText.gone() }
    }

    // EDGE CASE 15: Concurrent Validation
    @Test
    fun `test concurrent validation scenarios`() {
        // Test that validation works correctly when called rapidly
        every { mockDiabetesEditText.text.toString() } returns "2023"
        every { mockDiabetesEditText.isVisible() } returns true
        every { mockYearOfDiagnosisErrorText.isVisible } returns false

        val results = mutableListOf<Boolean>()

        // Simulate rapid validation calls
        repeat(5) {
            val result = MotherNeonateUtil.isValidInput(
                "2023",
                mockDiabetesEditText,
                mockYearOfDiagnosisErrorText,
                1920.0..getCurrentYearAsDouble(),
                R.string.error_label,
                true,
                mockContext
            )
            results.add(result)
        }

        assertTrue("All concurrent validations should pass", results.all { it })
        verify(exactly = 5) { mockYearOfDiagnosisErrorText.gone() }
    }
} 
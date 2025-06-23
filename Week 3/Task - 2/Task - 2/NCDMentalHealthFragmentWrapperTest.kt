package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMentalHealthViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = android.app.Application::class)
class NCDMentalHealthFragmentWrapperTest {

    @MockK
    private lateinit var mockViewModel: NCDMentalHealthViewModel

    @MockK
    private lateinit var mockContext: Context

    @MockK
    private lateinit var mockResources: android.content.res.Resources

    @MockK
    private lateinit var mockConfiguration: Configuration

    @MockK
    private lateinit var mockEtYrOfDiagnosis: AppCompatEditText

    @MockK
    private lateinit var mockEtSubstanceDiagnosis: AppCompatEditText

    @MockK
    private lateinit var mockEtYearOfDiagnosis: AppCompatEditText

    @MockK
    private lateinit var mockEtYearOfDiagnosisHtn: AppCompatEditText

    @MockK
    private lateinit var mockTvMentalHealthError: AppCompatTextView

    @MockK
    private lateinit var mockTvSubstanceUseError: AppCompatTextView

    @MockK
    private lateinit var mockTvMentalHealthDisorderError: AppCompatTextView

    @MockK
    private lateinit var mockTvCommentsError: AppCompatTextView

    @MockK
    private lateinit var mockTvSubstanceDisorderError: AppCompatTextView

    @MockK
    private lateinit var mockTvSubstanceCommentsError: AppCompatTextView

    @MockK
    private lateinit var mockTvYrOfDiagnosisError: AppCompatTextView

    @MockK
    private lateinit var mockTvDiagnosisError: AppCompatTextView

    @MockK
    private lateinit var mockTvDiabetesError: AppCompatTextView

    @MockK
    private lateinit var mockTvHypertensionError: AppCompatTextView

    @MockK
    private lateinit var mockTvDiabetesControlledError: AppCompatTextView

    @MockK
    private lateinit var mockTvYearOfDiagnosisError: AppCompatTextView

    @MockK
    private lateinit var mockTvYearOfDiagnosisErrorHtn: AppCompatTextView

    private lateinit var wrapper: NCDMentalHealthFragmentWrapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        
        // Setup context mocks
        every { mockContext.resources } returns mockResources
        every { mockResources.configuration } returns mockConfiguration
        every { mockContext.getString(R.string.na) } returns "N/A"
        every { mockContext.getString(R.string.newly_Diagnosed) } returns "Newly Diagnosed"
        every { mockContext.getString(R.string.known_patient) } returns "Known Patient"
        every { mockContext.getString(R.string.please_select) } returns "Please Select"
        every { mockContext.getString(R.string.error_label) } returns "Error"

        // Setup static mocks
        mockkObject(CommonUtils)
        mockkObject(DateUtils)
        mockkObject(MotherNeonateUtil)
        
        every { CommonUtils.checkIsTablet(any()) } returns false
        every { DateUtils.getCurrentYearAsDouble() } returns 2024.0
        every { MotherNeonateUtil.isValidInput(any(), any(), any(), any(), any(), any(), any()) } returns true

        wrapper = NCDMentalHealthFragmentWrapper(mockContext, mockViewModel)
    }

    @Test
    fun `test validateMentalHealthAndSubstance returns true for valid inputs`() {
        // Given
        val resultMentalHealthHashMap = mapOf(
            NCDMentalHealthFragment.MENTAL_HEALTH_STATUS to "Known Patient"
        )
        val resultSubstanceUseHashMap = mapOf(
            NCDMentalHealthFragment.SUBSTANCE_USE_STATUS to "Known Patient"
        )
        val selectedMentalHealthListItem = listOf(
            MultiSelectDropDownModel(1L, "Test", "Test", "test_value")
        )
        val selectedSubstanceListItem = listOf(
            MultiSelectDropDownModel(1L, "Test", "Test", "test_value")
        )
        val mentalHealthComments = "Test comments"
        val substanceUseComments = "Test comments"

        every { mockEtYrOfDiagnosis.text.toString() } returns "2020"
        every { mockEtSubstanceDiagnosis.text.toString() } returns "2021"

        // When
        val result = wrapper.validateMentalHealthAndSubstance(
            resultMentalHealthHashMap,
            resultSubstanceUseHashMap,
            selectedMentalHealthListItem,
            selectedSubstanceListItem,
            mentalHealthComments,
            substanceUseComments,
            mockEtYrOfDiagnosis,
            mockEtSubstanceDiagnosis,
            mockTvMentalHealthError,
            mockTvSubstanceUseError,
            mockTvMentalHealthDisorderError,
            mockTvCommentsError,
            mockTvSubstanceDisorderError,
            mockTvSubstanceCommentsError,
            mockTvYrOfDiagnosisError,
            mockTvDiagnosisError
        )

        // Then
        assertTrue(result)
        verify { 
            mockTvMentalHealthError.isVisible = false
            mockTvSubstanceUseError.isVisible = false
            mockTvMentalHealthDisorderError.isVisible = false
            mockTvCommentsError.isVisible = false
            mockTvSubstanceDisorderError.isVisible = false
            mockTvSubstanceCommentsError.isVisible = false
        }
    }

    @Test
    fun `test validateMentalHealthAndSubstance returns false when mental health status is empty`() {
        // Given
        val resultMentalHealthHashMap = emptyMap<String, Any>()
        val resultSubstanceUseHashMap = mapOf(
            NCDMentalHealthFragment.SUBSTANCE_USE_STATUS to "Known Patient"
        )

        // When
        val result = wrapper.validateMentalHealthAndSubstance(
            resultMentalHealthHashMap,
            resultSubstanceUseHashMap,
            emptyList(),
            emptyList(),
            null,
            null,
            mockEtYrOfDiagnosis,
            mockEtSubstanceDiagnosis,
            mockTvMentalHealthError,
            mockTvSubstanceUseError,
            mockTvMentalHealthDisorderError,
            mockTvCommentsError,
            mockTvSubstanceDisorderError,
            mockTvSubstanceCommentsError,
            mockTvYrOfDiagnosisError,
            mockTvDiagnosisError
        )

        // Then
        assertFalse(result)
        verify { mockTvMentalHealthError.isVisible = true }
    }

    @Test
    fun `test validateMentalHealthAndSubstance returns false when substance use status is empty`() {
        // Given
        val resultMentalHealthHashMap = mapOf(
            NCDMentalHealthFragment.MENTAL_HEALTH_STATUS to "Known Patient"
        )
        val resultSubstanceUseHashMap = emptyMap<String, Any>()

        // When
        val result = wrapper.validateMentalHealthAndSubstance(
            resultMentalHealthHashMap,
            resultSubstanceUseHashMap,
            emptyList(),
            emptyList(),
            null,
            null,
            mockEtYrOfDiagnosis,
            mockEtSubstanceDiagnosis,
            mockTvMentalHealthError,
            mockTvSubstanceUseError,
            mockTvMentalHealthDisorderError,
            mockTvCommentsError,
            mockTvSubstanceDisorderError,
            mockTvSubstanceCommentsError,
            mockTvYrOfDiagnosisError,
            mockTvDiagnosisError
        )

        // Then
        assertFalse(result)
        verify { mockTvSubstanceUseError.isVisible = true }
    }

    @Test
    fun `test validateNCDPatientStatus returns true for valid inputs`() {
        // Given
        val resultDiabetesHashMap = mapOf(
            NCDMentalHealthFragment.DIABETES to "Known Patient"
        )
        val resultHypertensionHashMap = mapOf(
            NCDMentalHealthFragment.HYPERTENSION to "Known Patient"
        )
        val value = "test_value"

        every { mockEtYearOfDiagnosis.isVisible } returns true
        every { mockEtYearOfDiagnosis.text.toString() } returns "2020"
        every { mockEtYearOfDiagnosisHtn.text.toString() } returns "2021"

        // When
        val result = wrapper.validateNCDPatientStatus(
            resultDiabetesHashMap,
            resultHypertensionHashMap,
            value,
            mockEtYearOfDiagnosis,
            mockEtYearOfDiagnosisHtn,
            mockTvDiabetesError,
            mockTvHypertensionError,
            mockTvDiabetesControlledError,
            mockTvYearOfDiagnosisError,
            mockTvYearOfDiagnosisErrorHtn
        )

        // Then
        assertTrue(result)
        verify { 
            mockTvDiabetesError.isVisible = false
            mockTvHypertensionError.isVisible = false
            mockTvDiabetesControlledError.isVisible = false
            mockTvYearOfDiagnosisErrorHtn.isVisible = false
        }
    }

    @Test
    fun `test validateNCDPatientStatus returns false when diabetes status is empty`() {
        // Given
        val resultDiabetesHashMap = emptyMap<String, Any>()
        val resultHypertensionHashMap = mapOf(
            NCDMentalHealthFragment.HYPERTENSION to "Known Patient"
        )

        // When
        val result = wrapper.validateNCDPatientStatus(
            resultDiabetesHashMap,
            resultHypertensionHashMap,
            null,
            mockEtYearOfDiagnosis,
            mockEtYearOfDiagnosisHtn,
            mockTvDiabetesError,
            mockTvHypertensionError,
            mockTvDiabetesControlledError,
            mockTvYearOfDiagnosisError,
            mockTvYearOfDiagnosisErrorHtn
        )

        // Then
        assertFalse(result)
        verify { mockTvDiabetesError.isVisible = true }
    }

    @Test
    fun `test validateNCDPatientStatus returns false when hypertension status is empty`() {
        // Given
        val resultDiabetesHashMap = mapOf(
            NCDMentalHealthFragment.DIABETES to "Known Patient"
        )
        val resultHypertensionHashMap = emptyMap<String, Any>()

        // When
        val result = wrapper.validateNCDPatientStatus(
            resultDiabetesHashMap,
            resultHypertensionHashMap,
            null,
            mockEtYearOfDiagnosis,
            mockEtYearOfDiagnosisHtn,
            mockTvDiabetesError,
            mockTvHypertensionError,
            mockTvDiabetesControlledError,
            mockTvYearOfDiagnosisError,
            mockTvYearOfDiagnosisErrorHtn
        )

        // Then
        assertFalse(result)
        verify { mockTvHypertensionError.isVisible = true }
    }

    @Test
    fun `test validateNCDPatientStatus returns false when diabetes value is null for known patient`() {
        // Given
        val resultDiabetesHashMap = mapOf(
            NCDMentalHealthFragment.DIABETES to "Known Patient"
        )
        val resultHypertensionHashMap = mapOf(
            NCDMentalHealthFragment.HYPERTENSION to "Known Patient"
        )

        every { mockEtYearOfDiagnosis.isVisible } returns true
        every { mockEtYearOfDiagnosis.text.toString() } returns "2020"

        // When
        val result = wrapper.validateNCDPatientStatus(
            resultDiabetesHashMap,
            resultHypertensionHashMap,
            null,
            mockEtYearOfDiagnosis,
            mockEtYearOfDiagnosisHtn,
            mockTvDiabetesError,
            mockTvHypertensionError,
            mockTvDiabetesControlledError,
            mockTvYearOfDiagnosisError,
            mockTvYearOfDiagnosisErrorHtn
        )

        // Then
        assertFalse(result)
        verify { mockTvDiabetesControlledError.isVisible = true }
    }

    @Test
    fun `test isValidDiagnosis calls MotherNeonateUtil correctly`() {
        // Given
        every { mockEtYrOfDiagnosis.text.toString() } returns "2020"
        every { MotherNeonateUtil.isValidInput(any(), any(), any(), any(), any(), any(), any()) } returns true

        // When
        val result = wrapper.isValidDiagnosis(mockEtYrOfDiagnosis, mockTvYrOfDiagnosisError)

        // Then
        assertTrue(result)
        verify { 
            MotherNeonateUtil.isValidInput(
                "2020",
                mockEtYrOfDiagnosis,
                mockTvYrOfDiagnosisError,
                1920.0..2024.0,
                R.string.error_label,
                true,
                mockContext
            )
        }
    }

    @Test
    fun `test isValidDiagnosisTwo calls MotherNeonateUtil correctly`() {
        // Given
        every { mockEtYearOfDiagnosisHtn.text.toString() } returns "2020"
        every { MotherNeonateUtil.isValidInput(any(), any(), any(), any(), any(), any(), any()) } returns true

        // When
        val result = wrapper.isValidDiagnosisTwo(mockEtYearOfDiagnosisHtn, mockTvYearOfDiagnosisErrorHtn)

        // Then
        assertTrue(result)
        verify { 
            MotherNeonateUtil.isValidInput(
                "2020",
                mockEtYearOfDiagnosisHtn,
                mockTvYearOfDiagnosisErrorHtn,
                1900.0..2024.0,
                R.string.error_label,
                true,
                mockContext
            )
        }
    }

    @Test
    fun `test shouldShowSpinner returns true for known patient`() {
        // Given
        val selectedValue = NCDMentalHealthFragment.Known_patient

        // When
        val result = wrapper.shouldShowSpinner(selectedValue)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test shouldShowSpinner returns true for newly diagnosed`() {
        // Given
        val selectedValue = NCDMentalHealthFragment.Newly_Diagnosed

        // When
        val result = wrapper.shouldShowSpinner(selectedValue)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test shouldShowSpinner returns false for N_A`() {
        // Given
        val selectedValue = NCDMentalHealthFragment.N_A

        // When
        val result = wrapper.shouldShowSpinner(selectedValue)

        // Then
        assertFalse(result)
    }

    @Test
    fun `test shouldShowYearOfDiagnosis returns true for known patient`() {
        // Given
        val selectedValue = NCDMentalHealthFragment.Known_patient

        // When
        val result = wrapper.shouldShowYearOfDiagnosis(selectedValue)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test shouldShowYearOfDiagnosis returns false for newly diagnosed`() {
        // Given
        val selectedValue = NCDMentalHealthFragment.Newly_Diagnosed

        // When
        val result = wrapper.shouldShowYearOfDiagnosis(selectedValue)

        // Then
        assertFalse(result)
    }

    @Test
    fun `test calculateDialogDimensions returns correct dimensions for phone`() {
        // Given
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT
        every { CommonUtils.checkIsTablet(any()) } returns false

        // When
        val result = wrapper.calculateDialogDimensions()

        // Then
        assertEquals(Pair(100, 100), result)
    }

    @Test
    fun `test calculateDialogDimensions returns correct dimensions for tablet landscape`() {
        // Given
        mockConfiguration.orientation = Configuration.ORIENTATION_LANDSCAPE
        every { CommonUtils.checkIsTablet(any()) } returns true

        // When
        val result = wrapper.calculateDialogDimensions()

        // Then
        assertEquals(Pair(50, 95), result)
    }

    @Test
    fun `test createSingleSelectionOptions returns correct options`() {
        // When
        val result = wrapper.createSingleSelectionOptions()

        // Then
        assertEquals(3, result.size)
        assertEquals(NCDMentalHealthFragment.N_A, result[0]["value"])
        assertEquals(NCDMentalHealthFragment.Newly_Diagnosed, result[1]["value"])
        assertEquals(NCDMentalHealthFragment.Known_patient, result[2]["value"])
    }

    @Test
    fun `test processNCDDiagnosisData filters out Pre-Diabetes and adds please select`() {
        // Given
        val data = listOf(
            NCDDiagnosisEntity(id = 1L, name = "Diabetes", displayValue = "Diabetes", value = "diabetes"),
            NCDDiagnosisEntity(id = 2L, name = "Pre-Diabetes", displayValue = "Pre-Diabetes", value = "pre_diabetes"),
            NCDDiagnosisEntity(id = 3L, name = "Hypertension", displayValue = "Hypertension", value = "hypertension")
        )

        // When
        val result = wrapper.processNCDDiagnosisData(data)

        // Then
        assertEquals(3, result.size) // Please select + 2 valid items (excluding Pre-Diabetes)
        assertEquals("Please Select", result[0]["name"])
        assertEquals(-1L, result[0]["id"])
        assertEquals("Diabetes", result[1]["name"])
        assertEquals("Hypertension", result[2]["name"])
    }

    @Test
    fun `test validateGender returns female when isFemale is true`() {
        // Given
        val isFemale = true

        // When
        val result = wrapper.validateGender(isFemale)

        // Then
        assertEquals("female", result)
    }

    @Test
    fun `test validateGender returns male when isFemale is false`() {
        // Given
        val isFemale = false

        // When
        val result = wrapper.validateGender(isFemale)

        // Then
        assertEquals("male", result)
    }

    @Test
    fun `test validatePregnancyStatus returns true when isPregnant is true`() {
        // Given
        val isPregnant = true

        // When
        val result = wrapper.validatePregnancyStatus(isPregnant)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test validatePregnancyStatus returns false when isPregnant is false`() {
        // Given
        val isPregnant = false

        // When
        val result = wrapper.validatePregnancyStatus(isPregnant)

        // Then
        assertFalse(result)
    }

    @Test
    fun `test validateNCDVisibility returns true when showNCD is true`() {
        // Given
        val showNCD = true

        // When
        val result = wrapper.validateNCDVisibility(showNCD)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test validateNCDVisibility returns false when showNCD is false`() {
        // Given
        val showNCD = false

        // When
        val result = wrapper.validateNCDVisibility(showNCD)

        // Then
        assertFalse(result)
    }
} 
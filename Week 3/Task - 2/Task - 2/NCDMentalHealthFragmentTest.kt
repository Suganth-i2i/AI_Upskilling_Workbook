package com.medtroniclabs.spice.ncd.medicalreview.dialog

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
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.formgeneration.utility.MultiSelectSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.data.MentalHealthStatus
import com.medtroniclabs.spice.ncd.data.NCDMentalHealthStatusRequest
import com.medtroniclabs.spice.ncd.data.NcdPatientStatus
import com.medtroniclabs.spice.ncd.medicalreview.NCDDialogDismissListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMentalHealthViewModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import io.mockk.*
import io.mockk.impl.annotations.MockK
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(application = android.app.Application::class)
class NCDMentalHealthFragmentTest {

    @MockK
    private lateinit var mockMentalHealthViewModel: NCDMentalHealthViewModel

    @MockK
    private lateinit var mockMedicalReviewViewModel: NCDMedicalReviewViewModel

    @MockK
    private lateinit var mockListener: NCDDialogDismissListener

    @MockK
    private lateinit var mockBinding: com.medtroniclabs.spice.databinding.FragmentNCDMentalHealthBinding

    @MockK
    private lateinit var mockNcdDiabetesHypertension: com.medtroniclabs.spice.databinding.NcdDiabetesHypertensionBinding

    @MockK
    private lateinit var mockEtYearOfDiagnosis: AppCompatEditText

    @MockK
    private lateinit var mockEtYrOfDiagnosis: AppCompatEditText

    @MockK
    private lateinit var mockEtSubstanceDiagnosis: AppCompatEditText

    @MockK
    private lateinit var mockTvYrOfDiagnosisError: AppCompatTextView

    @MockK
    private lateinit var mockTvDiagnosisError: AppCompatTextView

    @MockK
    private lateinit var mockGroupYearOfDiagnosis: Group

    @MockK
    private lateinit var mockGroupYearOfDiagnosis2: Group

    @MockK
    private lateinit var mockGroupDiabetesSpinner: Group

    @MockK
    private lateinit var mockGroupMentalHealth: Group

    @MockK
    private lateinit var mockGroupMentalHealthSpinner: Group

    @MockK
    private lateinit var mockGroupSubstanceUse: Group

    @MockK
    private lateinit var mockGroupSubstanceUseSpinner: Group

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
    private lateinit var mockTvDiabetesError: AppCompatTextView

    @MockK
    private lateinit var mockTvHypertensionError: AppCompatTextView

    @MockK
    private lateinit var mockTvDiabetesControlledError: AppCompatTextView

    @MockK
    private lateinit var mockTvYearOfDiagnosisError: AppCompatTextView

    @MockK
    private lateinit var mockTvYearOfDiagnosisErrorHtn: AppCompatTextView

    @MockK
    private lateinit var mockTvNCD: AppCompatTextView

    @MockK
    private lateinit var mockBtnConfirm: View

    @MockK
    private lateinit var mockBtnCancel: View

    @MockK
    private lateinit var mockIvClose: View

    @MockK
    private lateinit var mockLoadingProgress: View

    @MockK
    private lateinit var mockEtComments: AppCompatEditText

    @MockK
    private lateinit var mockEtSubstanceComments: AppCompatEditText

    @MockK
    private lateinit var mockEtMentalHealthDisorder: AppCompatEditText

    @MockK
    private lateinit var mockEtSubstanceDisorder: AppCompatEditText

    @MockK
    private lateinit var mockTvDiabetesControlledSpinner: AppCompatTextView

    @MockK
    private lateinit var mockLlMentalHealth: View

    @MockK
    private lateinit var mockLlSubstanceUse: View

    @MockK
    private lateinit var mockLlDiabetes: View

    @MockK
    private lateinit var mockLlHypertension: View

    @MockK
    private lateinit var mockRoot: View

    private lateinit var fragment: NCDMentalHealthFragment
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        
        setupMockBinding()
        setupMockNcdDiabetesHypertension()
        setupViewVisibilityMocks()
        setupErrorVisibilityMocks()
        setupTextMocks()
        setupStaticMocks()

        fragment = NCDMentalHealthFragment.newInstance(
            visitId = "test_visit_id",
            patientReference = "test_patient_ref",
            memberReference = "test_member_ref",
            isFemale = false,
            isPregnant = false,
            showNCD = true
        )
        fragment.listener = mockListener
    }

    private fun setupMockBinding() {
        every { mockBinding.ncdDiabetesHypertension } returns mockNcdDiabetesHypertension
        every { mockBinding.root } returns mockRoot
        every { mockBinding.tvNCD } returns mockTvNCD
        every { mockBinding.btnConfirm } returns mockBtnConfirm
        every { mockBinding.btnCancel } returns mockBtnCancel
        every { mockBinding.ivClose } returns mockIvClose
        every { mockBinding.loadingProgress } returns mockLoadingProgress
        every { mockBinding.etComments } returns mockEtComments
        every { mockBinding.etSubstanceComments } returns mockEtSubstanceComments
        every { mockBinding.etMentalHealthDisorder } returns mockEtMentalHealthDisorder
        every { mockBinding.etSubstanceDisorder } returns mockEtSubstanceDisorder
        every { mockBinding.etYrOfDiagnosis } returns mockEtYrOfDiagnosis
        every { mockBinding.etSubstanceDiagnosis } returns mockEtSubstanceDiagnosis
        every { mockBinding.tvMentalHealthError } returns mockTvMentalHealthError
        every { mockBinding.tvSubstanceUseError } returns mockTvSubstanceUseError
        every { mockBinding.tvMentalHealthDisorderError } returns mockTvMentalHealthDisorderError
        every { mockBinding.tvCommentsError } returns mockTvCommentsError
        every { mockBinding.tvSubstanceDisorderError } returns mockTvSubstanceDisorderError
        every { mockBinding.tvSubstanceCommentsError } returns mockTvSubstanceCommentsError
        every { mockBinding.tvYrOfDiagnosisError } returns mockTvYrOfDiagnosisError
        every { mockBinding.tvDiagnosisError } returns mockTvDiagnosisError
        every { mockBinding.groupMentalHealth } returns mockGroupMentalHealth
        every { mockBinding.groupMentalHealthSpinner } returns mockGroupMentalHealthSpinner
        every { mockBinding.groupSubstanceUse } returns mockGroupSubstanceUse
        every { mockBinding.groupSubstanceUseSpinner } returns mockGroupSubstanceUseSpinner
        every { mockBinding.llMentalHealth } returns mockLlMentalHealth
        every { mockBinding.llSubstanceUse } returns mockLlSubstanceUse
    }

    private fun setupMockNcdDiabetesHypertension() {
        every { mockNcdDiabetesHypertension.etYearOfDiagnosis } returns mockEtYearOfDiagnosis
        every { mockNcdDiabetesHypertension.etYearOfDiagnosisHtn } returns mockEtYearOfDiagnosis
        every { mockNcdDiabetesHypertension.tvDiabetesError } returns mockTvDiabetesError
        every { mockNcdDiabetesHypertension.tvHypertensionError } returns mockTvHypertensionError
        every { mockNcdDiabetesHypertension.tvDiabetesControlledError } returns mockTvDiabetesControlledError
        every { mockNcdDiabetesHypertension.tvYearOfDiagnosisError } returns mockTvYearOfDiagnosisError
        every { mockNcdDiabetesHypertension.tvYearOfDiagnosisErrorHtn } returns mockTvYearOfDiagnosisErrorHtn
        every { mockNcdDiabetesHypertension.tvDiabetesControlledSpinner } returns mockTvDiabetesControlledSpinner
        every { mockNcdDiabetesHypertension.groupYearOfDiagnosis } returns mockGroupYearOfDiagnosis
        every { mockNcdDiabetesHypertension.groupYearOfDiagnosis2 } returns mockGroupYearOfDiagnosis2
        every { mockNcdDiabetesHypertension.groupDiabetesSpinner } returns mockGroupDiabetesSpinner
        every { mockNcdDiabetesHypertension.llDiabetes } returns mockLlDiabetes
        every { mockNcdDiabetesHypertension.llHypertension } returns mockLlHypertension
        every { mockNcdDiabetesHypertension.root } returns mockRoot
    }

    private fun setupViewVisibilityMocks() {
        every { mockTvNCD.isVisible() } returns false
        every { mockGroupYearOfDiagnosis.isVisible = any() } just Runs
        every { mockGroupYearOfDiagnosis2.isVisible = any() } just Runs
        every { mockGroupDiabetesSpinner.isVisible = any() } just Runs
        every { mockGroupMentalHealth.setVisible(any()) } just Runs
        every { mockGroupMentalHealthSpinner.setVisible(any()) } just Runs
        every { mockGroupSubstanceUse.setVisible(any()) } just Runs
        every { mockGroupSubstanceUseSpinner.setVisible(any()) } just Runs
    }

    private fun setupErrorVisibilityMocks() {
        every { mockTvMentalHealthError.setVisible(any()) } just Runs
        every { mockTvSubstanceUseError.setVisible(any()) } just Runs
        every { mockTvMentalHealthDisorderError.setVisible(any()) } just Runs
        every { mockTvCommentsError.setVisible(any()) } just Runs
        every { mockTvSubstanceDisorderError.setVisible(any()) } just Runs
        every { mockTvSubstanceCommentsError.setVisible(any()) } just Runs
        every { mockTvDiabetesError.setVisible(any()) } just Runs
        every { mockTvHypertensionError.setVisible(any()) } just Runs
        every { mockTvDiabetesControlledError.setVisible(any()) } just Runs
        every { mockTvYearOfDiagnosisError.setVisible(any()) } just Runs
        every { mockTvYearOfDiagnosisErrorHtn.setVisible(any()) } just Runs
        every { mockTvYrOfDiagnosisError.gone() } just Runs
        every { mockTvDiagnosisError.gone() } just Runs
        every { mockTvDiabetesControlledError.gone() } just Runs
        every { mockTvDiabetesControlledError.visible() } just Runs
    }

    private fun setupTextMocks() {
        every { mockEtYearOfDiagnosis.text } returns mockk()
        every { mockEtYrOfDiagnosis.text } returns mockk()
        every { mockEtSubstanceDiagnosis.text } returns mockk()
        every { mockEtComments.text } returns mockk()
        every { mockEtSubstanceComments.text } returns mockk()
    }

    private fun setupStaticMocks() {
        mockkObject(SecuredPreference)
        every { SecuredPreference.getIsTranslationEnabled() } returns false

        mockkObject(MotherNeonateUtil)
        every { MotherNeonateUtil.isValidInput(any(), any(), any(), any(), any(), any(), any()) } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `test newInstance creates fragment with correct arguments`() {
        // Given
        val visitId = "test_visit_id"
        val patientReference = "test_patient_ref"
        val memberReference = "test_member_ref"
        val isFemale = true
        val isPregnant = true
        val showNCD = false

        // When
        val fragment = NCDMentalHealthFragment.newInstance(
            visitId, patientReference, memberReference, isFemale, isPregnant, showNCD
        )

        // Then
        assertNotNull(fragment.arguments)
        assertEquals(visitId, fragment.arguments?.getString(NCDMRUtil.EncounterReference))
        assertEquals(patientReference, fragment.arguments?.getString(NCDMRUtil.PATIENT_REFERENCE))
        assertEquals(memberReference, fragment.arguments?.getString(NCDMRUtil.MEMBER_REFERENCE))
        assertEquals(isFemale, fragment.arguments?.getBoolean(NCDMRUtil.IS_FEMALE))
        assertEquals(isPregnant, fragment.arguments?.getBoolean(NCDMRUtil.IsPregnant))
        assertEquals(showNCD, fragment.arguments?.getBoolean(NCDMRUtil.ShowNCD))
    }

    @Test
    fun `test getGender returns female when isFemale is true`() {
        // Given
        val fragment = NCDMentalHealthFragment.newInstance(
            visitId = "test",
            patientReference = "test",
            memberReference = "test",
            isFemale = true,
            isPregnant = false,
            showNCD = false
        )

        // When
        val result = fragment.getGender()

        // Then
        assertEquals(Screening.Female.lowercase(), result)
    }

    @Test
    fun `test getGender returns male when isFemale is false`() {
        // Given
        val fragment = NCDMentalHealthFragment.newInstance(
            visitId = "test",
            patientReference = "test",
            memberReference = "test",
            isFemale = false,
            isPregnant = false,
            showNCD = false
        )

        // When
        val result = fragment.getGender()

        // Then
        assertEquals(Screening.Male.lowercase(), result)
    }

    @Test
    fun `test isPregnant returns true when isPregnant is true`() {
        // Given
        val fragment = NCDMentalHealthFragment.newInstance(
            visitId = "test",
            patientReference = "test",
            memberReference = "test",
            isFemale = false,
            isPregnant = true,
            showNCD = false
        )

        // When
        val result = fragment.isPregnant()

        // Then
        assertTrue(result)
    }

    @Test
    fun `test isPregnant returns false when isPregnant is false`() {
        // Given
        val fragment = NCDMentalHealthFragment.newInstance(
            visitId = "test",
            patientReference = "test",
            memberReference = "test",
            isFemale = false,
            isPregnant = false,
            showNCD = false
        )

        // When
        val result = fragment.isPregnant()

        // Then
        assertFalse(result)
    }

    @Test
    fun `test showNCD returns true when ShowNCD is true`() {
        // Given
        val fragment = NCDMentalHealthFragment.newInstance(
            visitId = "test",
            patientReference = "test",
            memberReference = "test",
            isFemale = false,
            isPregnant = false,
            showNCD = true
        )

        // When
        val result = fragment.showNCD()

        // Then
        assertTrue(result)
    }

    @Test
    fun `test showNCD returns false when ShowNCD is false`() {
        // Given
        val fragment = NCDMentalHealthFragment.newInstance(
            visitId = "test",
            patientReference = "test",
            memberReference = "test",
            isFemale = false,
            isPregnant = false,
            showNCD = false
        )

        // When
        val result = fragment.showNCD()

        // Then
        assertFalse(result)
    }

    @Test
    fun `test getPatientReference returns correct value`() {
        // Given
        val expectedReference = "test_patient_ref"
        val fragment = NCDMentalHealthFragment.newInstance(
            visitId = "test",
            patientReference = expectedReference,
            memberReference = "test",
            isFemale = false,
            isPregnant = false,
            showNCD = false
        )

        // When
        val result = fragment.getPatientReference()

        // Then
        assertEquals(expectedReference, result)
    }

    @Test
    fun `test getMemberReference returns correct value`() {
        // Given
        val expectedReference = "test_member_ref"
        val fragment = NCDMentalHealthFragment.newInstance(
            visitId = "test",
            patientReference = "test",
            memberReference = expectedReference,
            isFemale = false,
            isPregnant = false,
            showNCD = false
        )

        // When
        val result = fragment.getMemberReference()

        // Then
        assertEquals(expectedReference, result)
    }

    @Test
    fun `test getEncounterReference returns correct value`() {
        // Given
        val expectedReference = "test_encounter_ref"
        val fragment = NCDMentalHealthFragment.newInstance(
            visitId = expectedReference,
            patientReference = "test",
            memberReference = "test",
            isFemale = false,
            isPregnant = false,
            showNCD = false
        )

        // When
        val result = fragment.getEncounterReference()

        // Then
        assertEquals(expectedReference, result)
    }

    @Test
    fun `test validateInput returns true when NCD not visible and mental health validation passes`() {
        // Given
        every { mockTvNCD.isVisible() } returns false
        fragment.binding = mockBinding

        // Mock viewModel state for valid mental health and substance use
        fragment.viewModel = mockMentalHealthViewModel
        every { mockMentalHealthViewModel.resultMentalHealthHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.MENTAL_HEALTH_STATUS to "Known Patient"
        )
        every { mockMentalHealthViewModel.resultSubstanceUseHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.SUBSTANCE_USE_STATUS to "Known Patient"
        )
        every { mockMentalHealthViewModel.selectedMentalHealthListItem } returns mutableListOf(
            MultiSelectDropDownModel(1L, "Test", "Test", "test_value")
        )
        every { mockMentalHealthViewModel.selectedSubstanceListItem } returns mutableListOf(
            MultiSelectDropDownModel(1L, "Test", "Test", "test_value")
        )
        every { mockMentalHealthViewModel.mentalHealthComments } returns "Test comments"
        every { mockMentalHealthViewModel.substanceUseComments } returns "Test comments"

        // When
        val result = fragment.validateInput()

        // Then
        assertTrue(result)
    }

    @Test
    fun `test validateInput returns false when mental health status is empty`() {
        // Given
        every { mockTvNCD.isVisible() } returns false
        fragment.binding = mockBinding

        fragment.viewModel = mockMentalHealthViewModel
        every { mockMentalHealthViewModel.resultMentalHealthHashMap } returns mutableMapOf()
        every { mockMentalHealthViewModel.resultSubstanceUseHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.SUBSTANCE_USE_STATUS to "Known Patient"
        )

        // When
        val result = fragment.validateInput()

        // Then
        assertFalse(result)
        verify { mockTvMentalHealthError.setVisible(true) }
    }

    @Test
    fun `test validateInput returns false when substance use status is empty`() {
        // Given
        every { mockTvNCD.isVisible() } returns false
        fragment.binding = mockBinding

        fragment.viewModel = mockMentalHealthViewModel
        every { mockMentalHealthViewModel.resultMentalHealthHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.MENTAL_HEALTH_STATUS to "Known Patient"
        )
        every { mockMentalHealthViewModel.resultSubstanceUseHashMap } returns mutableMapOf()

        // When
        val result = fragment.validateInput()

        // Then
        assertFalse(result)
        verify { mockTvSubstanceUseError.setVisible(true) }
    }

    @Test
    fun `test validateMentalHealthAndSubstance validates known patient requirements`() {
        // Given
        fragment.binding = mockBinding
        fragment.viewModel = mockMentalHealthViewModel

        every { mockMentalHealthViewModel.resultMentalHealthHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.MENTAL_HEALTH_STATUS to "Known Patient"
        )
        every { mockMentalHealthViewModel.resultSubstanceUseHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.SUBSTANCE_USE_STATUS to "Known Patient"
        )
        every { mockMentalHealthViewModel.selectedMentalHealthListItem } returns mutableListOf()
        every { mockMentalHealthViewModel.selectedSubstanceListItem } returns mutableListOf()
        every { mockMentalHealthViewModel.mentalHealthComments } returns ""
        every { mockMentalHealthViewModel.substanceUseComments } returns ""

        // When
        val result = fragment.validateMentalHealthAndSubstance()

        // Then
        assertFalse(result)
        verify { 
            mockTvMentalHealthDisorderError.setVisible(true)
            mockTvCommentsError.setVisible(true)
            mockTvSubstanceDisorderError.setVisible(true)
            mockTvSubstanceCommentsError.setVisible(true)
        }
    }

    @Test
    fun `test validateNCDPatientStatus returns false when diabetes status is empty`() {
        // Given
        fragment.binding = mockBinding
        fragment.viewModel = mockMentalHealthViewModel

        every { mockMentalHealthViewModel.resultDiabetesHashMap } returns mutableMapOf()
        every { mockMentalHealthViewModel.resultHypertensionHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.HYPERTENSION to "Known Patient"
        )

        // When
        val result = fragment.validateNCDPatientStatus()

        // Then
        assertFalse(result)
        verify { mockTvDiabetesError.setVisible(true) }
    }

    @Test
    fun `test validateNCDPatientStatus returns false when hypertension status is empty`() {
        // Given
        fragment.binding = mockBinding
        fragment.viewModel = mockMentalHealthViewModel

        every { mockMentalHealthViewModel.resultDiabetesHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.DIABETES to "Known Patient"
        )
        every { mockMentalHealthViewModel.resultHypertensionHashMap } returns mutableMapOf()

        // When
        val result = fragment.validateNCDPatientStatus()

        // Then
        assertFalse(result)
        verify { mockTvHypertensionError.setVisible(true) }
    }

    @Test
    fun `test validateNCDPatientStatus validates known patient diabetes requirements`() {
        // Given
        fragment.binding = mockBinding
        fragment.viewModel = mockMentalHealthViewModel

        every { mockMentalHealthViewModel.resultDiabetesHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.DIABETES to "Known Patient"
        )
        every { mockMentalHealthViewModel.resultHypertensionHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.HYPERTENSION to "Known Patient"
        )
        every { mockMentalHealthViewModel.value } returns null
        every { mockEtYearOfDiagnosis.isVisible() } returns true

        // When
        val result = fragment.validateNCDPatientStatus()

        // Then
        assertFalse(result)
        verify { mockTvDiabetesControlledError.visible() }
    }

    @Test
    fun `test isValidDiagnosis calls MotherNeonateUtil correctly`() {
        // Given
        fragment.binding = mockBinding
        val editText = mockEtYrOfDiagnosis
        val errorText = mockTvYrOfDiagnosisError
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        every { editText.text.toString() } returns "2020"
        every { MotherNeonateUtil.isValidInput(any(), any(), any(), any(), any(), any(), any()) } returns true

        // When
        val result = fragment.isValidDiagnosis(editText, errorText)

        // Then
        assertTrue(result)
        verify { 
            MotherNeonateUtil.isValidInput(
                "2020",
                editText,
                errorText,
                1920.0..2024.0,
                R.string.error_label,
                true,
                context
            )
        }
    }

    @Test
    fun `test isValidDiagnosisTwo calls MotherNeonateUtil correctly`() {
        // Given
        fragment.binding = mockBinding
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        every { mockEtYearOfDiagnosis.text.toString() } returns "2020"
        every { MotherNeonateUtil.isValidInput(any(), any(), any(), any(), any(), any(), any()) } returns true

        // When
        val result = fragment.isValidDiagnosisTwo()

        // Then
        assertTrue(result)
        verify { 
            MotherNeonateUtil.isValidInput(
                "2020",
                mockEtYearOfDiagnosis,
                mockTvYearOfDiagnosisErrorHtn,
                1900.0..2024.0,
                R.string.error_label,
                true,
                context
            )
        }
    }

    @Test
    fun `test showLoading sets correct UI state`() {
        // Given
        fragment.binding = mockBinding

        // When
        fragment.showLoading()

        // Then
        verify { 
            mockBtnConfirm.invisible()
            mockBtnCancel.invisible()
            mockLoadingProgress.visible()
        }
    }

    @Test
    fun `test hideLoading sets correct UI state`() {
        // Given
        fragment.binding = mockBinding

        // When
        fragment.hideLoading()

        // Then
        verify { 
            mockBtnConfirm.visible()
            mockBtnCancel.visible()
            mockLoadingProgress.gone()
        }
    }

    @Test
    fun `test onClick with confirm button calls validateInput and creates request when valid`() {
        // Given
        fragment.binding = mockBinding
        fragment.viewModel = mockMentalHealthViewModel

        every { mockMentalHealthViewModel.resultMentalHealthHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.MENTAL_HEALTH_STATUS to "Known Patient"
        )
        every { mockMentalHealthViewModel.resultSubstanceUseHashMap } returns mutableMapOf(
            NCDMentalHealthFragment.SUBSTANCE_USE_STATUS to "Known Patient"
        )
        every { mockMentalHealthViewModel.selectedMentalHealthListItem } returns mutableListOf(
            MultiSelectDropDownModel(1L, "Test", "Test", "test_value")
        )
        every { mockMentalHealthViewModel.selectedSubstanceListItem } returns mutableListOf(
            MultiSelectDropDownModel(1L, "Test", "Test", "test_value")
        )
        every { mockMentalHealthViewModel.mentalHealthComments } returns "Test comments"
        every { mockMentalHealthViewModel.substanceUseComments } returns "Test comments"
        every { mockMentalHealthViewModel.patientStatusId } returns "test_patient_id"
        every { mockMentalHealthViewModel.mentalHealthStatusId } returns "test_mh_id"
        every { mockMentalHealthViewModel.substanceUseStatusId } returns "test_su_id"
        every { mockMentalHealthViewModel.yearForMentalHealth } returns 2020
        every { mockMentalHealthViewModel.yearForSubstanceUse } returns 2021
        every { mockMentalHealthViewModel.createMentalHealthStatus(any()) } just Runs

        // When
        fragment.onClick(mockBtnConfirm)

        // Then
        verify { mockMentalHealthViewModel.createMentalHealthStatus(any()) }
    }

    @Test
    fun `test onClick with cancel button calls listener closePage`() {
        // Given
        fragment.binding = mockBinding
        fragment.listener = mockListener

        // When
        fragment.onClick(mockBtnCancel)

        // Then
        verify { mockListener.closePage() }
    }

    @Test
    fun `test onClick with close button calls listener closePage`() {
        // Given
        fragment.binding = mockBinding
        fragment.listener = mockListener

        // When
        fragment.onClick(mockIvClose)

        // Then
        verify { mockListener.closePage() }
    }

    @Test
    fun `test getSingleSelectionOptions returns correct options`() {
        // Given
        fragment.binding = mockBinding

        // When
        val result = fragment.getSingleSelectionOptions()

        // Then
        assertEquals(3, result.size)
        assertEquals(NCDMentalHealthFragment.N_A, result[0][DefinedParams.Value])
        assertEquals(NCDMentalHealthFragment.Newly_Diagnosed, result[1][DefinedParams.Value])
        assertEquals(NCDMentalHealthFragment.Known_patient, result[2][DefinedParams.Value])
    }

    @Test
    fun `test showViews shows year of diagnosis for known patient`() {
        // Given
        fragment.binding = mockBinding
        val group = mockGroupYearOfDiagnosis
        val selectedValue = NCDMentalHealthFragment.Known_patient
        val errorText = mockTvYearOfDiagnosisError
        val editText = mockEtYearOfDiagnosis

        // When
        fragment.showViews(group, selectedValue, errorText, editText)

        // Then
        verify { 
            group.isVisible = true
            errorText.gone()
        }
    }

    @Test
    fun `test showViews hides year of diagnosis for non-known patient`() {
        // Given
        fragment.binding = mockBinding
        val group = mockGroupYearOfDiagnosis
        val selectedValue = NCDMentalHealthFragment.N_A
        val errorText = mockTvYearOfDiagnosisError
        val editText = mockEtYearOfDiagnosis

        // When
        fragment.showViews(group, selectedValue, errorText, editText)

        // Then
        verify { 
            group.isVisible = false
            errorText.gone()
        }
    }

    @Test
    fun `test showSpinnerView shows spinner for known or newly diagnosed`() {
        // Given
        fragment.binding = mockBinding
        fragment.viewModel = mockMentalHealthViewModel
        val selectedValue = NCDMentalHealthFragment.Known_patient

        every { mockMentalHealthViewModel.value } returns null

        // When
        fragment.showSpinnerView(selectedValue)

        // Then
        verify { 
            mockGroupDiabetesSpinner.isVisible = true
            mockTvDiabetesControlledError.gone()
        }
    }

    @Test
    fun `test showSpinnerView hides spinner for other values`() {
        // Given
        fragment.binding = mockBinding
        fragment.viewModel = mockMentalHealthViewModel
        val selectedValue = NCDMentalHealthFragment.N_A

        every { mockMentalHealthViewModel.value } returns null

        // When
        fragment.showSpinnerView(selectedValue)

        // Then
        verify { 
            mockGroupDiabetesSpinner.isVisible = false
            mockTvDiabetesControlledError.gone()
        }
    }

    @Test
    fun `test handleOrientation sets correct dialog dimensions for tablet landscape`() {
        // Given
        fragment.binding = mockBinding
        val mockContext = mockk<android.content.Context>()
        val mockResources = mockk<android.content.res.Resources>()
        val mockConfiguration = Configuration()

        every { fragment.requireContext() } returns mockContext
        every { mockContext.resources } returns mockResources
        every { mockResources.configuration } returns mockConfiguration
        mockConfiguration.orientation = Configuration.ORIENTATION_LANDSCAPE

        // Mock CommonUtils.checkIsTablet to return true
        mockkObject(com.medtroniclabs.spice.common.CommonUtils)
        every { com.medtroniclabs.spice.common.CommonUtils.checkIsTablet(any()) } returns true

        // Mock setDialogPercent
        mockkObject(com.medtroniclabs.spice.appextensions.ViewExtensions)
        every { com.medtroniclabs.spice.appextensions.setDialogPercent(any(), any()) } just Runs

        // When
        fragment.handleOrientation()

        // Then
        verify { com.medtroniclabs.spice.appextensions.setDialogPercent(50, 95) }
    }

    @Test
    fun `test handleOrientation sets correct dialog dimensions for phone`() {
        // Given
        fragment.binding = mockBinding
        val mockContext = mockk<android.content.Context>()
        val mockResources = mockk<android.content.res.Resources>()
        val mockConfiguration = Configuration()

        every { fragment.requireContext() } returns mockContext
        every { mockContext.resources } returns mockResources
        every { mockResources.configuration } returns mockConfiguration
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT

        // Mock CommonUtils.checkIsTablet to return false
        mockkObject(com.medtroniclabs.spice.common.CommonUtils)
        every { com.medtroniclabs.spice.common.CommonUtils.checkIsTablet(any()) } returns false

        // Mock setDialogPercent
        mockkObject(com.medtroniclabs.spice.appextensions.ViewExtensions)
        every { com.medtroniclabs.spice.appextensions.setDialogPercent(any(), any()) } just Runs

        // When
        fragment.handleOrientation()

        // Then
        verify { com.medtroniclabs.spice.appextensions.setDialogPercent(100, 100) }
    }
} 
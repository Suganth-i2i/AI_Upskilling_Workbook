package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.content.Context
import android.content.res.Configuration
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.Group
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMentalHealthViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil

/**
 * Wrapper class to improve testability of NCDMentalHealthFragment
 * Extracts business logic into testable methods
 */
class NCDMentalHealthFragmentWrapper(
    private val context: Context,
    private val viewModel: NCDMentalHealthViewModel
) {

    /**
     * Validates mental health and substance use inputs
     */
    fun validateMentalHealthAndSubstance(
        resultMentalHealthHashMap: Map<String, Any>,
        resultSubstanceUseHashMap: Map<String, Any>,
        selectedMentalHealthListItem: List<MultiSelectDropDownModel>,
        selectedSubstanceListItem: List<MultiSelectDropDownModel>,
        mentalHealthComments: String?,
        substanceUseComments: String?,
        etYrOfDiagnosis: AppCompatEditText,
        etSubstanceDiagnosis: AppCompatEditText,
        tvMentalHealthError: AppCompatTextView,
        tvSubstanceUseError: AppCompatTextView,
        tvMentalHealthDisorderError: AppCompatTextView,
        tvCommentsError: AppCompatTextView,
        tvSubstanceDisorderError: AppCompatTextView,
        tvSubstanceCommentsError: AppCompatTextView,
        tvYrOfDiagnosisError: AppCompatTextView,
        tvDiagnosisError: AppCompatTextView
    ): Boolean {
        // Mental Health validation
        val isMentalHealthValid = resultMentalHealthHashMap.isNotEmpty()
        val isMentalHealthValueValid = selectedMentalHealthListItem.isNotEmpty()
        val isCommentsValidMentalHealth = mentalHealthComments?.isNotEmpty() == true

        // Substance Use validation
        val isSubstanceUseValid = resultSubstanceUseHashMap.isNotEmpty()
        val isSubstanceUseValueValid = selectedSubstanceListItem.isNotEmpty()
        val isCommentsValidSubstanceUse = substanceUseComments?.isNotEmpty() == true

        tvMentalHealthError.isVisible = !isMentalHealthValid
        tvSubstanceUseError.isVisible = !isSubstanceUseValid

        val isKnownMH = (resultMentalHealthHashMap[NCDMentalHealthFragment.MENTAL_HEALTH_STATUS] as? String)?.equals(NCDMentalHealthFragment.Known_patient, true) == true
        val isKnownSU = (resultSubstanceUseHashMap[NCDMentalHealthFragment.SUBSTANCE_USE_STATUS] as? String)?.equals(NCDMentalHealthFragment.Known_patient, true) == true

        // Validate Mental Health Disorder and Comments for both Known Patient and Newly Diagnosed
        if (isKnownMH) {
            tvMentalHealthDisorderError.isVisible = !isMentalHealthValueValid
            tvCommentsError.isVisible = !isCommentsValidMentalHealth
        }

        // Validate Substance Use Disorder and Comments for both Known Patient and Newly Diagnosed
        if (isKnownSU) {
            tvSubstanceDisorderError.isVisible = !isSubstanceUseValueValid
            tvSubstanceCommentsError.isVisible = !isCommentsValidSubstanceUse
        }

        // Validate year of diagnosis only for Known Patient
        val isYearsValidMentalHealth = if (isKnownMH) {
            isValidDiagnosis(etYrOfDiagnosis, tvYrOfDiagnosisError)
        } else {
            true
        }

        val isYearsValidSubstanceUse = if (isKnownSU) {
            isValidDiagnosis(etSubstanceDiagnosis, tvDiagnosisError)
        } else {
            true
        }

        // For Mental Health, require both status and (if Known/Newly Diagnosed) disorder and comments
        val isMentalHealthComplete = isMentalHealthValid && 
            (!isKnownMH || (isMentalHealthValueValid && isCommentsValidMentalHealth))

        // For Substance Use, require both status and (if Known/Newly Diagnosed) disorder and comments
        val isSubstanceUseComplete = isSubstanceUseValid && 
            (!isKnownSU || (isSubstanceUseValueValid && isCommentsValidSubstanceUse))

        return isMentalHealthComplete && isSubstanceUseComplete && isYearsValidMentalHealth && isYearsValidSubstanceUse
    }

    /**
     * Validates NCD patient status inputs
     */
    fun validateNCDPatientStatus(
        resultDiabetesHashMap: Map<String, Any>,
        resultHypertensionHashMap: Map<String, Any>,
        value: String?,
        etYearOfDiagnosis: AppCompatEditText,
        etYearOfDiagnosisHtn: AppCompatEditText,
        tvDiabetesError: AppCompatTextView,
        tvHypertensionError: AppCompatTextView,
        tvDiabetesControlledError: AppCompatTextView,
        tvYearOfDiagnosisError: AppCompatTextView,
        tvYearOfDiagnosisErrorHtn: AppCompatTextView
    ): Boolean {
        val isDiabetesValid = resultDiabetesHashMap.isNotEmpty()
        val isHypertensionValid = resultHypertensionHashMap.isNotEmpty()

        tvDiabetesError.isVisible = !isDiabetesValid
        tvHypertensionError.isVisible = !isHypertensionValid

        if (!isDiabetesValid || !isHypertensionValid) {
            return false
        }

        val diabetesStatus = resultDiabetesHashMap[NCDMentalHealthFragment.DIABETES] as? String
        val hypertensionStatus = resultHypertensionHashMap[NCDMentalHealthFragment.HYPERTENSION] as? String

        if (diabetesStatus == NCDMentalHealthFragment.Known_patient) {
            if (etYearOfDiagnosis.isVisible) {
                if (!isValidDiagnosis(etYearOfDiagnosis, tvYearOfDiagnosisError)) {
                    return false
                }
            }
            if (value.isNullOrBlank()) {
                tvDiabetesControlledError.isVisible = true
                return false
            }
            tvDiabetesControlledError.isVisible = false
        } else if (diabetesStatus == NCDMentalHealthFragment.Newly_Diagnosed) {
            if (value.isNullOrBlank()) {
                tvDiabetesControlledError.isVisible = true
                return false
            }
            tvDiabetesControlledError.isVisible = false
        }

        when (hypertensionStatus) {
            NCDMentalHealthFragment.Known_patient -> {
                if (!isValidDiagnosisTwo(etYearOfDiagnosisHtn, tvYearOfDiagnosisErrorHtn)) {
                    tvYearOfDiagnosisErrorHtn.isVisible = true
                    return false
                }
                tvYearOfDiagnosisErrorHtn.isVisible = false
            }
        }

        return true
    }

    /**
     * Validates diagnosis year input
     */
    fun isValidDiagnosis(
        etYearOfDiagnosis: AppCompatEditText,
        tvYearOfDiagnosisError: AppCompatTextView
    ): Boolean {
        return MotherNeonateUtil.isValidInput(
            etYearOfDiagnosis.text.toString(),
            etYearOfDiagnosis,
            tvYearOfDiagnosisError,
            1920.0..DateUtils.getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            context
        )
    }

    /**
     * Validates diagnosis year input for hypertension
     */
    fun isValidDiagnosisTwo(
        etYearOfDiagnosisHtn: AppCompatEditText,
        tvYearOfDiagnosisErrorHtn: AppCompatTextView
    ): Boolean {
        return MotherNeonateUtil.isValidInput(
            etYearOfDiagnosisHtn.text.toString(),
            etYearOfDiagnosisHtn,
            tvYearOfDiagnosisErrorHtn,
            1900.0..DateUtils.getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            context
        )
    }

    /**
     * Determines if spinner should be shown based on selected value
     */
    fun shouldShowSpinner(selectedValue: String): Boolean {
        return selectedValue.equals(NCDMentalHealthFragment.Known_patient, ignoreCase = true) ||
                selectedValue.equals(NCDMentalHealthFragment.Newly_Diagnosed, ignoreCase = true)
    }

    /**
     * Determines if year of diagnosis should be shown based on selected value
     */
    fun shouldShowYearOfDiagnosis(selectedValue: String): Boolean {
        return selectedValue.equals(NCDMentalHealthFragment.Known_patient, true)
    }

    /**
     * Calculates dialog dimensions based on device type and orientation
     */
    fun calculateDialogDimensions(): Pair<Int, Int> {
        val isTablet = CommonUtils.checkIsTablet(context)
        val isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        
        return when {
            isTablet && isLandscape -> Pair(50, 95)
            else -> Pair(100, 100)
        }
    }

    /**
     * Creates single selection options
     */
    fun createSingleSelectionOptions(): List<Map<String, Any>> {
        return listOf(
            mapOf(
                "name" to context.getString(R.string.na),
                "id" to NCDMentalHealthFragment.N_A,
                "value" to NCDMentalHealthFragment.N_A
            ),
            mapOf(
                "name" to context.getString(R.string.newly_Diagnosed),
                "id" to NCDMentalHealthFragment.Newly_Diagnosed,
                "value" to NCDMentalHealthFragment.Newly_Diagnosed
            ),
            mapOf(
                "name" to context.getString(R.string.known_patient),
                "id" to NCDMentalHealthFragment.Known_patient,
                "value" to NCDMentalHealthFragment.Known_patient
            )
        )
    }

    /**
     * Processes NCD diagnosis data for spinner
     */
    fun processNCDDiagnosisData(data: List<NCDDiagnosisEntity>): List<Map<String, Any>> {
        val list = mutableListOf<Map<String, Any>>()
        list.add(
            mapOf(
                "name" to context.getString(R.string.please_select),
                "id" to -1L
            )
        )

        data.forEach { symptoms ->
            if (symptoms.name != "Pre-Diabetes") {
                val item = mutableMapOf<String, Any>()
                item["id"] = symptoms.id
                item["name"] = symptoms.name
                symptoms.displayValue?.let { item["cultureValue"] = it }
                symptoms.value?.let { item["value"] = it }
                list.add(item)
            }
        }
        
        return list
    }

    /**
     * Validates gender input
     */
    fun validateGender(isFemale: Boolean): String {
        return if (isFemale) {
            "female"
        } else {
            "male"
        }
    }

    /**
     * Validates pregnancy status
     */
    fun validatePregnancyStatus(isPregnant: Boolean): Boolean {
        return isPregnant
    }

    /**
     * Validates NCD visibility
     */
    fun validateNCDVisibility(showNCD: Boolean): Boolean {
        return showNCD
    }
} 
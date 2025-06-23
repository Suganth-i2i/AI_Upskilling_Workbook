package com.medtroniclabs.spice.ncd.medicalreview.dialog

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.isVisible
import com.medtroniclabs.spice.appextensions.loadAsGif
import com.medtroniclabs.spice.appextensions.resetImageView
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentNCDMentalHealthBinding
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.formgeneration.utility.MultiSelectSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.data.MentalHealthStatus
import com.medtroniclabs.spice.ncd.data.NCDMentalHealthStatusRequest
import com.medtroniclabs.spice.ncd.data.NcdPatientStatus
import com.medtroniclabs.spice.ncd.medicalreview.NCDDialogDismissListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDPatientHistoryDialog.Companion.Newly_Diagnosed
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewViewModel
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMentalHealthViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDMentalHealthFragment : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentNCDMentalHealthBinding

    private val viewModel: NCDMentalHealthViewModel by activityViewModels()
    private val medicalReviewViewModel: NCDMedicalReviewViewModel by activityViewModels()

    val adapter by lazy { CustomSpinnerAdapter(requireContext()) }
    var listener: NCDDialogDismissListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNCDMentalHealthBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        handleOrientation()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleOrientation()
    }

    private fun handleOrientation() {
        val isTablet = CommonUtils.checkIsTablet(requireContext())
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = when {
            isTablet && isLandscape -> 50
            else -> 100
        }
        val height = when {
            isTablet && isLandscape -> 95
            else -> 100
        }
        setDialogPercent(width, height)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
        setListeners()
    }

    private fun prefillMH() {
        val mentalHealth = ArrayList<String>()
        val substanceUse = ArrayList<String>()
        medicalReviewViewModel.ncdPatientDiagnosisStatus.value?.data?.let { responseMap ->
            viewModel.patientStatusId = responseMap[DefinedParams.ID] as? String
            (responseMap[NCDMRUtil.MentalHealthStatus] as? Map<*, *>)?.let { mhsMap ->
                viewModel.mentalHealthStatusId = mhsMap[DefinedParams.ID] as? String
                (mhsMap[DefinedParams.Status] as? String)?.let { status ->
                    with(binding.llMentalHealth) {
                        if (childCount > 0) (getChildAt(0) as? SingleSelectionCustomView)?.singleSelectionAutofill(
                            status + "_${MENTAL_HEALTH_STATUS}"
                        )
                    }
                }
                (mhsMap[DefinedParams.MentalHealthDisorder] as? ArrayList<String>)?.let { disorders ->
                    mentalHealth.addAll(disorders)
                }
                (mhsMap[DefinedParams.Comments] as? String)?.let { comments ->
                    binding.etComments.setText(comments)
                }
                (mhsMap[DefinedParams.YearOfDiagnosis] as? String)?.let { yearOfDiagnosis ->
                    binding.etYrOfDiagnosis.setText(yearOfDiagnosis)
                }
            }
            (responseMap[NCDMRUtil.SubstanceUseStatus] as? Map<*, *>)?.let { susMap ->
                viewModel.substanceUseStatusId = susMap[DefinedParams.ID] as? String
                (susMap[DefinedParams.Status] as? String)?.let { status ->
                    with(binding.llSubstanceUse) {
                        if (childCount > 0) (getChildAt(0) as? SingleSelectionCustomView)?.singleSelectionAutofill(
                            status + "_${SUBSTANCE_USE_STATUS}"
                        )
                    }
                }
                (susMap[DefinedParams.MentalHealthDisorder] as? ArrayList<String>)?.let { disorders ->
                    substanceUse.addAll(disorders)
                }
                (susMap[DefinedParams.Comments] as? String)?.let { comments ->
                    binding.etSubstanceComments.setText(comments)
                }
                (susMap[DefinedParams.YearOfDiagnosis] as? String)?.let { yearOfDiagnosis ->
                    binding.etSubstanceDiagnosis.setText(yearOfDiagnosis)
                }
            }
        }
        initializeMentalHealthSpinner(mentalHealth)
        initializeSubstanceSpinner(substanceUse)
    }

    private fun prefill() {
        medicalReviewViewModel.ncdPatientDiagnosisStatus.value?.data?.let { responseMap ->
            viewModel.patientStatusId = responseMap[DefinedParams.ID] as? String
            (responseMap[NCDMRUtil.NCDPatientStatus] as? Map<*, *>)?.let { ncdMap ->
                viewModel.id = ncdMap[DefinedParams.ID] as? String
                //Diabetes
                (ncdMap[NCDMRUtil.DiabetesStatus] as? String)?.let { diabetesStatus ->
                    with(binding.ncdDiabetesHypertension.llDiabetes) {
                        if (childCount > 0) (getChildAt(0) as? SingleSelectionCustomView)?.singleSelectionAutofill(
                            diabetesStatus + "_${Diabetes}"
                        )
                    }
                }
                (ncdMap[NCDMRUtil.DiabetesYearOfDiagnosis] as? String)?.let { diabetesYear ->
                    binding.ncdDiabetesHypertension.etYearOfDiagnosis.setText(diabetesYear)
                }
                (ncdMap[NCDMRUtil.DiabetesControlledType] as? String)?.let { diabetesType ->
                    (binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.adapter as? CustomSpinnerAdapter)?.let {
                        it.getIndexOfItemByName(diabetesType).let { pos ->
                            if (pos > 0)
                                binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.post {
                                    binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.setSelection(
                                        pos,
                                        false
                                    )
                                }
                        }
                    }
                }


                //Hypertension
                (ncdMap[NCDMRUtil.HypertensionStatus] as? String)?.let { hypertensionStatus ->
                    with(binding.ncdDiabetesHypertension.llHypertension) {
                        if (childCount > 0) (getChildAt(0) as? SingleSelectionCustomView)?.singleSelectionAutofill(
                            hypertensionStatus + "_${Hypertension}"
                        )
                    }
                }
                (ncdMap[NCDMRUtil.HypertensionYearOfDiagnosis] as? String)?.let { hypertensionYear ->
                    binding.ncdDiabetesHypertension.etYearOfDiagnosisHtn.setText(hypertensionYear)
                }
            }
        }
    }

    private fun setListeners() {
        binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?, view: View?, pos: Int, itemId: Long
                ) {
                    adapter.getData(pos)?.let {
                        val selectedId = (it[DefinedParams.id] as? Long) ?: -1L
                        val selectedName = it[DefinedParams.NAME] as String?
                        val value = it[DefinedParams.Value] as String?
                        if (selectedId != -1L) {
                            viewModel.value = value
                        } else {
                            viewModel.value = null
                        }
                        medicalReviewViewModel.statusDiabetesValue = viewModel.value
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun attachObserver() {
        viewModel.getSymptomListByTypeForNCDLiveData.observe(viewLifecycleOwner) {
            medicalReviewViewModel.validationForStatus = it
            loadSiteDetails(ArrayList(it))
        }
        viewModel.getMHLiveData.observe(viewLifecycleOwner) {
            viewModel.getSubstanceAbuse(
                NCDMRUtil.SUBSTANCE_DISORDER.lowercase(),
                getGender(),
                isPregnant()
            )
        }
        viewModel.getSubstanceAbuseLiveData.observe(viewLifecycleOwner) {
            prefillMH()
        }
        viewModel.createMentalHealthStatus.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    dismiss()
                    listener?.onDialogDismissed(true)
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun initializeMentalHealthSpinner(mentalHealth: List<String>) {
        val dropDownList = viewModel.getMHLiveData.value
            ?.map { MultiSelectDropDownModel(it.id, it.name, it.displayValue, it.value) }
            ?: emptyList()

        viewModel.selectedMentalHealthListItem.clear()
        viewModel.selectedMentalHealthListItem.addAll(dropDownList.filter { it.value in mentalHealth })

        val adapter = MultiSelectSpinnerAdapter(
            requireContext(),
            dropDownList,
            viewModel.selectedMentalHealthListItem
        )
        binding.etMentalHealthDisorder.adapter = adapter
        adapter.setOnItemSelectedListener(object :
            MultiSelectSpinnerAdapter.OnItemSelectedListener {
            override fun onItemSelected(
                selectedItems: List<MultiSelectDropDownModel>,
                pos: Int,
            ) {
                viewModel.selectedMentalHealthListItem = ArrayList(selectedItems)
            }
        }
        )
    }

    private fun initializeSubstanceSpinner(substanceUse: ArrayList<String>) {
        val dropDownList = viewModel.getSubstanceAbuseLiveData.value
            ?.map { MultiSelectDropDownModel(it.id, it.name, it.displayValue, it.value) }
            ?: emptyList()
        viewModel.selectedSubstanceListItem.clear()
        viewModel.selectedSubstanceListItem.addAll(dropDownList.filter { it.value in substanceUse })

        val adapter = MultiSelectSpinnerAdapter(
            requireContext(),
            dropDownList,
            viewModel.selectedSubstanceListItem
        )
        binding.etSubstanceDisorder.adapter = adapter
        adapter.setOnItemSelectedListener(object :
            MultiSelectSpinnerAdapter.OnItemSelectedListener {
            override fun onItemSelected(
                selectedItems: List<MultiSelectDropDownModel>,
                pos: Int,
            ) {
                viewModel.selectedSubstanceListItem = ArrayList(selectedItems)
            }
        }
        )
    }

    private fun loadSiteDetails(data: ArrayList<NCDDiagnosisEntity>) {
        val list = arrayListOf<Map<String, Any>>(
            hashMapOf(
                DefinedParams.NAME to getString(R.string.please_select),
                DefinedParams.ID to DefinedParams.DefaultSelectID
            )
        )

        data.mapNotNullTo(list) { symptoms ->
            hashMapOf<String, Any>().apply {
                put(DefinedParams.ID, symptoms.id)
                put(DefinedParams.NAME, symptoms.name)
                symptoms.displayValue?.let { put(DefinedParams.cultureValue, it) }
                symptoms.value?.let { put(DefinedParams.Value, it) }
            }.takeIf { it.isNotEmpty() && it[DefinedParams.NAME]?.equals(DefinedParams.Pre_Diabetes) != true  }
        }
        adapter.setData(list)
        binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.post {
            binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.setSelection(0, false)
        }
        binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.adapter = adapter

        prefill()
    }

    private fun getSingleSelectionOptions(): ArrayList<Map<String, Any>> {
        val yearOfDiagnosis = ArrayList<Map<String, Any>>()
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                N_A,
                N_A,
                getString(R.string.na)
            )
        )
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                Newly_Diagnosed,
                Newly_Diagnosed,
                getString(R.string.newly_Diagnosed)
            )
        )
        yearOfDiagnosis.add(
            CommonUtils.getOptionMap(
                Known_patient,
                Known_patient,
                getString(R.string.known_patient)
            )
        )
        return yearOfDiagnosis
    }

    private var singleSelectionCallbackForDiabetes: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultDiabetesHashMap[NCDPregnancyDialog.DIABETES] = selectedID as String
            showViews(
                binding.ncdDiabetesHypertension.groupYearOfDiagnosis,
                selectedID,
                binding.ncdDiabetesHypertension.tvYearOfDiagnosisError,
                binding.ncdDiabetesHypertension.etYearOfDiagnosis
            )
            showSpinnerView(selectedID)

            // Reset Diabetes type spinner to 0th index
            binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.post {
                binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.setSelection(0, false)
            }
            viewModel.value = null
        }

    private var singleSelectionCallbackForSubstanceUse: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultSubstanceUseHashMap[SUBSTANCE_USE_STATUS] =
                selectedID as String

            val isKnownDiagnosed = selectedID.equals(Known_patient, ignoreCase = true)

            binding.apply {
                groupSubstanceUse.setVisible(isKnownDiagnosed)
                groupSubstanceUseSpinner.setVisible(isKnownDiagnosed)

                etSubstanceDiagnosis.text?.clear()
                etSubstanceComments.text?.clear()
                (etSubstanceDisorder.adapter as? MultiSelectSpinnerAdapter)?.reset()

                tvDiagnosisError.gone()
                tvSubstanceCommentsError.gone()
                tvSubstanceDisorderError.gone()
            }
        }

    private var singleSelectionCallbackForMentalHealth: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultMentalHealthHashMap[MENTAL_HEALTH_STATUS] =
                selectedID as String

            val isKnownDiagnosed = selectedID.equals(Known_patient, ignoreCase = true)

            binding.apply {
                groupMentalHealth.setVisible(isKnownDiagnosed)
                groupMentalHealthSpinner.setVisible(isKnownDiagnosed)

                etComments.text?.clear()
                etYrOfDiagnosis.text?.clear()
                (etMentalHealthDisorder.adapter as? MultiSelectSpinnerAdapter)?.reset()

                tvMentalHealthDisorderError.gone()
                tvCommentsError.gone()
                tvYrOfDiagnosisError.gone()
            }
        }

    private fun showSpinnerView(selectedValue: String) {
        val isKnownOrNewlyDiagnosed = selectedValue.equals(Known_patient, ignoreCase = true) ||
                                    selectedValue.equals(Newly_Diagnosed, ignoreCase = true)
        with(binding.ncdDiabetesHypertension) {
            groupDiabetesSpinner.isVisible = isKnownOrNewlyDiagnosed
            if (!isKnownOrNewlyDiagnosed) {
                etYearOfDiagnosis.setText(getString(R.string.empty))
                viewModel.value = null
                tvDiabetesControlledSpinner.post {
                    tvDiabetesControlledSpinner.setSelection(0, false)
                }
            }
            tvDiabetesControlledError.gone()
        }
    }

    private fun showViews(
        groupYearOfDiagnosis: Group,
        selectedValue: String,
        tvYearOfDiagnosis: AppCompatTextView,
        etYearOfDiagnosis: AppCompatEditText
    ) {
        if (selectedValue.equals(Known_patient, true)) {
            groupYearOfDiagnosis.isVisible = true
            etYearOfDiagnosis.text = null
        } else {
            groupYearOfDiagnosis.isVisible = false
            etYearOfDiagnosis.text = null
        }
        tvYearOfDiagnosis.gone()
    }

    private fun initView() {
        ncdVisibility()
        binding.apply {
            tvMentalHealth.markMandatory()
            tvMentalHealthDisorder.markMandatory()
            tvComments.markMandatory()
            tvYrOfDiagnosis.markMandatory()
            tvSubstanceUse.markMandatory()
            tvSubstanceDiagnosis.markMandatory()
            tvSubstanceDisorder.markMandatory()
            tvSubstanceComments.markMandatory()
            btnCancel.safeClickListener(this@NCDMentalHealthFragment)
            btnConfirm.safeClickListener(this@NCDMentalHealthFragment)
            ivClose.safeClickListener(this@NCDMentalHealthFragment)
            MotherNeonateUtil.initTextWatcherForString(ncdDiabetesHypertension.etYearOfDiagnosis) {
                viewModel.yearForDiabetes = it
            }
            MotherNeonateUtil.initTextWatcherForString(ncdDiabetesHypertension.etYearOfDiagnosisHtn) {
                viewModel.yearForHypertension = it
            }
            MotherNeonateUtil.initTextWatcherForString(etComments) {
                viewModel.mentalHealthComments = it
            }
            MotherNeonateUtil.initTextWatcherForString(etSubstanceComments) {
                viewModel.substanceUseComments = it
            }
            MotherNeonateUtil.initTextWatcherForInt(etYrOfDiagnosis) {
                viewModel.yearForMentalHealth = it
            }
            MotherNeonateUtil.initTextWatcherForInt(etSubstanceDiagnosis) {
                viewModel.yearForSubstanceUse = it
            }
        }
        binding.ncdDiabetesHypertension.apply {
            tvDiabetes.markMandatory()
            tvYearOfDiagnosis.markMandatory()
            tvDiabetesControlledTypeLabel.markMandatory()
            MotherNeonateUtil.initTextWatcherForString(etYearOfDiagnosis) {
                viewModel.yearForDiabetes = it
            }

            tvHypertension.markMandatory()
            tvYearOfDiagnosisHtn.markMandatory()
            MotherNeonateUtil.initTextWatcherForString(etYearOfDiagnosisHtn) {
                viewModel.yearForHypertension = it
            }
        }
        viewModel.getSymptoms(
            Diabetes.lowercase(),
            getGender(),
            isPregnant()
        )
        viewModel.getMH(
            NCDMRUtil.MENTALHEALTH.lowercase(),
            getGender(),
            isPregnant()
        )
        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = DIABETES
            view.addViewElements(
                it,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultDiabetesHashMap,
                Pair(DIABETES, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForDiabetes
            )
            binding.ncdDiabetesHypertension.llDiabetes.addView(view)
        }

        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = HYPERTENSION
            view.addViewElements(
                it,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultHypertensionHashMap,
                Pair(HYPERTENSION, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForHypertension
            )
            binding.ncdDiabetesHypertension.llHypertension.addView(view)
        }

        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = MENTAL_HEALTH_STATUS
            view.addViewElements(
                it,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultMentalHealthHashMap,
                Pair(MENTAL_HEALTH_STATUS, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForMentalHealth
            )
            binding.llMentalHealth.addView(view)
        }

        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = SUBSTANCE_USE_STATUS
            view.addViewElements(
                it,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultSubstanceUseHashMap,
                Pair(SUBSTANCE_USE_STATUS, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForSubstanceUse
            )
            binding.llSubstanceUse.addView(view)
        }
        binding.loadingProgress.safeClickListener {

        }
        binding.loadingProgress.bringToFront()
    }

    private fun ncdVisibility() {
        val showNCD = showNCD()
        binding.apply {
            medicalReviewViewModel.ncdPatientDiagnosisStatus.value?.data?.let { resultData ->
                val patientStatus =
                    (resultData[NCDMRUtil.NCDPatientStatus] as? Map<*, *>)?.let { map ->
                        (map.containsKey(NCDMRUtil.DiabetesStatus) && map[NCDMRUtil.DiabetesStatus] != null) ||
                                (map.containsKey(NCDMRUtil.HypertensionStatus) && map[NCDMRUtil.HypertensionStatus] != null)

                    } ?: false
                if (patientStatus) {
                    tvNCD.gone()
                    binding.ncdDiabetesHypertension.root.gone()
                } else {
                    tvNCD.setVisible(showNCD)
                    ncdDiabetesHypertension.root.setVisible(showNCD)
                }
            } ?: kotlin.run {
                tvNCD.setVisible(showNCD)
                ncdDiabetesHypertension.root.setVisible(showNCD)
            }
        }
    }

    private fun getGender(): String {
        return if (arguments?.getBoolean(NCDMRUtil.IS_FEMALE) == true) {
            Screening.Female.lowercase()
        } else {
            Screening.Male.lowercase()
        }
    }

    private fun isPregnant(): Boolean {
        return arguments?.getBoolean(NCDMRUtil.IsPregnant) ?: false
    }

    private fun showNCD(): Boolean {
        return arguments?.getBoolean(NCDMRUtil.ShowNCD) ?: false
    }

    private var singleSelectionCallbackForHypertension: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultHypertensionHashMap[NCDPregnancyDialog.HYPERTENSION] =
                selectedID as String
            showViews(
                binding.ncdDiabetesHypertension.groupYearOfDiagnosis2,
                selectedID,
                binding.ncdDiabetesHypertension.tvYearOfDiagnosisErrorHtn,
                binding.ncdDiabetesHypertension.etYearOfDiagnosisHtn
            )
        }

    companion object {
        const val TAG = "NCDMentalHealthFragment"
        const val Diabetes = "Diabetes"
        const val Hypertension = "Hypertension"

        const val N_A = "N/A"
        const val New_Patient = "New Patient"
        const val Known_patient = "Known Patient"

        const val DIABETES = "Diabetes"
        const val HYPERTENSION = "Hypertension"

        const val MENTAL_HEALTH_STATUS = "MentalHealthStatus"
        const val SUBSTANCE_USE_STATUS = "SubstanceUseStatus"
        fun newInstance(
            visitId: String?,
            patientReference: String?,
            memberReference: String?,
            isFemale: Boolean,
            isPregnant: Boolean,
            showNCD: Boolean,
        ): NCDMentalHealthFragment {
            return NCDMentalHealthFragment().apply {
                arguments = Bundle().apply {
                    putString(NCDMRUtil.PATIENT_REFERENCE, patientReference)
                    putString(NCDMRUtil.MEMBER_REFERENCE, memberReference)
                    putString(NCDMRUtil.EncounterReference, visitId)
                    putBoolean(NCDMRUtil.IS_FEMALE, isFemale)
                    putBoolean(NCDMRUtil.IsPregnant, isPregnant)
                    putBoolean(NCDMRUtil.ShowNCD, showNCD)
                }
            }
        }
    }

    fun validateInput(): Boolean {
        return if (binding.tvNCD.isVisible()) {
            validateNCDPatientStatus() && validateMentalHealthAndSubstance()
        } else {
            validateMentalHealthAndSubstance()
        }
    }

    private fun validateMentalHealthAndSubstance(): Boolean {
        //Mental Health
        val isMentalHealthValid = viewModel.resultMentalHealthHashMap.isNotEmpty()
        val isMentalHealthValueValid = viewModel.selectedMentalHealthListItem.isNotEmpty()
        val isCommentsValidMentalHealth = viewModel.mentalHealthComments?.isNotEmpty() == true

        //Substance Use
        val isSubstanceUseValid = viewModel.resultSubstanceUseHashMap.isNotEmpty()
        val isSubstanceUseValueValid = viewModel.selectedSubstanceListItem.isNotEmpty()
        val isCommentsValidSubstanceUse = viewModel.substanceUseComments?.isNotEmpty() == true

        binding.tvMentalHealthError.setVisible(!isMentalHealthValid)
        binding.tvSubstanceUseError.setVisible(!isSubstanceUseValid)

        val isKnownMH = (viewModel.resultMentalHealthHashMap[MENTAL_HEALTH_STATUS] as? String)?.equals(Known_patient, true) == true
        val isKnownSU = (viewModel.resultSubstanceUseHashMap[SUBSTANCE_USE_STATUS] as? String)?.equals(Known_patient, true) == true

        // Validate Mental Health Disorder and Comments for both Known Patient and Newly Diagnosed
        if (isKnownMH) {
            binding.tvMentalHealthDisorderError.setVisible(!isMentalHealthValueValid)
            binding.tvCommentsError.setVisible(!isCommentsValidMentalHealth)
        }

        // Validate Substance Use Disorder and Comments for both Known Patient and Newly Diagnosed
        if (isKnownSU) {
            binding.tvSubstanceDisorderError.setVisible(!isSubstanceUseValueValid)
            binding.tvSubstanceCommentsError.setVisible(!isCommentsValidSubstanceUse)
        }

        // Validate year of diagnosis only for Known Patient
        val isYearsValidMentalHealth = if (isKnownMH) {
            isValidDiagnosis(binding.etYrOfDiagnosis, binding.tvYrOfDiagnosisError)
        } else {
            true
        }

        val isYearsValidSubstanceUse = if (isKnownSU) {
            isValidDiagnosis(binding.etSubstanceDiagnosis, binding.tvDiagnosisError)
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

    private fun validateNCDPatientStatus(): Boolean {
        val isDiabetesValid = viewModel.resultDiabetesHashMap.isNotEmpty()
        val isHypertensionValid = viewModel.resultHypertensionHashMap.isNotEmpty()

        binding.ncdDiabetesHypertension.tvDiabetesError.setVisible(!isDiabetesValid)
        binding.ncdDiabetesHypertension.tvHypertensionError.setVisible(!isHypertensionValid)

        if (!isDiabetesValid || !isHypertensionValid) {
            return false
        }

        val diabetesStatus = viewModel.resultDiabetesHashMap[NCDPatientHistoryDialog.Diabetes] as? String
        val hypertensionStatus = viewModel.resultHypertensionHashMap[NCDPatientHistoryDialog.Hypertension] as? String
        val diabetesType = viewModel.value

        if (diabetesStatus == NCDPatientHistoryDialog.Known_patient) {
            if (binding.ncdDiabetesHypertension.etYearOfDiagnosis.isVisible()){
                isValidDiagnosis(
                    binding.ncdDiabetesHypertension.etYearOfDiagnosis,
                    binding.ncdDiabetesHypertension.tvYearOfDiagnosisError)
            }
            if (diabetesType.isNullOrBlank()) {
                binding.ncdDiabetesHypertension.tvDiabetesControlledError.visible()
                return false
            }
            binding.ncdDiabetesHypertension.tvDiabetesControlledError.gone()
        } else if (diabetesStatus == Newly_Diagnosed) {
            if (diabetesType.isNullOrBlank()) {
                binding.ncdDiabetesHypertension.tvDiabetesControlledError.visible()
                return false
            }
            binding.ncdDiabetesHypertension.tvDiabetesControlledError.gone()
        }

        when (hypertensionStatus) {
            NCDPatientHistoryDialog.Known_patient -> {
                if (!isValidDiagnosisTwo()) {
                    binding.ncdDiabetesHypertension.tvYearOfDiagnosisErrorHtn.visible()
                    return false
                }
                binding.ncdDiabetesHypertension.tvYearOfDiagnosisErrorHtn.gone()
            }
        }

        return true
    }

    private fun isValidDiagnosis(
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
            requireContext()
        )
    }

    private fun isValidDiagnosisTwo(): Boolean {
        return MotherNeonateUtil.isValidInput(
            binding.ncdDiabetesHypertension.etYearOfDiagnosisHtn.text.toString(),
            binding.ncdDiabetesHypertension.etYearOfDiagnosisHtn,
            binding.ncdDiabetesHypertension.tvYearOfDiagnosisErrorHtn,
            1900.0..DateUtils.getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            requireContext()
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnCancel.id, binding.ivClose.id -> {
                listener?.closePage()
            }
            binding.btnConfirm.id -> {
                if (validateInput()) {
                    val request = NCDMentalHealthStatusRequest(
                        id = viewModel.patientStatusId,
                        patientVisitId = getEncounterReference(),
                        provenance = ProvanceDto(),
                        memberReference = getMemberReference(),
                        patientReference = getPatientReference(),
                        ncdPatientStatus = NcdPatientStatus(
                            id = viewModel.id,
                            diabetesStatus = viewModel.resultDiabetesHashMap[Diabetes] as? String,
                            hypertensionStatus = viewModel.resultHypertensionHashMap[Hypertension] as? String,
                            hypertensionYearOfDiagnosis = viewModel.yearForHypertension.takeIf { !it.isNullOrBlank() },
                            diabetesYearOfDiagnosis = viewModel.yearForDiabetes.takeIf { !it.isNullOrBlank() },
                            diabetesControlledType = null,
                            diabetesDiagnosis = viewModel.value
                        ),
                        mentalHealthStatus = MentalHealthStatus(
                            id = viewModel.mentalHealthStatusId,
                            status = viewModel.resultMentalHealthHashMap[MENTAL_HEALTH_STATUS] as? String,
                            comments = viewModel.mentalHealthComments.takeIf { !it.isNullOrBlank() },
                            yearOfDiagnosis = viewModel.yearForMentalHealth?.takeIf { true },
                            mentalHealthDisorder = viewModel.selectedMentalHealthListItem.takeIf { it.isNotEmpty() }
                                ?.mapNotNull { it.value } as ArrayList<String>?
                        ),
                        substanceUseStatus = MentalHealthStatus(
                            id = viewModel.substanceUseStatusId,
                            status = viewModel.resultSubstanceUseHashMap[SUBSTANCE_USE_STATUS] as? String,
                            comments = viewModel.substanceUseComments.takeIf { !it.isNullOrBlank() },
                            yearOfDiagnosis = viewModel.yearForSubstanceUse?.takeIf { true },
                            mentalHealthDisorder = viewModel.selectedSubstanceListItem.takeIf { it.isNotEmpty() }
                                ?.mapNotNull { it.value } as ArrayList<String>?
                        )
                    )
                    viewModel.createMentalHealthStatus(request)
                }
            }
        }
    }

    private fun getPatientReference(): String? {
        return arguments?.getString(NCDMRUtil.PATIENT_REFERENCE)
    }

    private fun getMemberReference(): String? {
        return arguments?.getString(NCDMRUtil.MEMBER_REFERENCE)
    }

    private fun getEncounterReference(): String? {
        return arguments?.getString(NCDMRUtil.EncounterReference)
    }

    fun showLoading() {
        binding.apply {
            btnConfirm.invisible()
            btnCancel.invisible()
            loadingProgress.visible()
            loaderImage.apply {
                loadAsGif(R.drawable.loader_spice)
            }
        }
    }

    fun hideLoading() {
        binding.apply {
            btnConfirm.visible()
            btnCancel.visible()
            loadingProgress.gone()
            loaderImage.apply {
                resetImageView()
            }
        }
    }
}
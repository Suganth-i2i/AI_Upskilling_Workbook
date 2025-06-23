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
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.isVisible
import com.medtroniclabs.spice.appextensions.loadAsGif
import com.medtroniclabs.spice.appextensions.resetImageView
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils.getCurrentYearAsDouble
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.FragmentNcdPatientHistoryDialogBinding
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.Screening.Female
import com.medtroniclabs.spice.mappingkey.Screening.Male
import com.medtroniclabs.spice.ncd.data.NCDPatientStatusRequest
import com.medtroniclabs.spice.ncd.data.NcdPatientStatus
import com.medtroniclabs.spice.ncd.medicalreview.NCDDialogDismissListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDPatientHistoryViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDPatientHistoryDialog : DialogFragment(), View.OnClickListener {
    var listener: NCDDialogDismissListener? = null
    private lateinit var binding: FragmentNcdPatientHistoryDialogBinding
    private val viewModel: NCDPatientHistoryViewModel by viewModels()
    private val medicalReviewViewModel : NCDMedicalReviewViewModel by activityViewModels()
    val adapter by lazy { CustomSpinnerAdapter(requireContext(), SecuredPreference.getIsTranslationEnabled()) }

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
            isTablet && isLandscape -> 70
            else -> 100
        }
        setDialogPercent(width, height)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdPatientHistoryDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "NCDPatientHistoryDialog"
        const val Diabetes = "Diabetes"
        const val Hypertension = "Hypertension"
        const val N_A = "N/A"
        const val New_Patient = "New Patient"
        const val Known_patient = "Known Patient"
        const val Newly_Diagnosed = "Newly Diagnosed"
        fun newInstance(
            visitId: String?,
            patientReference: String?,
            memberReference: String?,
            isFemale: Boolean,
            isPregnant: Boolean
        ): NCDPatientHistoryDialog {
            return NCDPatientHistoryDialog().apply {
                arguments = Bundle().apply {
                    putString(NCDMRUtil.VISIT_ID,visitId)
                    putString(NCDMRUtil.PATIENT_REFERENCE, patientReference)
                    putString(NCDMRUtil.MEMBER_REFERENCE, memberReference)
                    putBoolean(NCDMRUtil.IS_FEMALE, isFemale)
                    putBoolean(NCDMRUtil.IsPregnant, isPregnant)
                }
            }
        }
    }

    private fun getPatientReference(): String? {
        return arguments?.getString(NCDMRUtil.PATIENT_REFERENCE)
    }
    private fun getVisitId(): String? {
        return arguments?.getString(NCDMRUtil.VISIT_ID)
    }

    private fun getMemberReference(): String? {
        return arguments?.getString(NCDMRUtil.MEMBER_REFERENCE)
    }

    private fun getGender(): String {
        return if (arguments?.getBoolean(NCDMRUtil.IS_FEMALE) == true) {
            Female.lowercase()
        } else {
            Male.lowercase()
        }
    }
    private fun isPregnant(): Boolean {
        return arguments?.getBoolean(NCDMRUtil.IsPregnant) ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
        setListeners()
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

    private fun attachObserver() {
        viewModel.getSymptomListByTypeForNCDLiveData.observe(viewLifecycleOwner) {
            medicalReviewViewModel.validationForStatus = it
            loadSiteDetails(ArrayList(it))
        }
        viewModel.createNCDPatientStatus.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    listener?.onDialogDismissed(true)
                }

                ResourceState.ERROR -> {
                    hideLoading()
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
            }.takeIf { it.isNotEmpty()&& it[DefinedParams.NAME]?.equals(DefinedParams.Pre_Diabetes) != true  }
        }
        adapter.setData(list)
        binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.post {
            binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.setSelection(0, false)
        }
        binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.adapter = adapter

        prefill()
    }

    private fun initView() {
        medicalReviewViewModel.ncdPatientDiagnosisStatus.value?.data?.let { resultData ->
            val patientStatus = (resultData[NCDMRUtil.NCDPatientStatus] as? Map<*, *>)?.let { map ->
                (map.containsKey(NCDMRUtil.DiabetesStatus) && map[NCDMRUtil.DiabetesStatus] != null) ||
                        (map.containsKey(NCDMRUtil.HypertensionStatus) && map[NCDMRUtil.HypertensionStatus] != null)

            } ?: false
            if (patientStatus) {
                binding.ncdDiabetesHypertension.root.gone()
            }
        }
        binding.apply {

            ncdDiabetesHypertension.apply {
                tvDiabetes.markMandatory()
                tvYearOfDiagnosis.markMandatory()
                tvYearOfDiagnosisHtn.markMandatory()
                tvDiabetesControlledTypeLabel.markMandatory()
                tvHypertension.markMandatory()
                MotherNeonateUtil.initTextWatcherForString(etYearOfDiagnosis) {
                    viewModel.yearForDiabetes = it
                }
                MotherNeonateUtil.initTextWatcherForString(etYearOfDiagnosisHtn) {
                    viewModel.yearForHypertension = it
                }
            }
            btnConfirm.text = getString(R.string.save)
            btnConfirm.isEnabled = true
            btnConfirm.safeClickListener(this@NCDPatientHistoryDialog)
            btnCancel.safeClickListener(this@NCDPatientHistoryDialog)
            ivClose.safeClickListener(this@NCDPatientHistoryDialog)
        }
        viewModel.getSymptoms(Diabetes.lowercase(), getGender(),isPregnant())

        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = Diabetes
            view.addViewElements(
                it,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultDiabetesHashMap,
                Pair(Diabetes, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForDiabetes
            )
            binding.ncdDiabetesHypertension.llDiabetes.addView(view)
        }

        getSingleSelectionOptions().let {
            val view = SingleSelectionCustomView(requireContext())
            view.tag = Hypertension
            view.addViewElements(
                it,
                SecuredPreference.getIsTranslationEnabled(),
                viewModel.resultHypertensionHashMap,
                Pair(Hypertension, null),
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallbackForHypertension
            )
            binding.ncdDiabetesHypertension.llHypertension.addView(view)
        }
    }

    private var singleSelectionCallbackForDiabetes: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultDiabetesHashMap[Diabetes] = selectedID as String
            showViews(
                binding.ncdDiabetesHypertension.groupYearOfDiagnosis,
                selectedID,
                binding.ncdDiabetesHypertension.tvYearOfDiagnosisError,
                binding.ncdDiabetesHypertension.etYearOfDiagnosis
            )
            showSpinnerView(selectedID)
            
            binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.post {
                binding.ncdDiabetesHypertension.tvDiabetesControlledSpinner.setSelection(0, false)
            }
            viewModel.value = null
        }

    private var singleSelectionCallbackForHypertension: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultHypertensionHashMap[Hypertension] =
                selectedID as String
            showViews(
                binding.ncdDiabetesHypertension.groupYearOfDiagnosis2,
                selectedID,
                binding.ncdDiabetesHypertension.tvYearOfDiagnosisErrorHtn,
                binding.ncdDiabetesHypertension.etYearOfDiagnosisHtn
            )
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

    private fun showSpinnerView(selectedValue: String) {
        val isKnownPatient = selectedValue.equals(Known_patient, ignoreCase = true)
        val isNewlyDiagnosed = selectedValue.equals(Newly_Diagnosed, ignoreCase = true)
        with(binding.ncdDiabetesHypertension) {
            groupDiabetesSpinner.isVisible = isKnownPatient || isNewlyDiagnosed
            if (!isKnownPatient && !isNewlyDiagnosed) {
                etYearOfDiagnosis.setText("")
                viewModel.value = null
                tvDiabetesControlledSpinner.post {
                    tvDiabetesControlledSpinner.setSelection(0, false)
                }
            }
            tvDiabetesControlledError.gone()
        }
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

    fun validateInput(): Boolean {
        val isDiabetesValid = viewModel.resultDiabetesHashMap.isNotEmpty()
        val isHypertensionValid = viewModel.resultHypertensionHashMap.isNotEmpty()

        binding.ncdDiabetesHypertension.tvDiabetesError.setVisible(!isDiabetesValid)
        binding.ncdDiabetesHypertension.tvHypertensionError.setVisible(!isHypertensionValid)

        if (!isDiabetesValid || !isHypertensionValid) {
            return false
        }

        val diabetesStatus = viewModel.resultDiabetesHashMap[Diabetes] as? String
        val hypertensionStatus = viewModel.resultHypertensionHashMap[Hypertension] as? String
        val diabetesType = viewModel.value

        if (diabetesStatus == Known_patient) {
            if (binding.ncdDiabetesHypertension.etYearOfDiagnosis.isVisible()){
                isValidDiagnosis()
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
            Known_patient -> {
                if (!isValidDiagnosisTwo()) {
                    binding.ncdDiabetesHypertension.tvYearOfDiagnosisErrorHtn.visible()
                    return false
                }
                binding.ncdDiabetesHypertension.tvYearOfDiagnosisErrorHtn.gone()
            }
        }

        return true
    }

    private fun isValidDiagnosis(): Boolean {
        return MotherNeonateUtil.isValidInput(
            binding.ncdDiabetesHypertension.etYearOfDiagnosis.text.toString(),
            binding.ncdDiabetesHypertension.etYearOfDiagnosis,
            binding.ncdDiabetesHypertension.tvYearOfDiagnosisError,
            1920.0..getCurrentYearAsDouble(),
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
            1900.0..getCurrentYearAsDouble(),
            R.string.error_label,
            true,
            requireContext()
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnConfirm.id -> {
                if (validateInput()) {
                    // Do the API call
                    val request = NCDPatientStatusRequest(
                        id = viewModel.patientStatusId,
                        provenance = ProvanceDto(),
                        patientReference = getPatientReference(),
                        memberReference = getMemberReference(),
                        patientVisitId = getVisitId(),
                        ncdPatientStatus = NcdPatientStatus(
                            id = viewModel.id,
                            diabetesStatus = viewModel.resultDiabetesHashMap[Diabetes] as? String,
                            hypertensionStatus = viewModel.resultHypertensionHashMap[Hypertension] as? String,
                            hypertensionYearOfDiagnosis = viewModel.yearForHypertension.takeIf { !it.isNullOrBlank() },
                            diabetesYearOfDiagnosis = viewModel.yearForDiabetes.takeIf { !it.isNullOrBlank() },
                            diabetesControlledType = null,
                            diabetesDiagnosis = viewModel.value
                        )
                    )
                    viewModel.createNCDPatientStatus(request)
                }
            }

            binding.btnCancel.id, binding.ivClose.id -> {
                listener?.closePage()
            }
        }
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
        binding.loaderImage.apply {
            loadAsGif(R.drawable.loader_spice)
        }
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
        binding.loaderImage.apply {
            resetImageView()
        }
    }

}
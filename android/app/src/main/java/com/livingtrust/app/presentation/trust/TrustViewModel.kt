package com.livingtrust.app.presentation.trust

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.domain.usecase.trust.CreateTrustUseCase
import com.livingtrust.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrustWizardState(
    val step: Int = 1,
    val totalSteps: Int = 4,
    val trustName: String = "",
    val grantor: String = "",
    val trustee: String = "",
    val successorTrustee: String = "",
    val beneficiaries: List<String> = emptyList(),
    val assets: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class TrustViewModel @Inject constructor(
    private val createTrustUseCase: CreateTrustUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TrustWizardState())
    val state: StateFlow<TrustWizardState> = _state.asStateFlow()

    fun updateTrustName(value: String) { _state.value = _state.value.copy(trustName = value) }
    fun updateGrantor(value: String) { _state.value = _state.value.copy(grantor = value) }
    fun updateTrustee(value: String) { _state.value = _state.value.copy(trustee = value) }
    fun updateSuccessorTrustee(value: String) { _state.value = _state.value.copy(successorTrustee = value) }

    fun addBeneficiary(name: String) {
        if (name.isBlank()) return
        _state.value = _state.value.copy(
            beneficiaries = _state.value.beneficiaries + name
        )
    }

    fun removeBeneficiary(index: Int) {
        _state.value = _state.value.copy(
            beneficiaries = _state.value.beneficiaries.toMutableList().also { it.removeAt(index) }
        )
    }

    fun addAsset(asset: String) {
        if (asset.isBlank()) return
        _state.value = _state.value.copy(assets = _state.value.assets + asset)
    }

    fun removeAsset(index: Int) {
        _state.value = _state.value.copy(
            assets = _state.value.assets.toMutableList().also { it.removeAt(index) }
        )
    }

    fun nextStep() {
        val current = _state.value.step
        if (current < _state.value.totalSteps) {
            _state.value = _state.value.copy(step = current + 1)
        }
    }

    fun previousStep() {
        val current = _state.value.step
        if (current > 1) {
            _state.value = _state.value.copy(step = current - 1)
        }
    }

    fun submitTrust() {
        val s = _state.value
        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            val trust = Trust(
                trustName = s.trustName,
                grantor = s.grantor,
                trustee = s.trustee,
                successorTrustee = s.successorTrustee,
                beneficiaries = s.beneficiaries,
                assets = s.assets
            )
            when (val result = createTrustUseCase(trust)) {
                is Resource.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    isComplete = true
                )
                is Resource.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message
                )
                Resource.Loading -> Unit
            }
        }
    }
}

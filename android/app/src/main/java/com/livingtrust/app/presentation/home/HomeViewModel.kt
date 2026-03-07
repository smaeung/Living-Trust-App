package com.livingtrust.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.domain.repository.AuthRepository
import com.livingtrust.app.domain.usecase.trust.GetTrustsUseCase
import com.livingtrust.app.domain.repository.TrustRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val trusts: List<Trust> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrustsUseCase: GetTrustsUseCase,
    private val trustRepository: TrustRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        observeTrusts()
        refreshTrusts()
    }

    private fun observeTrusts() {
        viewModelScope.launch {
            getTrustsUseCase().collect { trusts ->
                _state.value = _state.value.copy(trusts = trusts)
            }
        }
    }

    fun refreshTrusts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            trustRepository.refreshTrusts()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}

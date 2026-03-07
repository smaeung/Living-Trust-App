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

/**
 * UI state for the Home screen.
 *
 * WHY `trusts: List<Trust> = emptyList()`?
 * - Defaulting to an empty list means HomeScreen can render immediately without
 *   a null check. An empty list renders an empty state UI (e.g., "No trusts yet").
 *
 * WHY `isLoading` and `error` as separate fields?
 * - These can coexist with data: you can show cached trusts (trusts list populated)
 *   while loading fresh data from the network (isLoading = true).
 *   This is the offline-first UX: stale data is better than a blank screen.
 */
data class HomeState(
    val trusts: List<Trust> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Home screen — the main dashboard after login.
 *
 * WHY inject both GetTrustsUseCase AND TrustRepository directly?
 * - GetTrustsUseCase wraps the reactive Flow query (read from local DB).
 * - TrustRepository.refreshTrusts() is a one-shot network sync.
 * - Ideally, refreshTrusts() would also have its own Use Case. For v1, calling the
 *   repository directly here is acceptable since there's no extra business logic to test.
 *
 * WHY inject AuthRepository directly (not a Use Case)?
 * - Logout is a simple operation (clear token) with no validation needed.
 *   A LogoutUseCase would just delegate to authRepository.logout() with no added value.
 *
 * init block: WHY call both observeTrusts() AND refreshTrusts()?
 * - observeTrusts(): immediately subscribes to Room's Flow — shows cached trusts instantly.
 * - refreshTrusts(): fires a network call to sync fresh data from the server.
 * This combination gives users immediate display (from cache) + up-to-date data (from network).
 * Without observeTrusts(), the screen stays blank until the network responds.
 * Without refreshTrusts(), the screen shows stale cached data forever.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrustsUseCase: GetTrustsUseCase,
    private val trustRepository: TrustRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        observeTrusts()   // step 1: show cached data immediately
        refreshTrusts()   // step 2: sync fresh data from network in background
    }

    /**
     * Subscribes to the Room-backed Flow of trusts.
     *
     * WHY private?
     * - This is an internal implementation detail. External callers (the screen)
     *   should only trigger refreshTrusts() or logout() — not the observation setup.
     *
     * WHY `collect { trusts -> ... }`?
     * - collect() is a terminal operator that starts consuming the Flow.
     * - Every time Room emits a new list (after insert/update/delete),
     *   this lambda runs and updates the state. The UI automatically recomposes.
     * - This coroutine runs for the lifetime of the ViewModel (viewModelScope),
     *   so the subscription stays active as long as the Home screen is in the back stack.
     */
    private fun observeTrusts() {
        viewModelScope.launch {
            getTrustsUseCase().collect { trusts ->
                _state.value = _state.value.copy(trusts = trusts)
            }
        }
    }

    /**
     * Triggers a network sync: fetches trusts from the API and writes them to Room.
     *
     * WHY public?
     * - The HomeScreen's pull-to-refresh gesture calls this to manually trigger a sync.
     *
     * WHY not update trusts directly from the network response here?
     * - The offline-first pattern: refreshTrusts() writes to Room, and Room's Flow
     *   (in observeTrusts()) picks up the change and emits the new list.
     *   There's only one source of truth — the local database.
     *   The ViewModel never holds the network response directly.
     */
    fun refreshTrusts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            trustRepository.refreshTrusts()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    /**
     * Logs the user out and invokes the navigation callback.
     *
     * WHY pass a callback lambda instead of returning a navigation event?
     * - Navigation (calling navController.navigate()) must happen on the Compose side.
     *   The ViewModel shouldn't hold a reference to NavController — that would create
     *   a memory leak (NavController is tied to the Activity lifecycle).
     * - The callback pattern lets the screen decide what to do after logout
     *   (navigate to Login, clear back stack, etc.) while the ViewModel handles
     *   the actual auth cleanup.
     */
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()  // clears the JWT token from DataStore
            onLoggedOut()            // navigate to Login screen (called from HomeScreen)
        }
    }
}

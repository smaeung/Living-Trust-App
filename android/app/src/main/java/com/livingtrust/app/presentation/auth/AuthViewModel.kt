package com.livingtrust.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livingtrust.app.domain.model.User
import com.livingtrust.app.domain.usecase.auth.LoginUseCase
import com.livingtrust.app.domain.usecase.auth.RegisterUseCase
import com.livingtrust.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the complete UI state for both the Login and Register screens.
 *
 * WHY a single state class for two screens?
 * - LoginScreen and RegisterScreen share the same ViewModel (AuthViewModel), so they
 *   share the same state class. This avoids duplication since the states are nearly
 *   identical (loading indicator, error message, logged-in flag).
 *
 * WHY `data class`?
 * - data class generates equals() which StateFlow uses to detect changes.
 *   If state were a regular class, `copy()` would always emit a new event even if
 *   the content didn't change, causing unnecessary recomposition in Compose.
 *
 * WHY `user: User? = null`?
 * - The user object is only available after a successful login/register.
 *   Null means "not yet authenticated". A non-null value means auth succeeded.
 *
 * WHY both `user` and `isLoggedIn`? Isn't `user != null` enough?
 * - Having an explicit `isLoggedIn` flag makes intent clear.
 *   LaunchedEffect in LoginScreen watches `isLoggedIn` — this is more readable
 *   than watching `user != null`, especially if the user model could theoretically
 *   be populated for other reasons in the future.
 */
data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,            // populated after successful auth
    val error: String? = null,         // error message to show in the UI
    val isLoggedIn: Boolean = false    // triggers navigation away from the auth screen
)

/**
 * ViewModel for authentication screens (Login and Register).
 *
 * WHY ViewModel?
 * - ViewModels survive screen rotations. Without a ViewModel, rotating the phone
 *   would restart the login process and lose any typed text or error messages.
 * - ViewModel lives longer than Composables but shorter than the Application.
 *   It's destroyed when the user navigates away permanently (back stack cleared).
 *
 * WHY @HiltViewModel + @Inject constructor?
 * - @HiltViewModel tells Hilt to create this ViewModel using the Hilt-aware factory.
 *   Without it, Hilt can't inject constructor parameters into ViewModels.
 * - @Inject marks the constructor so Hilt knows how to build it (with Use Cases).
 * - This lets Compose call `hiltViewModel()` in the screen composable to get
 *   an already-constructed ViewModel with all dependencies provided.
 *
 * WHY inject Use Cases instead of the Repository directly?
 * - Use Cases contain business validation. If the ViewModel called the repository
 *   directly, it would have to duplicate the validation logic.
 * - The ViewModel's job is to: hold state, trigger operations, react to results.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    // MutableStateFlow holds the current state and allows updates from this class only.
    // WHY private? External code (UI) should never mutate state directly.
    private val _state = MutableStateFlow(AuthState())

    // Expose as read-only StateFlow to the UI.
    // WHY asStateFlow()? It wraps the mutable flow in a read-only interface,
    // enforcing the one-direction data flow: ViewModel → UI.
    val state: StateFlow<AuthState> = _state.asStateFlow()

    /**
     * Initiates the login process.
     *
     * WHY viewModelScope.launch?
     * - loginUseCase is a suspend function (async). It must run in a coroutine.
     * - viewModelScope is automatically cancelled when the ViewModel is destroyed,
     *   preventing memory leaks if the user navigates away during the API call.
     *
     * WHY set isLoading = true before the call and reset it after?
     * - The UI reads `isLoading` to show a spinner and disable the button.
     *   Disabling the button prevents duplicate submissions if the user taps again.
     *
     * WHY `_state.value = _state.value.copy(...)`?
     * - copy() is a data class feature that creates a new object with only the
     *   specified fields changed. This is immutable state mutation — the Compose
     *   way of updating UI state safely.
     *
     * WHY on Success: replace ALL of _state with a fresh AuthState (not copy)?
     * - On successful login, we want a completely clean state: no loading, no error.
     *   Using copy() would keep any previous error message around.
     *   Starting fresh (AuthState(user=..., isLoggedIn=true)) guarantees a clean slate.
     *
     * WHY `Resource.Loading -> Unit`?
     * - The Use Case currently never emits Loading (it's synchronous validation + suspend call).
     *   Handling it as a no-op keeps the `when` exhaustive (Kotlin requires all cases).
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = loginUseCase(email, password)) {
                is Resource.Success -> _state.value = AuthState(
                    user = result.data,
                    isLoggedIn = true
                )
                is Resource.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message
                )
                Resource.Loading -> Unit // no-op: Use Case doesn't emit Loading
            }
        }
    }

    /** Same pattern as login() but calls RegisterUseCase. See login() comments for WHY. */
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = registerUseCase(name, email, password)) {
                is Resource.Success -> _state.value = AuthState(
                    user = result.data,
                    isLoggedIn = true
                )
                is Resource.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message
                )
                Resource.Loading -> Unit
            }
        }
    }

    /**
     * Clears the current error message from state.
     *
     * WHY is this needed?
     * - When the user corrects their input and retries, the old error message
     *   should disappear immediately (not linger until the new result arrives).
     * - The UI can call clearError() on text field change events to dismiss errors
     *   as soon as the user starts typing.
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

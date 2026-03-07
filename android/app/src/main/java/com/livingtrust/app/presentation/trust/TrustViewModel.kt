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

/**
 * UI state for the multi-step Trust Creation Wizard.
 *
 * WHY store ALL wizard fields in one state class?
 * - The wizard has 4 steps, but the user can go back and forth between them.
 *   Storing all fields in one place means any step can read any field.
 * - If each step had its own state, passing data between steps would require
 *   navigation arguments or a shared parent ViewModel — both more complex.
 *
 * WHY `step: Int = 1` and `totalSteps: Int = 4`?
 * - Keeping totalSteps in state (rather than hardcoding 4 in every UI check) means
 *   adding a 5th wizard step only requires changing `totalSteps = 5` here.
 *
 * WHY `isComplete: Boolean = false`?
 * - When submitTrust() succeeds, the ViewModel sets isComplete = true.
 *   The TrustWizardScreen observes this flag and calls its onTrustCreated callback,
 *   which triggers navigation back to the Home screen.
 *   This keeps navigation logic in the UI layer where it belongs.
 */
data class TrustWizardState(
    val step: Int = 1,
    val totalSteps: Int = 4,
    // Step 1: Basic Info
    val trustName: String = "",
    val grantor: String = "",
    // Step 2: Trustee Info
    val trustee: String = "",
    val successorTrustee: String = "",
    // Step 3: Beneficiaries (multiple people)
    val beneficiaries: List<String> = emptyList(),
    // Step 4: Assets (multiple items)
    val assets: List<String> = emptyList(),
    // Submission state
    val isLoading: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false    // true when trust is successfully saved
)

/**
 * ViewModel for the Trust Creation Wizard screen.
 *
 * WHY a ViewModel for a form wizard?
 * - Screen rotation destroys and recreates Composables. Without a ViewModel, the user
 *   would lose all their typed-in trust information on every rotation.
 * - The ViewModel holds all form state in memory, surviving configuration changes.
 *
 * WHY separate update functions (updateTrustName, updateGrantor, etc.)?
 * - Each function updates exactly one field via copy().
 * - Alternative approaches:
 *   1. One generic updateField(field, value) — harder to type-check and read.
 *   2. Let the UI hold local state (remember{}) — doesn't survive rotation.
 * - Explicit per-field functions are verbose but clear and testable.
 *
 * WHY is beneficiaries/assets a List instead of a single text field?
 * - A living trust typically has multiple beneficiaries (multiple children, for example)
 *   and multiple assets (house, car, bank accounts).
 *   A List lets the UI render each item as a chip/row that can be individually deleted.
 */
@HiltViewModel
class TrustViewModel @Inject constructor(
    private val createTrustUseCase: CreateTrustUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TrustWizardState())
    val state: StateFlow<TrustWizardState> = _state.asStateFlow()

    // ---- Field updaters (Step 1 & 2) ----
    // Each function uses copy() to produce a new immutable state object with one field changed.
    // WHY not use var fields? Mutable state in ViewModels must go through StateFlow so Compose
    // can observe changes. Regular var fields don't notify observers.
    fun updateTrustName(value: String) { _state.value = _state.value.copy(trustName = value) }
    fun updateGrantor(value: String) { _state.value = _state.value.copy(grantor = value) }
    fun updateTrustee(value: String) { _state.value = _state.value.copy(trustee = value) }
    fun updateSuccessorTrustee(value: String) { _state.value = _state.value.copy(successorTrustee = value) }

    // ---- Beneficiary management (Step 3) ----

    /**
     * Adds a beneficiary name to the list.
     *
     * WHY guard against blank? The UI might allow the user to tap "Add" with an empty field.
     * Early return prevents adding empty strings to the list.
     *
     * WHY `_state.value.beneficiaries + name`?
     * - The + operator on a List creates a new list (beneficiaries is immutable).
     *   This is required because we use immutable state — we never mutate lists in-place.
     */
    fun addBeneficiary(name: String) {
        if (name.isBlank()) return
        _state.value = _state.value.copy(
            beneficiaries = _state.value.beneficiaries + name
        )
    }

    /**
     * Removes a beneficiary by its position in the list.
     *
     * WHY by index instead of by name?
     * - A user could add two beneficiaries with the same name (e.g., "John Smith Sr." vs
     *   "John Smith Jr." typed identically). Index removal is always unambiguous.
     *
     * WHY `toMutableList().also { it.removeAt(index) }`?
     * - The beneficiaries list is immutable (List<String>).
     *   toMutableList() creates a mutable copy, removeAt() removes the item,
     *   and the result is assigned back via copy(). This preserves immutability in state.
     */
    fun removeBeneficiary(index: Int) {
        _state.value = _state.value.copy(
            beneficiaries = _state.value.beneficiaries.toMutableList().also { it.removeAt(index) }
        )
    }

    // ---- Asset management (Step 4) ----
    // Same pattern as beneficiary management — see comments above.

    fun addAsset(asset: String) {
        if (asset.isBlank()) return
        _state.value = _state.value.copy(assets = _state.value.assets + asset)
    }

    fun removeAsset(index: Int) {
        _state.value = _state.value.copy(
            assets = _state.value.assets.toMutableList().also { it.removeAt(index) }
        )
    }

    // ---- Step navigation ----

    /**
     * Advances to the next wizard step (capped at totalSteps).
     *
     * WHY guard `if (current < totalSteps)`?
     * - Prevents going past the last step. The "Next" button on the last step
     *   should call submitTrust(), not nextStep().
     */
    fun nextStep() {
        val current = _state.value.step
        if (current < _state.value.totalSteps) {
            _state.value = _state.value.copy(step = current + 1)
        }
    }

    /**
     * Goes back to the previous wizard step (capped at 1).
     *
     * WHY guard `if (current > 1)`?
     * - Prevents going below step 1. On step 1, "Back" should call the navigation
     *   back callback (popBackStack), not this function.
     */
    fun previousStep() {
        val current = _state.value.step
        if (current > 1) {
            _state.value = _state.value.copy(step = current - 1)
        }
    }

    // ---- Final submission ----

    /**
     * Assembles the Trust domain model from current state and submits it.
     *
     * WHY snapshot `val s = _state.value` at the start?
     * - Inside a coroutine, _state.value could theoretically change while we're building
     *   the Trust object (e.g., user typed something). Snapshotting ensures all fields
     *   of the submitted trust come from the same consistent state.
     *
     * WHY build a Trust object here instead of passing individual fields to the use case?
     * - Trust is the domain model. The use case expects a Trust, not raw strings.
     *   The ViewModel is responsible for assembling domain objects from UI state.
     *
     * WHY `isComplete = true` on success instead of navigating directly?
     * - The ViewModel cannot and should not hold a NavController reference.
     *   Instead, TrustWizardScreen observes `isComplete` and calls `onTrustCreated()`
     *   to navigate back. This keeps navigation in the Compose layer.
     */
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
                // id defaults to "" (assigned by server), status defaults to "draft"
            )
            when (val result = createTrustUseCase(trust)) {
                is Resource.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    isComplete = true    // triggers navigation in TrustWizardScreen
                )
                is Resource.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message   // shows error on the final review step
                )
                Resource.Loading -> Unit  // use case never emits Loading; required for exhaustive when
            }
        }
    }
}

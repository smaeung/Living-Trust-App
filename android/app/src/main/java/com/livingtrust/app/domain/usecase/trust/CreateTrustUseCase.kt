package com.livingtrust.app.domain.usecase.trust

import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.domain.repository.TrustRepository
import com.livingtrust.app.util.Resource
import javax.inject.Inject

/**
 * Use case that validates and submits a new Living Trust to the backend.
 *
 * WHY validation lives here and not in TrustViewModel?
 * - TrustViewModel collects form input across multiple wizard steps.
 *   Without a Use Case, the ViewModel would contain both UI state management AND
 *   business rules — a violation of the Single Responsibility Principle.
 * - The Use Case is independently testable: you can run CreateTrustUseCaseTest
 *   without an Android device, emulator, or Compose UI.
 *
 * WHY validate these 4 specific fields?
 * - Legal requirement: a valid living trust MUST identify:
 *   1. The trust name (document identifier)
 *   2. The grantor (the person creating/funding the trust)
 *   3. The trustee (the person managing the trust)
 *   4. A successor trustee (required in case the trustee can no longer serve)
 * - Beneficiaries and assets are optional in many trust documents (they can be added later).
 *   We don't validate them here to avoid blocking users who are still gathering information.
 *
 * WHY does invoke() take a Trust object instead of individual strings?
 * - The trust wizard collects many fields over 4 steps. Passing them all as individual
 *   parameters would make the function signature very long and fragile.
 *   Grouping them in a Trust domain model is cleaner and avoids parameter ordering mistakes.
 *
 * WHY return Resource<Trust>?
 * - On success, the backend assigns an ID to the new trust and returns it.
 *   The caller (TrustViewModel) uses this to update the local state and navigate away.
 * - On failure (validation or network), Resource.Error carries the error message for display.
 */
class CreateTrustUseCase @Inject constructor(
    private val trustRepository: TrustRepository
) {
    /**
     * Validates required fields then creates the trust via the repository.
     *
     * Returns Resource.Error immediately on the first invalid field found.
     * Returns Resource.Success(trust) with the server-assigned trust (including ID) on success.
     */
    suspend operator fun invoke(trust: Trust): Resource<Trust> {
        if (trust.trustName.isBlank()) return Resource.Error("Trust name is required")
        if (trust.grantor.isBlank()) return Resource.Error("Grantor name is required")
        if (trust.trustee.isBlank()) return Resource.Error("Trustee is required")
        if (trust.successorTrustee.isBlank()) return Resource.Error("Successor trustee is required")
        return trustRepository.createTrust(trust)
    }
}

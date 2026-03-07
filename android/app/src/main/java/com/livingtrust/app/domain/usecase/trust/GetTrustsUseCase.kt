package com.livingtrust.app.domain.usecase.trust

import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.domain.repository.TrustRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case that retrieves the list of trusts as a reactive stream.
 *
 * WHY does this Use Case exist if it just delegates to the repository?
 * - It might seem redundant to have a Use Case that only calls one repository method.
 *   However, Use Cases serve as the boundary between presentation and data layers.
 *   Even simple ones provide:
 *   1. A stable contract: the ViewModel depends on GetTrustsUseCase, not TrustRepository.
 *      If the repository signature changes, only this file needs updating.
 *   2. A place for future logic: e.g., filter only active trusts, sort by date,
 *      or combine trusts from multiple sources.
 *   3. Testability: a test can mock GetTrustsUseCase without touching the repository.
 *
 * WHY return Flow<List<Trust>> and not suspend?
 * - Flow is a continuous stream of values. When the database changes (e.g., a new trust
 *   is added), Room emits a new List<Trust> to all collectors automatically.
 * - The HomeViewModel subscribes once; it never needs to re-call this function.
 *   This is the "observe, don't poll" pattern.
 *
 * WHY is invoke() NOT suspend?
 * - Flow collection is lazy and async — no actual work happens when `invoke()` is called.
 *   Work starts when the caller calls `.collect {}` on the returned Flow.
 * - suspend is only needed for functions that perform blocking async work themselves.
 */
class GetTrustsUseCase @Inject constructor(
    private val trustRepository: TrustRepository
) {
    /**
     * Returns a Flow that emits the current list of trusts and re-emits whenever it changes.
     * Backed by Room database queries, so it reflects the offline-first local cache.
     */
    operator fun invoke(): Flow<List<Trust>> = trustRepository.getTrusts()
}

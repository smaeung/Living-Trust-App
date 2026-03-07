package com.livingtrust.app.domain.usecase.trust

import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.domain.repository.TrustRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrustsUseCase @Inject constructor(
    private val trustRepository: TrustRepository
) {
    operator fun invoke(): Flow<List<Trust>> = trustRepository.getTrusts()
}

package com.livingtrust.app.domain.usecase.trust

import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.domain.repository.TrustRepository
import com.livingtrust.app.util.Resource
import javax.inject.Inject

class CreateTrustUseCase @Inject constructor(
    private val trustRepository: TrustRepository
) {
    suspend operator fun invoke(trust: Trust): Resource<Trust> {
        if (trust.trustName.isBlank()) return Resource.Error("Trust name is required")
        if (trust.grantor.isBlank()) return Resource.Error("Grantor name is required")
        if (trust.trustee.isBlank()) return Resource.Error("Trustee is required")
        if (trust.successorTrustee.isBlank()) return Resource.Error("Successor trustee is required")
        return trustRepository.createTrust(trust)
    }
}

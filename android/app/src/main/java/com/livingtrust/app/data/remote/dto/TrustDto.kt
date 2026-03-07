package com.livingtrust.app.data.remote.dto

data class TrustDto(
    val id: String,
    val trustName: String,
    val grantor: String,
    val trustee: String,
    val successorTrustee: String,
    val beneficiaries: List<String>,
    val assets: List<String>,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

data class CreateTrustRequest(
    val trustName: String,
    val grantor: String,
    val trustee: String,
    val successorTrustee: String,
    val beneficiaries: List<String>,
    val assets: List<String>
)

data class TrustListResponse(
    val trusts: List<TrustDto>
)

data class TrustResponse(
    val message: String,
    val trust: TrustDto
)

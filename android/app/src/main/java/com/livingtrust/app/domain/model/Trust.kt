package com.livingtrust.app.domain.model

data class Trust(
    val id: String = "",
    val trustName: String,
    val grantor: String,
    val trustee: String,
    val successorTrustee: String,
    val beneficiaries: List<String>,
    val assets: List<String>,
    val status: String = "draft"
)

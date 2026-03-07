package com.livingtrust.app.data.remote.dto

/**
 * Trust DTOs — Data Transfer Objects for Trust-related API calls.
 *
 * Flow of data for creating a trust:
 *   1. User fills in the 4-step wizard → TrustWizardState (ViewModel)
 *   2. ViewModel calls CreateTrustUseCase with a domain Trust model
 *   3. Use case validates fields, then calls TrustRepository.createTrust()
 *   4. Repository converts Trust → CreateTrustRequest (this DTO)
 *   5. Retrofit serializes CreateTrustRequest to JSON and sends to the server
 *   6. Server responds with TrustResponse containing the saved TrustDto
 *   7. Repository converts TrustDto → TrustEntity (saved in Room) and Trust (returned to UI)
 *
 * Notice that beneficiaries and assets are List<String> here (proper Kotlin types)
 * but stored as JSON strings in TrustEntity. The conversion happens in the repository.
 */

/**
 * Represents one Trust document as returned by the server.
 * Field names match the Express backend's JSON keys exactly.
 *
 * Note: createdAt and updatedAt are ISO date strings from the server (e.g. "2024-01-15T...")
 * but we convert them to Long (milliseconds) when saving to Room for easier sorting.
 */
data class TrustDto(
    val id: String,
    val trustName: String,
    val grantor: String,
    val trustee: String,
    val successorTrustee: String,
    /** Proper list type from JSON array: ["Alice Smith", "Bob Smith"] */
    val beneficiaries: List<String>,
    /** Proper list type from JSON array: ["House at 123 Main St"] */
    val assets: List<String>,
    /** Current lifecycle state: "draft" | "review" | "complete" */
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * The request body sent to POST /api/trusts to create a new trust.
 * Does NOT include id, status, createdAt, or updatedAt — the server generates those.
 */
data class CreateTrustRequest(
    val trustName: String,
    val grantor: String,
    val trustee: String,
    val successorTrustee: String,
    val beneficiaries: List<String>,
    val assets: List<String>
)

/**
 * Response from GET /api/trusts — a list of all trusts for the authenticated user.
 * Wraps the list in an object because the server returns { "trusts": [...] } not just [...].
 */
data class TrustListResponse(
    val trusts: List<TrustDto>
)

/**
 * Response from POST /api/trusts (create) and PUT /api/trusts/:id (update).
 * Contains both a confirmation message and the full saved trust data.
 */
data class TrustResponse(
    val message: String,
    val trust: TrustDto
)

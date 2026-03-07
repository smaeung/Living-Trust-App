package com.livingtrust.app.domain.model

/**
 * Domain model representing a Living Trust document.
 *
 * WHY is this a domain model (not a DTO or Entity)?
 * - Pure Kotlin data class with no framework annotations.
 *   This makes it usable in unit tests without Android or Room dependencies.
 * - It is the "source of truth" representation that all layers agree on.
 *   DTOs (network shapes) and Entities (database shapes) are converted TO this model.
 *
 * WHY does `id` default to empty string?
 * - When creating a new trust, the ID is assigned by the backend after saving.
 *   An empty string signals "this trust has not been saved yet".
 *   Alternative: use a nullable `id: String?`, but empty string is simpler to handle
 *   in UI (no null checks needed in text displays).
 *
 * WHY are `beneficiaries` and `assets` List<String>?
 * - A living trust can have multiple beneficiaries (heirs) and assets (property, accounts).
 *   Using a list mirrors the real-world concept and is easy to display in a LazyColumn.
 * - In the database (Room/SQLite), lists can't be stored directly. They are serialized
 *   to JSON strings (e.g., `["Alice","Bob"]`) via Gson in TrustRepositoryImpl.
 *
 * WHY does `status` default to "draft"?
 * - A trust starts as a draft while the user fills out the wizard.
 *   Once submitted and approved, the server can update the status (e.g., "active", "finalized").
 *   Defaulting to "draft" means new Trust objects are always in a valid initial state.
 *
 * Legal roles explained (for non-lawyers):
 * - grantor: the person who creates and funds the trust (usually the owner of the assets)
 * - trustee: manages the trust assets on behalf of the grantor (often the grantor themselves)
 * - successorTrustee: takes over management if the trustee becomes incapacitated or passes away
 * - beneficiaries: the people who receive the trust assets when conditions are met
 */
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

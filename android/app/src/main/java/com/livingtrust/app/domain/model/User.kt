package com.livingtrust.app.domain.model

/**
 * Domain model representing an authenticated user.
 *
 * WHY is this a domain model (not a DTO or Entity)?
 * - This class lives in the domain layer, which is the core of Clean Architecture.
 *   It is pure Kotlin — no Android imports, no Retrofit annotations, no Room annotations.
 *   This makes it completely independent and portable.
 *
 * WHY does it have only 3 fields (not the full user object from the server)?
 * - The domain model contains only what the UI actually needs.
 * - Extra server fields (createdAt, passwordHash, roles, etc.) are stripped out
 *   when mapping from UserDto → User in the repository. The UI never sees them.
 *
 * WHY `data class`?
 * - Kotlin's data class automatically generates equals(), hashCode(), toString(), and copy().
 * - equals() is important for StateFlow: Compose only recomposes when the state value
 *   actually changes. If User were a regular class, every assignment would look "new"
 *   even if the content was identical.
 */
data class User(
    val id: String,
    val email: String,
    val name: String
)

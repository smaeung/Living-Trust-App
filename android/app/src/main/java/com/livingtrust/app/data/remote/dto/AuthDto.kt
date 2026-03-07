package com.livingtrust.app.data.remote.dto

/**
 * Auth DTOs — Data Transfer Objects for authentication API calls.
 *
 * WHY DTOs (Data Transfer Objects)?
 * DTOs are simple data containers that mirror the exact JSON structure the server
 * sends or expects. They exist only in the `data` layer and are NEVER passed to
 * the UI. The repository converts them to domain models (User, etc.) before
 * returning them to the rest of the app.
 *
 * WHY keep DTOs separate from domain models?
 * If the server changes its JSON field names (e.g. renames "name" to "fullName"),
 * you only update the DTO — not the ViewModel, not the UI. The domain model stays
 * stable and the conversion happens in one place (the repository).
 *
 * How JSON → Kotlin mapping works with Gson:
 * Retrofit uses Gson to deserialize JSON responses into these data classes.
 * The field name in the data class must match the JSON key exactly (unless
 * you use @SerializedName("different_name") to override it).
 *
 * Example server response for login:
 * {
 *   "message": "Login successful",
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "user": { "id": "123", "email": "john@example.com", "name": "John" }
 * }
 * → This maps directly to AuthResponse containing a UserDto.
 */

/**
 * Sent to POST /api/auth/login.
 * Gson serializes this to: {"email":"...","password":"..."}
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Sent to POST /api/auth/register.
 * Includes the user's display name in addition to credentials.
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

/**
 * Received from both /login and /register on success.
 * The token is immediately saved via TokenManager so all future API calls
 * are automatically authenticated.
 */
data class AuthResponse(
    /** Human-readable confirmation message from the server. */
    val message: String,
    /** JWT token string. Save this — it is sent with every future request. */
    val token: String,
    /** Basic user info returned after authentication. */
    val user: UserDto
)

/**
 * Basic user data returned by the server.
 * Intentionally minimal — we do not store the password here (the server
 * never sends it back for security reasons).
 */
data class UserDto(
    val id: String,
    val email: String,
    val name: String
)

/**
 * Received from GET /api/auth/me — returns the current user's profile
 * based on the JWT token in the Authorization header.
 */
data class MeResponse(
    val user: UserDto
)

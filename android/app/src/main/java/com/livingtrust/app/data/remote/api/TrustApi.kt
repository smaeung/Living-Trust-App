package com.livingtrust.app.data.remote.api

import com.livingtrust.app.data.remote.dto.*
import retrofit2.http.*

/**
 * TrustApi — Retrofit interface for Trust CRUD (Create, Read, Update, Delete) endpoints.
 *
 * This maps directly to the Express backend routes in backend/src/routes/trustRoutes.ts.
 * Every request is automatically authenticated by the OkHttp interceptor in NetworkModule —
 * the JWT token is added to the "Authorization: Bearer ..." header on every call.
 *
 * Retrofit annotation reference:
 *   @GET    → HTTP GET request (fetch data, no request body)
 *   @POST   → HTTP POST request (create new resource, has request body)
 *   @PUT    → HTTP PUT request (replace existing resource, has request body)
 *   @DELETE → HTTP DELETE request (remove resource, no request body)
 *   @Path("id") → replaces {id} placeholder in the URL with the actual id value
 *   @Body   → serializes the Kotlin object to JSON as the request body
 */
interface TrustApi {

    /**
     * Fetches all trusts for the currently authenticated user.
     * Maps to: GET /api/trusts
     *
     * Called by TrustRepositoryImpl.refreshTrusts() to sync the local Room
     * database with the server whenever the Home screen loads.
     */
    @GET("api/trusts")
    suspend fun getTrusts(): TrustListResponse

    /**
     * Fetches a single trust by its server-assigned ID.
     * Maps to: GET /api/trusts/{id}
     *
     * @Path("id") replaces {id} in the URL with the value of the id parameter.
     * Example: getTrust("abc123") → GET /api/trusts/abc123
     */
    @GET("api/trusts/{id}")
    suspend fun getTrust(@Path("id") id: String): TrustResponse

    /**
     * Creates a new trust on the server.
     * Maps to: POST /api/trusts
     *
     * The server assigns a unique ID and sets the initial status to "draft".
     * After creation, the repository saves the result to Room so it's available offline.
     */
    @POST("api/trusts")
    suspend fun createTrust(@Body request: CreateTrustRequest): TrustResponse

    /**
     * Replaces all fields of an existing trust.
     * Maps to: PUT /api/trusts/{id}
     *
     * PUT means "replace everything" (different from PATCH which is "partial update").
     * The caller must provide all fields, not just the changed ones.
     */
    @PUT("api/trusts/{id}")
    suspend fun updateTrust(
        @Path("id") id: String,
        @Body request: CreateTrustRequest
    ): TrustResponse

    /**
     * Permanently deletes a trust from the server.
     * Maps to: DELETE /api/trusts/{id}
     *
     * Returns a Map<String, String> because the server responds with:
     * { "message": "Trust deleted successfully" }
     * Using Map avoids creating a dedicated response DTO for a single field.
     */
    @DELETE("api/trusts/{id}")
    suspend fun deleteTrust(@Path("id") id: String): Map<String, String>
}

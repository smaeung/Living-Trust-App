package com.livingtrust.app.data.remote.api

import com.livingtrust.app.data.remote.dto.*
import retrofit2.http.*

interface TrustApi {

    @GET("api/trusts")
    suspend fun getTrusts(): TrustListResponse

    @GET("api/trusts/{id}")
    suspend fun getTrust(@Path("id") id: String): TrustResponse

    @POST("api/trusts")
    suspend fun createTrust(@Body request: CreateTrustRequest): TrustResponse

    @PUT("api/trusts/{id}")
    suspend fun updateTrust(
        @Path("id") id: String,
        @Body request: CreateTrustRequest
    ): TrustResponse

    @DELETE("api/trusts/{id}")
    suspend fun deleteTrust(@Path("id") id: String): Map<String, String>
}

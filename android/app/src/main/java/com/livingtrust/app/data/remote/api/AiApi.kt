package com.livingtrust.app.data.remote.api

import com.livingtrust.app.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApi {

    @POST("api/ai/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    @POST("api/ai/analyze")
    suspend fun analyze(@Body request: AnalyzeRequest): AnalyzeResponse
}

package com.ai.neuraforge.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import com.ai.neuraforge.network.dto.AnalyzeResponseDto
import com.ai.neuraforge.network.dto.ChatRequestDto
import com.ai.neuraforge.network.dto.ChatResponseDto

interface ApiService {
    @Multipart
    @POST("analyze")
    suspend fun analyzePdf(
        @Part file: MultipartBody.Part
    ): Response<AnalyzeResponseDto>

    @POST("chat")
    suspend fun chat(
        @Body request: ChatRequestDto
    ): Response<ChatResponseDto>


}

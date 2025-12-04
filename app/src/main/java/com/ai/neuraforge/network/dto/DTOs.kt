package com.ai.neuraforge.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TopicDto(val topic: String, val text: String)

// Matches Backend: doc_id, summary, topics, name, email, phone
@JsonClass(generateAdapter = true)
data class AnalyzeResponseDto(
    val doc_id: String,
    val summary: String,
    val topics: List<TopicDto> = emptyList(),
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null
)

// Matches Backend: doc_id, question, top_k
@JsonClass(generateAdapter = true)
data class ChatRequestDto(
    val doc_id: String,
    val question: String,
    val top_k: Int = 5
)

// Matches Backend: answer, sources
@JsonClass(generateAdapter = true)
data class ChatResponseDto(
    val answer: String,
    // We use Any here to loosely capture the source dicts, or you can create a specific SourceDto
    val sources: List<Map<String, Any>>? = null
)
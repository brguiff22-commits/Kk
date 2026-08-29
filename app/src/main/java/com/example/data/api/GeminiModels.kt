package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<ContentPayload>,
    @Json(name = "system_instruction") val systemInstruction: ContentPayload? = null,
    @Json(name = "generationConfig") val generationConfig: GenerationConfigPayload? = null
)

@JsonClass(generateAdapter = true)
data class ContentPayload(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<PartPayload>
)

@JsonClass(generateAdapter = true)
data class PartPayload(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inline_data") val inlineData: InlineDataPayload? = null
)

@JsonClass(generateAdapter = true)
data class InlineDataPayload(
    @Json(name = "mime_type") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfigPayload(
    @Json(name = "temperature") val temperature: Float = 0.7f,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 4096,
    @Json(name = "topP") val topP: Float? = 0.95f
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<CandidatePayload>? = null,
    @Json(name = "error") val error: ErrorPayload? = null
)

@JsonClass(generateAdapter = true)
data class CandidatePayload(
    @Json(name = "content") val content: ContentPayload? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class ErrorPayload(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "status") val status: String? = null
)

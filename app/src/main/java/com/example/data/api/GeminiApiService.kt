package com.example.data.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

class GeminiClient(private val context: Context) {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    /**
     * Resolves the active API key:
     * 1. Custom user key if provided and non-blank
     * 2. Key from BuildConfig.GEMINI_API_KEY
     */
    fun getEffectiveApiKey(customKey: String?): String {
        val trimmedCustom = customKey?.trim() ?: ""
        if (trimmedCustom.isNotEmpty()) {
            return trimmedCustom
        }
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    suspend fun generateResponse(
        modelName: String,
        apiKey: String,
        systemInstructionText: String,
        conversationHistory: List<ContentPayload>,
        temperature: Float = 0.7f
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = getEffectiveApiKey(apiKey)
            if (key.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Chave de API do Gemini não configurada. Por favor, adicione sua chave nas Configurações do app ou configure GEMINI_API_KEY no painel de Secrets."
                    )
                )
            }

            val request = GeminiRequest(
                contents = conversationHistory,
                systemInstruction = ContentPayload(
                    parts = listOf(PartPayload(text = systemInstructionText))
                ),
                generationConfig = GenerationConfigPayload(
                    temperature = temperature,
                    maxOutputTokens = 4096
                )
            )

            val response = api.generateContent(
                model = modelName.ifEmpty { "gemini-2.5-flash" },
                apiKey = key,
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.candidates?.firstOrNull()?.content?.parts?.joinToString(separator = "\n") {
                    it.text ?: ""
                }
                if (!text.isNullOrBlank()) {
                    Result.success(text)
                } else {
                    val finishReason = body?.candidates?.firstOrNull()?.finishReason
                    Result.failure(Exception("Resposta vazia da IA (Motivo: ${finishReason ?: "desconhecido"})."))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = try {
                    val errorObj = moshi.adapter(GeminiResponse::class.java).fromJson(errorBody ?: "")
                    errorObj?.error?.message ?: "Erro HTTP ${response.code()}: ${response.message()}"
                } catch (e: Exception) {
                    "Erro HTTP ${response.code()}: ${response.message()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro desconhecido ao conectar com a IA."))
        }
    }

    suspend fun prepareImageInlineData(uri: Uri): InlineDataPayload? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                // Scale bitmap if too large to save bandwidth and memory
                val maxDim = 1280
                val ratio = Math.min(
                    maxDim.toFloat() / bitmap.width,
                    maxDim.toFloat() / bitmap.height
                )
                val scaled = if (ratio < 1.0f) {
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * ratio).toInt(),
                        (bitmap.height * ratio).toInt(),
                        true
                    )
                } else {
                    bitmap
                }

                val baos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                val bytes = baos.toByteArray()
                val base64Str = Base64.encodeToString(bytes, Base64.NO_WRAP)
                InlineDataPayload(mimeType = "image/jpeg", data = base64Str)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun readFileContentAsText(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val text = inputStream?.bufferedReader()?.use { it.readText() }
            text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

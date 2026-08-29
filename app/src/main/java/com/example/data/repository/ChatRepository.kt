package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.api.ContentPayload
import com.example.data.api.GeminiClient
import com.example.data.api.PartPayload
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(
    private val context: Context,
    private val chatDao: ChatDao,
    private val geminiClient: GeminiClient,
    private val settingsRepository: SettingsRepository
) {
    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun createNewSession(initialTitle: String = "Nova Conversa"): ChatSessionEntity {
        val session = ChatSessionEntity(
            id = UUID.randomUUID().toString(),
            title = initialTitle,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chatDao.insertSession(session)
        return session
    }

    suspend fun getOrCreateLatestSession(): ChatSessionEntity {
        // If there's an existing session, we could return it or create new
        val session = ChatSessionEntity(
            id = UUID.randomUUID().toString(),
            title = "Nova Conversa",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chatDao.insertSession(session)
        return session
    }

    suspend fun updateSessionTitle(sessionId: String, newTitle: String) {
        val existing = chatDao.getSessionById(sessionId) ?: return
        chatDao.updateSession(existing.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSessionById(sessionId)
    }

    suspend fun deleteAllHistory() {
        chatDao.deleteAllSessions()
    }

    suspend fun sendMessage(
        sessionId: String,
        userPrompt: String,
        attachmentUri: Uri? = null,
        attachmentType: String? = null,
        attachmentName: String? = null
    ): Result<ChatMessageEntity> {
        val session = chatDao.getSessionById(sessionId)
            ?: createNewSession(userPrompt.take(30).ifEmpty { "Nova Conversa" })

        // 1. Insert user message in DB
        val userMsg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            role = "user",
            content = userPrompt,
            attachmentUri = attachmentUri?.toString(),
            attachmentType = attachmentType,
            attachmentName = attachmentName,
            timestamp = System.currentTimeMillis(),
            isError = false
        )
        chatDao.insertMessage(userMsg)

        // Update session title if default
        if (session.title == "Nova Conversa" || session.title.isEmpty()) {
            val title = if (userPrompt.isNotBlank()) userPrompt.take(28) + if (userPrompt.length > 28) "..." else "" else (attachmentName ?: "Conversa com Imagem")
            chatDao.updateSession(session.copy(title = title, updatedAt = System.currentTimeMillis()))
        } else {
            chatDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
        }

        // 2. Build conversation history for API payload
        val allMessages = chatDao.getMessagesListForSession(session.id)
        val contentsPayload = mutableListOf<ContentPayload>()

        for (msg in allMessages) {
            val parts = mutableListOf<PartPayload>()

            // If message has attachment
            if (msg.role == "user" && msg.attachmentUri != null) {
                val uri = Uri.parse(msg.attachmentUri)
                if (msg.attachmentType == "image") {
                    val inlineData = geminiClient.prepareImageInlineData(uri)
                    if (inlineData != null) {
                        parts.add(PartPayload(inlineData = inlineData))
                    }
                } else if (msg.attachmentType == "file") {
                    val fileText = geminiClient.readFileContentAsText(uri)
                    if (!fileText.isNullOrBlank()) {
                        parts.add(PartPayload(text = "[Conteúdo do arquivo anexado '${msg.attachmentName ?: "anexo"}']:\n$fileText\n"))
                    }
                }
            }

            if (msg.content.isNotBlank()) {
                parts.add(PartPayload(text = msg.content))
            }

            if (parts.isNotEmpty()) {
                contentsPayload.add(
                    ContentPayload(
                        role = if (msg.role == "user") "user" else "model",
                        parts = parts
                    )
                )
            }
        }

        // 3. Call Gemini
        val settings = settingsRepository.settings.value
        val result = geminiClient.generateResponse(
            modelName = settings.selectedModel,
            apiKey = settings.apiKey,
            systemInstructionText = settings.customSystemPrompt,
            conversationHistory = contentsPayload,
            temperature = settings.temperature
        )

        return if (result.isSuccess) {
            val responseText = result.getOrNull() ?: ""
            val aiMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = session.id,
                role = "model",
                content = responseText,
                timestamp = System.currentTimeMillis(),
                isError = false
            )
            chatDao.insertMessage(aiMsg)
            chatDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
            Result.success(aiMsg)
        } else {
            val errorText = result.exceptionOrNull()?.message ?: "Erro ao gerar resposta da IA."
            val errorMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = session.id,
                role = "model",
                content = "⚠️ $errorText",
                timestamp = System.currentTimeMillis(),
                isError = true
            )
            chatDao.insertMessage(errorMsg)
            Result.failure(result.exceptionOrNull() ?: Exception(errorText))
        }
    }

    suspend fun regenerateLastResponse(sessionId: String): Result<ChatMessageEntity> {
        val messages = chatDao.getMessagesListForSession(sessionId)
        if (messages.isEmpty()) {
            return Result.failure(IllegalStateException("Nenhuma mensagem anterior para regenerar."))
        }

        // If the last message was from model (or error), delete it
        val lastMsg = messages.last()
        if (lastMsg.role == "model") {
            chatDao.deleteMessage(lastMsg)
        }

        // Find last user message
        val updatedMessages = chatDao.getMessagesListForSession(sessionId)
        if (updatedMessages.isEmpty()) {
            return Result.failure(IllegalStateException("Nenhuma mensagem de usuário encontrada."))
        }

        val contentsPayload = mutableListOf<ContentPayload>()
        for (msg in updatedMessages) {
            val parts = mutableListOf<PartPayload>()
            if (msg.role == "user" && msg.attachmentUri != null) {
                val uri = Uri.parse(msg.attachmentUri)
                if (msg.attachmentType == "image") {
                    val inlineData = geminiClient.prepareImageInlineData(uri)
                    if (inlineData != null) {
                        parts.add(PartPayload(inlineData = inlineData))
                    }
                } else if (msg.attachmentType == "file") {
                    val fileText = geminiClient.readFileContentAsText(uri)
                    if (!fileText.isNullOrBlank()) {
                        parts.add(PartPayload(text = "[Arquivo '${msg.attachmentName}']:\n$fileText\n"))
                    }
                }
            }
            if (msg.content.isNotBlank()) {
                parts.add(PartPayload(text = msg.content))
            }
            if (parts.isNotEmpty()) {
                contentsPayload.add(
                    ContentPayload(
                        role = if (msg.role == "user") "user" else "model",
                        parts = parts
                    )
                )
            }
        }

        val settings = settingsRepository.settings.value
        val result = geminiClient.generateResponse(
            modelName = settings.selectedModel,
            apiKey = settings.apiKey,
            systemInstructionText = settings.customSystemPrompt,
            conversationHistory = contentsPayload,
            temperature = settings.temperature
        )

        val session = chatDao.getSessionById(sessionId)
        return if (result.isSuccess) {
            val text = result.getOrNull() ?: ""
            val aiMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "model",
                content = text,
                timestamp = System.currentTimeMillis(),
                isError = false
            )
            chatDao.insertMessage(aiMsg)
            if (session != null) {
                chatDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
            }
            Result.success(aiMsg)
        } else {
            val errorText = result.exceptionOrNull()?.message ?: "Erro ao regenerar resposta."
            val errorMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "model",
                content = "⚠️ $errorText",
                timestamp = System.currentTimeMillis(),
                isError = true
            )
            chatDao.insertMessage(errorMsg)
            Result.failure(result.exceptionOrNull() ?: Exception(errorText))
        }
    }
}

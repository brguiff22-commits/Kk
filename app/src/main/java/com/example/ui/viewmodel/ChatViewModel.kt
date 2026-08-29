package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.repository.ChatRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val geminiClient = GeminiClient(application)
    private val settingsRepository = SettingsRepository(application)
    private val chatRepository = ChatRepository(
        context = application,
        chatDao = db.chatDao(),
        geminiClient = geminiClient,
        settingsRepository = settingsRepository
    )

    val settings: StateFlow<UserSettings> = settingsRepository.settings

    val allSessions: StateFlow<List<ChatSessionEntity>> = chatRepository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentSessionId.flatMapLatest { id ->
        if (id != null) {
            chatRepository.getMessagesForSession(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _attachedUri = MutableStateFlow<Uri?>(null)
    val attachedUri: StateFlow<Uri?> = _attachedUri.asStateFlow()

    private val _attachedType = MutableStateFlow<String?>(null)
    val attachedType: StateFlow<String?> = _attachedType.asStateFlow()

    private val _attachedName = MutableStateFlow<String?>(null)
    val attachedName: StateFlow<String?> = _attachedName.asStateFlow()

    private val _isListeningVoice = MutableStateFlow(false)
    val isListeningVoice: StateFlow<Boolean> = _isListeningVoice.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        viewModelScope.launch {
            allSessions.collect { sessions ->
                if (_currentSessionId.value == null && sessions.isNotEmpty()) {
                    _currentSessionId.value = sessions.first().id
                }
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun setAttachment(uri: Uri, type: String, name: String) {
        _attachedUri.value = uri
        _attachedType.value = type
        _attachedName.value = name
    }

    fun clearAttachment() {
        _attachedUri.value = null
        _attachedType.value = null
        _attachedName.value = null
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        clearAttachment()
    }

    fun createNewSession() {
        viewModelScope.launch {
            val session = chatRepository.createNewSession("Nova Conversa")
            _currentSessionId.value = session.id
            clearAttachment()
            _inputText.value = ""
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            chatRepository.updateSessionTitle(sessionId, newTitle)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                val remaining = allSessions.value.filter { it.id != sessionId }
                if (remaining.isNotEmpty()) {
                    _currentSessionId.value = remaining.first().id
                } else {
                    createNewSession()
                }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            chatRepository.deleteAllHistory()
            createNewSession()
        }
    }

    fun sendMessage() {
        val prompt = _inputText.value.trim()
        val uri = _attachedUri.value
        val type = _attachedType.value
        val name = _attachedName.value

        if (prompt.isEmpty() && uri == null) return

        viewModelScope.launch {
            var activeSessionId = _currentSessionId.value
            if (activeSessionId == null) {
                val newSession = chatRepository.createNewSession(
                    prompt.take(28).ifEmpty { name ?: "Nova Conversa" }
                )
                activeSessionId = newSession.id
                _currentSessionId.value = activeSessionId
            }

            _inputText.value = ""
            clearAttachment()
            _isGenerating.value = true

            chatRepository.sendMessage(
                sessionId = activeSessionId,
                userPrompt = prompt,
                attachmentUri = uri,
                attachmentType = type,
                attachmentName = name
            )

            _isGenerating.value = false
        }
    }

    fun regenerateLastResponse() {
        val sessionId = _currentSessionId.value ?: return
        if (_isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            chatRepository.regenerateLastResponse(sessionId)
            _isGenerating.value = false
        }
    }

    fun updateSettings(newSettings: UserSettings) {
        settingsRepository.updateSettings(newSettings)
    }

    // Voice Input Management with pt-BR locale
    fun toggleVoiceInput() {
        if (_isListeningVoice.value) {
            stopVoiceListening()
        } else {
            startVoiceListening()
        }
    }

    private fun startVoiceListening() {
        val context = getApplication<Application>()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Reconhecimento de voz não disponível no dispositivo", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListeningVoice.value = true
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListeningVoice.value = false
                    }
                    override fun onError(error: Int) {
                        _isListeningVoice.value = false
                    }
                    override fun onResults(results: Bundle?) {
                        _isListeningVoice.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognizedText = matches[0]
                            val current = _inputText.value
                            _inputText.value = if (current.isBlank()) recognizedText else "$current $recognizedText"
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            // Can show live partials
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale com Minha IA em português...")
            }

            speechRecognizer?.startListening(intent)
            _isListeningVoice.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isListeningVoice.value = false
            Toast.makeText(context, "Erro ao iniciar voz: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopVoiceListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isListeningVoice.value = false
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

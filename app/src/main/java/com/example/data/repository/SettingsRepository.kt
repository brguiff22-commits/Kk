package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeStyle(val displayName: String, val description: String) {
    MIDNIGHT("Midnight Indigo", "Preto profundo com toques de índigo e violeta"),
    OLED_BLACK("OLED Puro", "Preto absoluto com cinza escuro e alto contraste"),
    CYBER_NEON("Cyberpunk Neon", "Preto com ciano elétrico e lilás neon"),
    EMERALD_DARK("Dark Esmeralda", "Verde esmeralda escuro e grafite")
}

data class UserSettings(
    val appName: String = "Minha IA",
    val botPersonaName: String = "Minha IA",
    val userName: String = "Você",
    val apiKey: String = "",
    val selectedModel: String = "gemini-2.5-flash",
    val themeStyle: AppThemeStyle = AppThemeStyle.MIDNIGHT,
    val autoSpeak: Boolean = false,
    val temperature: Float = 0.7f,
    val customSystemPrompt: String = "Você é Minha IA, uma assistente virtual avançada, prestativa e amigável. Responda sempre em português do Brasil com clareza e rica formatação."
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("minha_ia_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val themeName = prefs.getString("theme_style", AppThemeStyle.MIDNIGHT.name)
        val theme = try {
            AppThemeStyle.valueOf(themeName ?: AppThemeStyle.MIDNIGHT.name)
        } catch (e: Exception) {
            AppThemeStyle.MIDNIGHT
        }

        return UserSettings(
            appName = prefs.getString("app_name", "Minha IA") ?: "Minha IA",
            botPersonaName = prefs.getString("bot_name", "Minha IA") ?: "Minha IA",
            userName = prefs.getString("user_name", "Você") ?: "Você",
            apiKey = prefs.getString("api_key", "") ?: "",
            selectedModel = prefs.getString("selected_model", "gemini-2.5-flash") ?: "gemini-2.5-flash",
            themeStyle = theme,
            autoSpeak = prefs.getBoolean("auto_speak", false),
            temperature = prefs.getFloat("temperature", 0.7f),
            customSystemPrompt = prefs.getString(
                "system_prompt",
                "Você é Minha IA, uma assistente virtual avançada, prestativa e amigável. Responda sempre em português do Brasil com clareza e rica formatação."
            ) ?: ""
        )
    }

    fun updateSettings(newSettings: UserSettings) {
        prefs.edit().apply {
            putString("app_name", newSettings.appName)
            putString("bot_name", newSettings.botPersonaName)
            putString("user_name", newSettings.userName)
            putString("api_key", newSettings.apiKey)
            putString("selected_model", newSettings.selectedModel)
            putString("theme_style", newSettings.themeStyle.name)
            putBoolean("auto_speak", newSettings.autoSpeak)
            putFloat("temperature", newSettings.temperature)
            putString("system_prompt", newSettings.customSystemPrompt)
            apply()
        }
        _settings.value = newSettings
    }
}

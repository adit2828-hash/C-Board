package com.example.data.model

import com.example.ui.theme.ThemeMode

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val keyboardLayoutMode: KeyboardLayoutMode = KeyboardLayoutMode.ALPHABET_AZ,
    val fontSizeScale: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val ttsLanguage: String = "id", // Indonesian ("id") or English ("en")
    val confirmClearAll: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val debounceMs: Long = 250L,
    val simulationMode: Boolean = false
)

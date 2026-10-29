package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppPreferences
import com.example.data.model.KeyboardLayoutMode
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "strokeboard_prefs")

class StrokeBoardPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val KEYBOARD_LAYOUT_MODE = stringPreferencesKey("keyboard_layout_mode")
        val FONT_SIZE_SCALE = floatPreferencesKey("font_size_scale")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val TTS_LANGUAGE = stringPreferencesKey("tts_language")
        val CONFIRM_CLEAR_ALL = booleanPreferencesKey("confirm_clear_all")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val DEBOUNCE_MS = longPreferencesKey("debounce_ms")
        val SIMULATION_MODE = booleanPreferencesKey("simulation_mode")
    }

    val preferencesFlow: Flow<AppPreferences> = context.dataStore.data.map { preferences ->
        val themeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val theme = runCatching { ThemeMode.valueOf(themeString) }.getOrDefault(ThemeMode.SYSTEM)
        val layoutString = preferences[PreferencesKeys.KEYBOARD_LAYOUT_MODE] ?: KeyboardLayoutMode.ALPHABET_AZ.name
        val layout = runCatching { KeyboardLayoutMode.valueOf(layoutString) }.getOrDefault(KeyboardLayoutMode.ALPHABET_AZ)
        val fontScale = preferences[PreferencesKeys.FONT_SIZE_SCALE] ?: 1.0f
        val rate = preferences[PreferencesKeys.SPEECH_RATE] ?: 1.0f
        val pitch = preferences[PreferencesKeys.SPEECH_PITCH] ?: 1.0f
        val lang = preferences[PreferencesKeys.TTS_LANGUAGE] ?: "id"
        val confirmClear = preferences[PreferencesKeys.CONFIRM_CLEAR_ALL] ?: true
        val haptic = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true
        val debounce = preferences[PreferencesKeys.DEBOUNCE_MS] ?: 250L
        val simulation = preferences[PreferencesKeys.SIMULATION_MODE] ?: false

        AppPreferences(
            themeMode = theme,
            keyboardLayoutMode = layout,
            fontSizeScale = fontScale,
            speechRate = rate,
            speechPitch = pitch,
            ttsLanguage = lang,
            confirmClearAll = confirmClear,
            hapticFeedbackEnabled = haptic,
            debounceMs = debounce,
            simulationMode = simulation
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun setKeyboardLayoutMode(mode: KeyboardLayoutMode) {
        context.dataStore.edit { it[PreferencesKeys.KEYBOARD_LAYOUT_MODE] = mode.name }
    }

    suspend fun setFontSizeScale(scale: Float) {
        context.dataStore.edit { it[PreferencesKeys.FONT_SIZE_SCALE] = scale }
    }

    suspend fun setSpeechRate(rate: Float) {
        context.dataStore.edit { it[PreferencesKeys.SPEECH_RATE] = rate }
    }

    suspend fun setTtsLanguage(lang: String) {
        context.dataStore.edit { it[PreferencesKeys.TTS_LANGUAGE] = lang }
    }

    suspend fun setConfirmClearAll(confirm: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CONFIRM_CLEAR_ALL] = confirm }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setDebounceMs(debounceMs: Long) {
        context.dataStore.edit { it[PreferencesKeys.DEBOUNCE_MS] = debounceMs }
    }

    suspend fun setSimulationMode(simulation: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SIMULATION_MODE] = simulation }
    }
}

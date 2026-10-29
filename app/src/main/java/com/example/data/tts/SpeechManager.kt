package com.example.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "StrokeBoardTTS"
        private const val UTTERANCE_ID = "strokeboard_utterance"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _ttsStatusMessage = MutableStateFlow<String?>(null)
    val ttsStatusMessage: StateFlow<String?> = _ttsStatusMessage.asStateFlow()

    private var lastSpokenText: String = ""
    private var currentRate: Float = 1.0f
    private var currentPitch: Float = 1.0f
    private var currentLanguageCode: String = "id"

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            applyLanguage(currentLanguageCode)
            applySettings(currentRate, currentPitch)
            setupProgressListener()
            _ttsStatusMessage.value = "TTS Siap digunakan"
            Log.d(TAG, "TextToSpeech successfully initialized")
        } else {
            isInitialized = false
            _ttsStatusMessage.value = "Gagal menginisialisasi mesin Text-To-Speech"
            Log.e(TAG, "Failed to initialize TextToSpeech, status: $status")
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _ttsStatusMessage.value = "Kesalahan pemutaran suara"
            }
        })
    }

    fun applyLanguage(langCode: String) {
        currentLanguageCode = langCode
        if (!isInitialized || tts == null) return

        val targetLocale = if (langCode.equals("id", ignoreCase = true)) {
            Locale("id", "ID")
        } else {
            Locale.US
        }

        val result = tts?.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Language $targetLocale not supported, falling back to default")
            tts?.setLanguage(Locale.getDefault())
            _ttsStatusMessage.value = "Bahasa Indonesia tidak tersedia di mesin TTS perangkat, menggunakan bahasa sistem."
        } else {
            _ttsStatusMessage.value = "Bahasa TTS: ${targetLocale.displayLanguage}"
        }
    }

    fun applySettings(rate: Float, pitch: Float) {
        currentRate = rate
        currentPitch = pitch
        tts?.setSpeechRate(rate)
        tts?.setPitch(pitch)
    }

    fun speak(text: String) {
        if (text.isBlank()) {
            _ttsStatusMessage.value = "Pesan masih kosong"
            return
        }

        if (!isInitialized || tts == null) {
            _ttsStatusMessage.value = "Mesin TTS sedang disiapkan..."
            return
        }

        lastSpokenText = text
        _isSpeaking.value = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun repeat() {
        if (lastSpokenText.isNotBlank()) {
            speak(lastSpokenText)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}

package com.example.blindnavjpc.helpers

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

object TTSManager : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("id", "ID"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle language not supported
            } else {
                isInitialized = true
            }
        } else {
            // Handle initialization failed
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    fun readTextWithAccessibility(text: String) {
        if (isInitialized) {
            // Speak the text using TTS without creating an AccessibilityEvent
            speak(text)
        }
    }


    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
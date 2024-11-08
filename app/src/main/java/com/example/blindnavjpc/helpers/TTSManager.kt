package com.example.blindnavjpc.helpers

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

object TTSManager : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var onDoneCallback: (() -> Unit)? = null

    fun initialize(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context, this).apply {
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        // Handle TTS start (optional, if needed)
                    }

                    override fun onDone(utteranceId: String?) {
                        // Callback saat TTS selesai
                        onDoneCallback?.invoke()
                    }

                    override fun onError(utteranceId: String?) {
                        // Handle error (optional, if needed)
                    }
                })
            }
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

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (isInitialized) {
            onDoneCallback = onDone  // Set the callback
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UniqueID")
        }
    }

    fun readTextWithAccessibility(text: String, onDone: (() -> Unit)? = null) {
        speak(text, onDone)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}

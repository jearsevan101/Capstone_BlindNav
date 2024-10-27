package com.example.blindnavjpc.helpers

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.example.blindnavjpc.MainActivity
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.net.URL
import java.net.URLDecoder
import java.util.Locale

class ScannerHelper(private val context: Context) {
    private var onMarkerDetectedCallback: ((Int) -> Unit)? = null
    private var _lastDetectedMarker: Int? = null

    val lastDetectedMarker: Int?
        get() = _lastDetectedMarker

    fun setOnMarkerDetectedListener(callback: (Int) -> Unit) {
        onMarkerDetectedCallback = callback
    }

    // Call this when a marker is detected from your scanner
    fun onMarkerDetected(markerId: Int) {
        _lastDetectedMarker = markerId
        onMarkerDetectedCallback?.invoke(markerId)
    }
    private var isScannerInstalled = false
    private lateinit var scanner: GmsBarcodeScanner
    private lateinit var textToSpeech: TextToSpeech

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.getDefault()
            }
        }
    }

    fun installGoogleScanner() {
        isScannerInstalled = true
        scanner = GmsBarcodeScanning.getClient(context, initializeGoogleScanner())
    }

    private fun initializeGoogleScanner(): GmsBarcodeScannerOptions {
        return GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
    }

    fun startScanning(
        onSuccess: (String) -> Unit,
        onCancelled: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (isScannerInstalled) {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        // Ekstrak dan proses konten QR
                        val content = extractContentFromQR(rawValue)
                        speakText(content) // Langsung membaca konten
                        onSuccess(content)
                    }
                }
                .addOnCanceledListener {
                    onCancelled()
                }
                .addOnFailureListener { e ->
                    val errorMessage = "Error: ${e.message}"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    onError(errorMessage)
                }
        } else {
            val errorMessage = "Scanner belum diinstall"
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onError(errorMessage)
        }
    }

    private fun extractContentFromQR(qrValue: String): String {
        return try {
            when {
                qrValue.contains("?") -> {
                    val url = URL(qrValue)
                    val query = url.query ?: ""
                    val params = query.split("&").associate {
                        val parts = it.split("=")
                        if (parts.size == 2) parts[0] to URLDecoder.decode(parts[1], "UTF-8")
                        else "" to ""
                    }
                    params["content"] ?: params["text"] ?: params["message"] ?: qrValue
                }
                qrValue.contains("/") -> {
                    val pathSegments = URL(qrValue).path.split("/")
                    if (pathSegments.isNotEmpty()) {
                        URLDecoder.decode(pathSegments.last(), "UTF-8")
                    } else qrValue
                }
                else -> qrValue
            }
        } catch (e: Exception) {
            qrValue
        }
    }

    private fun speakText(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Cleanup method untuk TextToSpeech
    fun cleanup() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    fun setOnMarkerDetectedListener(any: Any) {
        //kalau ada aruco kedetect
    }
}
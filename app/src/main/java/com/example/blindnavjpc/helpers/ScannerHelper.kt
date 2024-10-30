package com.example.blindnavjpc.helpers

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
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
    private var scanMode: ScanMode = ScanMode.CONTINUOUS
    enum class ScanMode {
        SINGLE_SCAN,  // Scan once and close
        CONTINUOUS    // Continue scanning after detection
    }
    val lastDetectedMarker: Int?
        get() = _lastDetectedMarker

    fun setOnMarkerDetectedListener(callback: (Int) -> Unit) {
        onMarkerDetectedCallback = callback
    }
    fun setScanMode(mode: ScanMode) {
        scanMode = mode
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
        onSuccess: (Int) -> Unit,
        onCancelled: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (isScannerInstalled) {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        // Check if QR content is numeric
                        val content = extractContentFromQR(rawValue)
                        if (content.isNumeric()) {
                            val markerId = content.toInt()
                            val displayText = "Marker ID: $markerId"

                            // Handle the marker detection based on scan mode
                            when (scanMode) {
                                ScanMode.SINGLE_SCAN -> {
                                    onMarkerDetected(markerId)
                                    speakText("Marker ID $markerId detected")
                                    onSuccess(markerId)
                                    // Close the scanner after detection
                                    cleanup()
                                }
                                ScanMode.CONTINUOUS -> {
                                    onMarkerDetected(markerId)
                                    speakText("Marker ID $markerId detected")
                                    onSuccess(markerId)
                                    // Continue scanning - no cleanup
                                }
                            }
                        } else {
                            speakText("Invalid marker format")
                            onError("Invalid marker format: Not a number")
                        }
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
    private fun String.isNumeric(): Boolean {
        return this.all { it.isDigit() }
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
}

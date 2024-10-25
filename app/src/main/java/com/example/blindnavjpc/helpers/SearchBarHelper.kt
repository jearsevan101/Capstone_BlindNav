package com.example.blindnavjpc.helpers

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResultLauncher
import android.widget.Toast

class SearchBarHelper(
    private val context: Context
) {
    data class RoomInfo(
        val name: String,
        val floor: Int,
        val category: String
    )

    fun startVoiceRecognition(voiceLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Sebutkan ruangan yang Anda cari")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
        }
        try {
            voiceLauncher.launch(intent)
        } catch (e: Exception) {
            TTSManager.speak("Error memulai pengenalan suara: ${e.message}")
        }
    }

    fun processVoiceResult(
        voiceInput: String,
        onRoomFound: (RoomInfo) -> Unit,
        onSearchError: (String) -> Unit
    ) {
        val roomInfo = findMatchingRoom(voiceInput)
        if (roomInfo != null) {
            onRoomFound(roomInfo)
        } else {
            onSearchError("Ruangan tidak ditemukan. Coba lagi.")
            TTSManager.speak("Ruangan tidak ditemukan. Coba lagi.")
        }
    }

    private fun findMatchingRoom(voiceInput: String): RoomInfo? {
        for (floor in 1..3) {
            for (category in listOf("Laboratorium", "Ruang Kelas", "Ruang Kantor dan Staff", "Fasilitas Lain")) {
                val rooms = getRoomsForFloorAndCategory(floor, category)
                for (room in rooms) {
                    // Check for a match
                    if (room.lowercase().contains(voiceInput.lowercase())) {
                        return RoomInfo(room, floor, category)
                    }
                }
            }
        }
        return null
    }

    private fun getRoomsForFloorAndCategory(floor: Int, category: String): List<String> {
        return com.example.blindnavjpc.screens.getRoomsForFloorAndCategory(floor, category)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

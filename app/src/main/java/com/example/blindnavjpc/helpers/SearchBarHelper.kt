package com.example.blindnavjpc.helpers

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.example.blindnavjpc.dataconnection.ApiService
import com.example.blindnavjpc.dataconnection.Rooms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchBarHelper(
    private val context: Context,
    private val apiService: ApiService,
    private val coroutineScope: CoroutineScope
) {
    data class RoomInfo(
        val roomNumber: String,
        val roomName: String,
        val floor: Int,
        val category: String
    )

    private var cachedRooms: List<Rooms>? = null

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
        onSearchError: (String) -> Unit,
        onSearchStarted: () -> Unit = {}
    ) {
        onSearchStarted()
        coroutineScope.launch {
            try {
                val roomInfo = findMatchingRoom(voiceInput)
                withContext(Dispatchers.Main) {
                    if (roomInfo != null) {
                        onRoomFound(roomInfo)
                    } else {
                        val errorMessage = "Ruangan tidak ditemukan. Coba lagi."
                        onSearchError(errorMessage)
                        TTSManager.speak(errorMessage)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = "Error mencari ruangan: ${e.message}"
                    onSearchError(errorMessage)
                    TTSManager.speak(errorMessage)
                }
            }
        }
    }

    private suspend fun findMatchingRoom(voiceInput: String): RoomInfo? {
        try {
            // Use cached rooms if available, otherwise fetch from API
            val rooms = cachedRooms ?: fetchAndCacheRooms()

            // Convert voice input to lowercase for case-insensitive matching
            val searchTerm = voiceInput.lowercase()

            // Search through all rooms
            return rooms.find { room ->
                room.room_number.lowercase().contains(searchTerm) ||
                        room.room_name.lowercase().contains(searchTerm)
            }?.let { room ->
                RoomInfo(
                    roomNumber = room.room_number,
                    roomName = room.room_name,
                    floor = room.floor,
                    category = room.room_type
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to search rooms: ${e.message}")
        }
    }

    private suspend fun fetchAndCacheRooms(): List<Rooms> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRooms()
                if (response.isSuccessful) {
                    val rooms = response.body() ?: emptyList()
                    cachedRooms = rooms // Cache the results
                    rooms
                } else {
                    throw Exception("Failed to fetch rooms: ${response.message()}")
                }
            } catch (e: Exception) {
                throw Exception("Error fetching rooms: ${e.message}")
            }
        }
    }

    // Function to manually clear the cache if needed
    fun clearCache() {
        cachedRooms = null
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Helper function to refresh the rooms data
    fun refreshRoomsData(
        onRefreshStarted: () -> Unit = {},
        onRefreshComplete: () -> Unit = {},
        onRefreshError: (String) -> Unit = {}
    ) {
        onRefreshStarted()
        coroutineScope.launch {
            try {
                clearCache()
                fetchAndCacheRooms()
                withContext(Dispatchers.Main) {
                    onRefreshComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onRefreshError("Failed to refresh rooms: ${e.message}")
                }
            }
        }
    }
}
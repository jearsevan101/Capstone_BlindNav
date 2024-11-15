package com.example.blindnavjpc.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blindnavjpc.R
import com.example.blindnavjpc.dataconnection.ApiService
import com.example.blindnavjpc.helpers.TTSManager
import com.example.blindnavjpc.ui.theme.fontFamily
import kotlinx.coroutines.delay

data class Room(
    val id: Int,
    val arucoId: Int,
    val number: String,
    val name: String,
    val fullName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomSelectionScreen(
    apiService: ApiService,
    floor: Int,
    category: String,
    onRoomSelected: (Room) -> Unit,
    onBackClick: () -> Unit
) {
    var rooms by remember { mutableStateOf<List<Room>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    rememberCoroutineScope()

    // Fetch rooms and initiate TTS on first display
    LaunchedEffect(Unit) {
        try {
            delay(2000) // Delay to ensure TTS readiness
            TTSManager.speak("Silakan pilih ruangan pada kategori $category di lantai $floor.")
            val response = apiService.getRooms()
            val markersResponse = apiService.getAllArucoMarkers() // Fetch markers data

            if (response.isSuccessful && markersResponse.isSuccessful) {
                val allRooms = response.body() ?: emptyList()
                val allMarkers = markersResponse.body() ?: emptyList()

                rooms = allRooms
                    .filter { it.floor == floor && it.room_type == category }
                    .map { room ->
                        val matchingMarker = allMarkers.firstOrNull { marker ->
                            marker.west_room_id == room.room_id ||
                                    marker.east_room_id == room.room_id ||
                                    marker.north_room_id == room.room_id ||
                                    marker.south_room_id == room.room_id
                        }
                        Room(
                            id = room.room_id,
                            arucoId = matchingMarker?.marker_id ?: 0,
                            number = room.room_number,
                            name = room.room_name,
                            fullName = "${room.room_number}: ${room.room_name}"
                        )
                    }
            } else {
                error = "Gagal mengambil data ruang ${response.message()}"
            }
        } catch (e: Exception) {
            error = "Terjadi kesalahan saat memuat data ruang ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pilih Ruang - Lantai $floor",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = fontFamily
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.primary),
                    titleContentColor = Color.White,
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Terjadi kesalahan",
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Kategori: $category",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp, top = 32.dp),
                            textAlign = TextAlign.Center
                        )

                        LazyColumn(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(rooms) { room ->
                                RoomButton(room, onRoomSelected)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    TTSManager.speak("Kembali ke layar sebelumnya")
                    onBackClick()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 32.dp, end = 32.dp)
                    .semantics { contentDescription = "Kembali ke layar sebelumnya" },
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.tertiary),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    "Kembali",
                    fontSize = 24.sp,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RoomButton(room: Room, onRoomSelected: (Room) -> Unit) {
    Button(
        onClick = {
            TTSManager.speak("Memilih ruangan ${room.fullName}")
            onRoomSelected(room)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .semantics { contentDescription = "Pilih Ruangan ${room.fullName}" },
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.secondary),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Text(
            text = room.number,
            fontSize = 24.sp,
            fontFamily = fontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            lineHeight = 24.sp
        )
    }
}

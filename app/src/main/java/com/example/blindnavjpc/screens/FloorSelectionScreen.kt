
package com.example.blindnavjpc.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
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
import com.example.blindnavjpc.helpers.TTSManager
import com.example.blindnavjpc.ui.theme.fontFamily
import com.example.blindnavjpc.dataconnection.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.time.Duration

sealed class RoomsState {
    data object Loading : RoomsState()
    data class Success(val rooms: List<Rooms>) : RoomsState()
    data class Error(val message: String) : RoomsState()
}

@Composable
private fun FloorButton(
    floorNumber: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onClick(floorNumber) },
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(bottom = 24.dp, start = 32.dp, end = 32.dp)
            .semantics { contentDescription = "Lantai $floorNumber" },
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.secondary),
            contentColor = Color.White
        )
    ) {
        Text(
            "Lantai $floorNumber",
            fontSize = 18.sp,
            fontFamily = fontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorSelectionScreen(
    apiService: ApiService,
    onFloorSelected: (Int) -> Unit,
    onDiscardClick: () -> Unit
) {
    var roomsState by remember { mutableStateOf<RoomsState>(RoomsState.Loading) }
    val scope = rememberCoroutineScope()
    var isTTSReady by remember { mutableStateOf(false) }
    var isTalkBackEnabled by remember { mutableStateOf(true) }

    // LaunchedEffect untuk inisialisasi TTS
    LaunchedEffect(Unit) {
        // Delay kecil untuk memastikan TTS engine siap
        delay(Duration.ofMillis(1000))
        isTTSReady = true
    }

    // LaunchedEffect untuk memainkan suara setelah TTS siap
    LaunchedEffect(isTTSReady) {
        if (isTTSReady) {
            // Matikan TalkBack sementara
            isTalkBackEnabled = false
            TTSManager.speak("Silakan pilih lantai yang ingin anda tuju.")
            {
                // Aktifkan kembali TalkBack setelah TTS selesai
                isTalkBackEnabled = true
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = apiService.getRooms()
                if (response.isSuccessful) {
                    response.body()?.let { rooms ->
                        roomsState = RoomsState.Success(rooms)
                    } ?: run {
                        roomsState = RoomsState.Error("Data kosong")
                    }
                } else {
                    roomsState = RoomsState.Error("Gagal mengambil data: ${response.message()}")
                }
            } catch (e: Exception) {
                roomsState = RoomsState.Error("Terjadi kesalahan: ${e.message}")
            }
        }

//        TTSManager.speak("Silakan pilih lantai yang ingin anda tuju.")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pilih Lantai",
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
            when (roomsState) {
                is RoomsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is RoomsState.Error -> {
                    Text(
                        text = (roomsState as RoomsState.Error).message,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                is RoomsState.Success -> {
                    val uniqueFloors = (roomsState as RoomsState.Success)
                        .rooms
                        .map { it.floor }
                        .distinct()
                        .sorted()

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        uniqueFloors.forEach { floor ->
                            FloorButton(
                                floorNumber = floor,
                                onClick = onFloorSelected
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onDiscardClick,
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


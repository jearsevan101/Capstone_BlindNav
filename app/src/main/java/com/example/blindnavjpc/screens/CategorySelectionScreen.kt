package com.example.blindnavjpc.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    apiService: ApiService,
    floor: Int,
    onCategorySelected: (String) -> Unit = {},
    onDiscardClick: () -> Unit = {}
) {
    var roomsState by remember { mutableStateOf<RoomsState>(RoomsState.Loading) }
    val scope = rememberCoroutineScope()
    var isTTSReady by remember { mutableStateOf(false) }
    var isTalkBackEnabled by remember { mutableStateOf(true) }

    // Inisialisasi delay untuk TTS
    LaunchedEffect(Unit) {
        delay(2000)  // Delay sebelum TTS dimulai
        isTTSReady = true
    }

    // Play TTS ketika siap
    LaunchedEffect(isTTSReady) {
        if (isTTSReady) {
            // Menonaktifkan TalkBack sementara
            isTalkBackEnabled = false
            TTSManager.speak("Silakan pilih kategori ruangan di lantai $floor.") {
                // Mengaktifkan TalkBack setelah TTS selesai
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

        TTSManager.speak("Silakan pilih kategori ruangan di lantai $floor.")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pilih Kategori - Lantai $floor",
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
                    // Get unique categories for the selected floor
                    val categories = (roomsState as RoomsState.Success)
                        .rooms
                        .filter { it.floor == floor }
                        .map { it.room_type }
                        .distinct()
                        .sorted()

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        categories.forEach { category ->
                            CategoryButton(category) {
                                onCategorySelected(category)
                            }
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

@Composable
fun CategoryButton(
    category: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.secondary),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Text(
            text = category,
            fontSize = 24.sp,
            fontFamily = fontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

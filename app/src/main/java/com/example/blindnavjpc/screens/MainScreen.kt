package com.example.blindnavjpc.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blindnavjpc.R
import com.example.blindnavjpc.helpers.ScannerHelper
import com.example.blindnavjpc.ui.theme.fontFamily
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.blindnavjpc.helpers.SearchBarHelper
import com.example.blindnavjpc.helpers.TTSManager
import kotlinx.coroutines.launch

@Composable
fun MainScreen(scannerHelper: ScannerHelper) {
    var currentScreen by remember { mutableStateOf("main") }
    var selectedFloor by remember { mutableIntStateOf(1) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedRoom by remember { mutableStateOf("") }
    var isQrScanned by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val searchBarHelper = remember { SearchBarHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.get(0)?.let { voiceInput ->
                searchBarHelper.processVoiceResult(
                    voiceInput = voiceInput,
                    onRoomFound = { roomInfo ->
                        selectedFloor = roomInfo.floor
                        selectedCategory = roomInfo.category
                        selectedRoom = roomInfo.name
                        val roomDetails = "Menuju ke ${roomInfo.name}, terletak di lantai ${roomInfo.floor} kategori ${roomInfo.category}."
                        TTSManager.speak(roomDetails)

                        coroutineScope.launch {
                            kotlinx.coroutines.delay(12000)
                            currentScreen = "navigation"
                        }
                    },
                    onSearchError = {
                        // Handle search error
                    }
                )
            }
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == "main") {
            // Modify the TTS message based on whether QR has been scanned
            if (!isQrScanned) {
                TTSManager.speak("Anda berada di halaman Home. Untuk mengetahui informasi gedung, silakan scan QR terlebih dahulu. Setelah itu, untuk memulai navigasi silakan tekan tombol Pilih Lantai. Namun, jika anda sudah familiar dengan gedung ini, anda dapat menggunakan fitur mencari ruang dengan suara.")
            } else {
                TTSManager.speak("Anda berada di halaman Home. Silakan tekan tombol Pilih Lantai untuk memulai navigasi, atau gunakan fitur pencarian suara untuk mencari ruang.")
            }
        }
    }

    when (currentScreen) {
        "main" -> {
            MainMenu(
                onScanClick = {
                    TTSManager.speak("Scan QR dimulai, silakan arahkan kamera ke QR code. Untuk membatalkan proses ini silakan tekan button X di ujung kiri atas")
                    scannerHelper.startScanning(
                        onSuccess = {
                            isQrScanned = true
                            currentScreen = "main"
                            TTSManager.speak("Scan berhasil. Silakan pilih lantai untuk memulai navigasi.")
                        },
                        onError = {
                            TTSManager.speak("Terjadi kesalahan dalam pemindaian")
                        },
                        onCancelled = {
                            TTSManager.speak("Pemindaian dibatalkan") // Stop any ongoing TTS
                        }
                    )
                },
                onSelectFloorClick = {
                    currentScreen = "selectFloor"
                },
                onVoiceSearchClick = {
                    TTSManager.speak("Silakan katakan nama ruang yang ingin Anda cari.")
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(3500)
                        searchBarHelper.startVoiceRecognition(voiceLauncher)
                    }
                },
                isQrScanned = isQrScanned
            )
        }
        "selectFloor" -> {
            FloorSelectionScreen(
                onFloorSelected = { floor ->
                    selectedFloor = floor
                    currentScreen = "categorySelection"
                },
                onDiscardClick = {
                    currentScreen = "main"
                }
            )
        }
        "categorySelection" -> {
            CategorySelectionScreen(
                floor = selectedFloor,
                onCategorySelected = { category ->
                    selectedCategory = category
                    currentScreen = "roomSelection"
                },
                onDiscardClick = {
                    currentScreen = "selectFloor"
                }
            )
        }
        "roomSelection" -> {
            RoomSelectionScreen(
                floor = selectedFloor,
                category = selectedCategory,
                onRoomSelected = { room ->
                    selectedRoom = room
                    currentScreen = "navigation"
                },
                onBackClick = {
                    currentScreen = "categorySelection"
                }
            )
        }
        "navigation" -> {
            NavigationScreen(
                onDiscardClick = { currentScreen = "roomSelection" },
                onBackToHomeClick = { currentScreen = "main" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(
    onScanClick: () -> Unit,
    onSelectFloorClick: () -> Unit,
    onVoiceSearchClick: () -> Unit,
    isQrScanned: Boolean
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Home",
                        fontSize = 32.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onVoiceSearchClick,
                    modifier = Modifier
                        .size(100.dp)
                        .background(color = Color.LightGray, shape = CircleShape)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.mic),
                        contentDescription = "Voice Search",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                MainMenuButton(
                    text = "Scan QR",
                    onClick = onScanClick,
                    contentDescription = "Scan QR for Building Information",
                    isQrScanned = true  // Scan QR button should always be enabled
                )

                Spacer(modifier = Modifier.height(24.dp))

                MainMenuButton(
                    text = "Select Floor",
                    onClick = onSelectFloorClick,
                    contentDescription = "Select Floor",
                    isQrScanned = isQrScanned  // This button should be disabled until QR is scanned
                )
            }
        }
    }
}

@Composable
fun MainMenuButton(
    text: String,
    onClick: () -> Unit,
    contentDescription: String,
    isQrScanned: Boolean
) {
    val isEnabled = when (text) {
        "Select Floor" -> isQrScanned
        else -> true
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .semantics {
                this.contentDescription = if (!isEnabled && text == "Select Floor") {
                    "Select Floor button disabled. Please scan QR first"
                } else {
                    contentDescription
                }
            },
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.secondary),
            contentColor = Color.White,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.White
        ),
        enabled = isEnabled,
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontFamily = fontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainMenu() {
    MainMenu(
        onScanClick = {},
        onSelectFloorClick = {},
        onVoiceSearchClick = {},
        isQrScanned = false  // Preview with QR not scanned
    )
}
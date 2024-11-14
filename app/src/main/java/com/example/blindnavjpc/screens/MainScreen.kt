package com.example.blindnavjpc.screens

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
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
import android.content.IntentFilter
import android.os.Build
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.blindnavjpc.dataconnection.ApiService
import com.example.blindnavjpc.helpers.SearchBarHelper
import com.example.blindnavjpc.helpers.TTSManager
import kotlinx.coroutines.launch
import com.example.blindnavjpc.dataconnection.NavigationState
import com.example.blindnavjpc.CameraActivity
import kotlinx.coroutines.time.delay
import java.time.Duration


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    navigationState: NavigationState,
    onDestinationSelected: (currentId: Int, destinationId: Int) -> Unit,
    onDistanceAngleUpdated: (currentId: Int, distance: Float, angle: Float) -> Unit,
    apiService: ApiService
) {
    var currentScreen by remember { mutableStateOf("main") }
    var selectedFloor by remember { mutableIntStateOf(1) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedRoom by remember { mutableStateOf("") }
    var selectedRoomID by remember { mutableIntStateOf(1) }
    var currentArucoID by remember { mutableIntStateOf(1) }
    var currentDistance by remember { mutableFloatStateOf(1F) }
    var currentAngle by remember { mutableFloatStateOf(1F) }
    var isQrScanned by remember { mutableStateOf(false) }
//    var isScannerActive by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var isNavigationMode by remember { mutableStateOf(false) }
    var isTalkBackEnabled by remember { mutableStateOf(true) }
    var isTTSReady by remember { mutableStateOf(false) }


    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val searchBarHelper = remember {
        SearchBarHelper(
            context = context,
            apiService = apiService,
            coroutineScope = coroutineScope
        )
    }
// Create broadcast receiver
    val markerReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent?.action == CameraActivity.MARKER_UPDATE_ACTION) {
                    val markerId = intent.getStringExtra(CameraActivity.EXTRA_MARKER_ID)?.toIntOrNull() ?: return
                    val distance = intent.getStringExtra(CameraActivity.EXTRA_DISTANCE)?.toFloatOrNull() ?: return
                    val angle = intent.getStringExtra(CameraActivity.EXTRA_ANGLE)?.toFloatOrNull() ?: return

                    currentArucoID = markerId
                    currentDistance = Math.round(distance * 100*100) / 100f // Convert to cm
                    currentAngle = Math.round(angle * 100) / 100f
                    isQrScanned = true

                    if (!isNavigationMode) {
                        TTSManager.speak("Silahkan maju ke depan sejauh ${currentDistance.toInt()} centimeter, selanjutnya silahkan pilih ruangan yang ingin dituju")

                        // Use coroutineScope to handle the delay and camera stop
                        coroutineScope.launch {
                            // Wait for TTS to finish (adjust delay as needed)
//                            delay(3000)
                            // Use the remembered context instead of receiver context
                            LocalBroadcastManager.getInstance(context)
                                .sendBroadcast(Intent("STOP_CAMERA"))
                            currentScreen = "main"
                        }
                    } else {
                        if (currentArucoID == selectedRoomID){
                            TTSManager.speak("Anda telah tiba di lokasi tujuan")
                            coroutineScope.launch {
                                // Use the remembered context instead of receiver context
                                LocalBroadcastManager.getInstance(context)
                                    .sendBroadcast(Intent("STOP_CAMERA"))
                                currentScreen = "main"
                            }
                        }
                        onDistanceAngleUpdated(currentArucoID, currentDistance, currentAngle)
                    }
                }
            }
        }
    }

    // Launch camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {  }
    // Register/unregister receiver
    DisposableEffect(Unit) {
        val filter = IntentFilter(CameraActivity.MARKER_UPDATE_ACTION)
        val localBroadcastManager = LocalBroadcastManager.getInstance(context)
        localBroadcastManager.registerReceiver(markerReceiver, filter)

        onDispose {
            localBroadcastManager.unregisterReceiver(markerReceiver)
        }
    }
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
                        selectedRoom = roomInfo.roomName
                        val roomDetails = "Menuju ke ${roomInfo.roomName}, terletak di lantai ${roomInfo.floor} kategori ${roomInfo.category}."
                        TTSManager.speak(roomDetails)

                        coroutineScope.launch {
                            kotlinx.coroutines.delay(12000)
                            currentScreen = "navigation"
                        }
                    },
                    onSearchError = { errorMessage ->
                        // Handle the error message
                        TTSManager.speak(errorMessage)
                    },
                    onSearchStarted = {
                        isSearching = true
                    }
                )
            }
        }
    }

    // LaunchedEffect untuk memastikan TTS siap
    LaunchedEffect(Unit) {
        // Beri sedikit delay untuk memastikan TTS engine siap
        delay(Duration.ofMillis(1000))
        isTTSReady = true
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == "main" && isTTSReady) {
            if (!isQrScanned) {
                // Matikan TalkBack sementara
                isTalkBackEnabled = false
                TTSManager.speak("Anda berada di halaman beranda. Untuk mengetahui informasi gedung, silakan pindai aruco terlebih dahulu. Setelah itu, untuk memulai navigasi silakan tekan tombol Pilih Lantai. Namun, jika anda sudah familiar dengan gedung ini, anda dapat menggunakan fitur mencari ruang dengan suara.")
                // Aktifkan kembali TalkBack setelah 3 detik
                coroutineScope.launch {
                    delay(Duration.ofMillis(3000))
                    isTalkBackEnabled = true
                }
            } else {

                // Matikan TalkBack sementara
                isTalkBackEnabled = false
                TTSManager.speak("Anda berada di halaman beranda. Silakan tekan tombol Pilih Lantai untuk memulai navigasi, atau gunakan fitur pencarian suara untuk mencari ruang.")
                // Aktifkan kembali TalkBack setelah 3 detik
                coroutineScope.launch {
                    delay(Duration.ofMillis(3000))
                    isTalkBackEnabled = true
                }
            }
        }
    }

    when (currentScreen) {
        "main" -> {
            MainMenu(
                onScanClick = {
                    TTSManager.speak("Pemindaian Aruco dimulai, silakan arahkan kamera ke penanda aruco")
                    isNavigationMode = false
//                    isScannerActive = true

                    val intent = Intent(context, CameraActivity::class.java)
                    cameraLauncher.launch(intent)

//                    if (isQrScanned == true){
////                        isScannerActive = false
//                        currentScreen = "main"
//                    }
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
                },
                apiService = apiService
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
                },
                apiService = apiService,
            )
        }
        "roomSelection" -> {
            RoomSelectionScreen(
                floor = selectedFloor,
                category = selectedCategory,
                onRoomSelected = { room ->
                    selectedRoom = room.name
                    selectedRoomID = room.arucoId
                    onDestinationSelected(currentArucoID,selectedRoomID)
                    currentScreen = "navigation"
                },
                onBackClick = {
                    currentScreen = "categorySelection"
                },
                apiService = apiService
            )
        }
        "navigation" -> {
            NavigationScreen(
                navigationState = navigationState,
                onDiscardClick = { currentScreen = "roomSelection"
                    LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(Intent("CLOSE_CAMERA"))},
                onBackToHomeClick = { currentScreen = "main"
                    LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(Intent("CLOSE_CAMERA"))},
                onScanClick = {
                    isNavigationMode = true
                    TTSManager.speak("Memulai pemindaian Penanda ArUco")
//                    isScannerActive = true

                    val intent = Intent(context, CameraActivity::class.java)
                    cameraLauncher.launch(intent)
                },
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
                        text = "Beranda",
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
                    text = "Pindai ArUco",
                    onClick = onScanClick,
                    contentDescription = "Pindai Aruco",
                    isQrScanned = true  // Scan QR button should always be enabled
                )

                Spacer(modifier = Modifier.height(24.dp))

                MainMenuButton(
                    text = "Pilih Lantai",
                    onClick = onSelectFloorClick,
                    contentDescription = "Pilih Lantai",
                    isQrScanned = isQrScanned  // This button should be disabled until QR is scanned
                )
            }
        }
    }
}

@Suppress("KotlinConstantConditions")
@Composable
fun MainMenuButton(
    text: String,
    onClick: () -> Unit,
    contentDescription: String,
    isQrScanned: Boolean
) {
    val isEnabled = when (text) {
        "Pilih Lantai" -> isQrScanned
        else -> true
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .semantics {
                this.contentDescription = if (!isEnabled && text == "Pilih Lantai") {
                    "Tombol pilih lantai nonaktif. Pindai ArUco terlebih dahulu"
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
        isQrScanned = false
    )

}

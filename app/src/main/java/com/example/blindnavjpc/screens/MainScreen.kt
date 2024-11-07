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
import com.example.blindnavjpc.dataconnection.ApiService
import com.example.blindnavjpc.helpers.SearchBarHelper
import com.example.blindnavjpc.helpers.TTSManager
import kotlinx.coroutines.launch
import com.example.blindnavjpc.dataconnection.NavigationState
import com.example.blindnavjpc.CameraActivity



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
    var isScannerActive by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var isNavigationMode by remember { mutableStateOf(false) }


    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Get the marker ID from the result data
            val markerId = result.data?.getStringExtra("MARKER_ID")
            markerId?.let {
                currentArucoID = markerId.toInt()
            }
            val distance = result.data?.getStringExtra("DISTANCE")
            distance?.let {
                currentDistance = distance.toFloat()*100
            }
            val angle = result.data?.getStringExtra("ANGLE")
            angle?.let {
                currentAngle = angle.toFloat()
            }
            onDistanceAngleUpdated(currentArucoID,currentDistance,currentAngle)

            TTSManager.speak("Scanned Marker ID: ${currentArucoID}")
//            TTSManager.speak("Scanned distance: ${currentDistance}")
//            TTSManager.speak("Scanned current Angle: ${currentAngle}")
            if (isNavigationMode == false){
                currentScreen = "main"
                isQrScanned = true
            }
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val searchBarHelper = remember {
        SearchBarHelper(
            context = context,
            apiService = apiService,
            coroutineScope = coroutineScope
        )
    }

//    var distance by remember { mutableStateOf(0f) }
//    var angle by remember { mutableStateOf(0f) }

//    fun setupListeners(cameraActivity: CameraActivity) {
//        cameraActivity.onPositionUpdate = { x, y ->
//            println("Position updated: x=$x, y=$y")
//            TTSManager.speak("Position di update")
//            distance = x
//            angle = y
//            onDistanceAngleUpdated(currentArucoID,distance,angle)
//        }

//        cameraActivity.onIdUpdate = { id ->
//            println("ID updated: id=$id")
//            TTSManager.speak("id di update")
//            currentArucoID = id
//            isQrScanned = true // Update the state after scanning
//        }
//    }
    // Callback for handling marker detection
//    val onMarkerDetectedCallback: (Int, Float, Float) -> Unit = { id, newDistance, newAngle ->
//        currentArucoID = id
//        distance = newDistance
//        angle = newAngle
//        isQrScanned = true // Update state indicating that a QR code has been scanned
//        onDistanceAngleUpdated(id, newDistance, newAngle)
//    }
//    // Define the onPositionUpdate function
//    val onPositionUpdate: (Float, Float) -> Unit = { newDistance, newAngle ->
//        distance = newDistance
//        angle = newAngle
//        onDistanceAngleUpdated(currentArucoID, distance, angle)
//        // Do additional processing if needed
//    }

//    val cameraActivity = CameraActivity()  // Or however you get the instance
//    setupListeners(cameraActivity)
    // Set the onPositionUpdate callback in the CameraActivity
//    val cameraActivity = LocalContext.current.findViewTreeCameraActivity() ?: return
//    cameraActivity.setOnPositionUpdateCallback(onPositionUpdate)
//    fun startScannerActivity() {
//        val intent = Intent(context, CameraActivity::class.java).apply {
//            // Pass the listener callback to CameraActivity
//            (context as? Activity)?.let { activity ->
//                (activity as CameraActivity).onMarkerDetected = onMarkerDetectedCallback
//            }
//        }
//        context.startActivity(intent)
//    }

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

    LaunchedEffect(currentScreen) {
        if (currentScreen == "main") {
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
                    isNavigationMode = false
                    isScannerActive = true

                    val intent = Intent(context, CameraActivity::class.java)
                    cameraLauncher.launch(intent)

                    if (isQrScanned == true){
                        isScannerActive = false
                    }


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
                    selectedRoomID = room.id
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
                onDiscardClick = { currentScreen = "roomSelection" },
                onBackToHomeClick = { currentScreen = "main" },
                onScanClick = {
                    isNavigationMode = true
                    TTSManager.speak("Memulai pemindaian ArUco marker")
                    isScannerActive = true

                    val intent = Intent(context, CameraActivity::class.java)
                    cameraLauncher.launch(intent)

//                    if (isQrScanned == true){
//                        TTSManager.speak("Masuk ke proses is qr scanned true")
////                        currentScreen = "main"
////                        isScannerActive = false
//                    }
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

@Suppress("KotlinConstantConditions")
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

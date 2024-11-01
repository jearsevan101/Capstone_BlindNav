package com.example.blindnavjpc

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.blindnavjpc.dataconnection.ApiService
import com.example.blindnavjpc.dataconnection.NavigationService
import com.example.blindnavjpc.dataconnection.NavigationUpdate
import com.example.blindnavjpc.dataconnection.RetrofitClient
import com.example.blindnavjpc.dataconnection.RetrofitClient.apiService
import com.example.blindnavjpc.helpers.ScannerHelper
import com.example.blindnavjpc.helpers.TTSManager
import com.example.blindnavjpc.screens.LaunchScreen
import com.example.blindnavjpc.screens.MainScreen
import com.example.blindnavjpc.ui.theme.BlindNavJPCTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var scannerHelper: ScannerHelper
    private lateinit var navigationService: NavigationService
    private var currentDestination: Int? = null
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TTSManager.initialize(this)

        // Initialize the scanner helper and navigation service
        scannerHelper = ScannerHelper(this)
        navigationService = NavigationService(lifecycleScope)

        // Configure scanner to handle marker detection
        scannerHelper.setOnMarkerDetectedListener { markerId ->
            handleMarkerDetection(markerId)
        }

        scannerHelper.installGoogleScanner()

        // Start collecting navigation state updates
        lifecycleScope.launch {
            navigationService.navigationState.collect { state ->
                // Announce navigation updates via TTS
                if (state.direction.isNotEmpty()) {
                    TTSManager.speak(state.direction)
                }
                if (state.distance.isNotEmpty()) {
                    TTSManager.speak(state.distance)
                }
                state.error?.let { error ->
                    TTSManager.speak("Error: $error")
                }
            }
        }

        setContent {
            BlindNavJPCTheme {
                var showLaunchScreen by remember { mutableStateOf(true) }
                val navigationState by navigationService.navigationState.collectAsState()

                if (showLaunchScreen) {
                    LaunchScreen(
                        onNavigateToMain = {
                            showLaunchScreen = false
                        }
                    )
                } else {
                    MainScreen(
                        scannerHelper = scannerHelper,
                        navigationState = navigationState,
                        apiService = apiService,
                        onDestinationSelected = { destinationId ->
                            currentDestination = destinationId
                            scannerHelper.lastDetectedMarker?.let { currentId ->
                                startNavigation(currentId, destinationId)
                            }
                        },
                        onDistanceAngleUpdated = onDistanceAngleUpdated
                    )
                }
            }
        }
    }
    val onDistanceAngleUpdated: (Int, Float, Float) -> Unit = { newCurrentId, newDistance, newAngle ->
        updatePositionInfo(newCurrentId,newDistance,newAngle)
        // Do additional processing if needed
    }
    private fun handleMarkerDetection(markerId: Int) {
        lifecycleScope.launch {
            if (navigationService.isNavigating()) {
                navigationService.updateLocation(markerId)
                // If navigation is complete, go back to main screen
                if (!navigationService.isNavigating()) {
                    TTSManager.speak("Anda telah tiba di lokasi tujuan")
                    // Reset navigation state
                    currentDestination = null
                }
            } else {
                currentDestination?.let { destinationId ->
                    startNavigation(markerId, destinationId)
                }
            }
        }
    }

    private fun startNavigation(fromId: Int, toId: Int) {
        lifecycleScope.launch {
            val success = navigationService.startNavigation(fromId, toId)
            if (!success) {
                TTSManager.speak("Gagal untuk memulai navigasi")
                currentDestination = null
            }
        }
    }
    private fun startContinuousScanning() {
        scannerHelper.setScanMode(ScannerHelper.ScanMode.CONTINUOUS)
        scannerHelper.startScanning(
            onSuccess = { /* Handle in marker detection listener */ },
            onError = { TTSManager.speak("Gagal memindai marker") },
            onCancelled = { /* Scanner was cancelled */ }
        )
    }
    // Call this method when you receive distance and angle updates
    private fun updatePositionInfo(markerId: Int, distance: Float, angle: Float) {
        lifecycleScope.launch {
            navigationService.updatePositionInfo(
                NavigationUpdate(
                    currentMarkerId = markerId,
                    distance = distance,
                    angle = angle
                )
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        scannerHelper.cleanup()
    }
}
package com.example.blindnavjpc

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.blindnavjpc.helpers.ScannerHelper
import com.example.blindnavjpc.helpers.TTSManager
import com.example.blindnavjpc.screens.LaunchScreen
import com.example.blindnavjpc.screens.MainScreen
import com.example.blindnavjpc.ui.theme.BlindNavJPCTheme

class MainActivity : ComponentActivity() {
    private lateinit var scannerHelper: ScannerHelper

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TTSManager.initialize(this)

        // Initialize the scanner helper
        scannerHelper = ScannerHelper(this)
        scannerHelper.installGoogleScanner()

        setContent {
            BlindNavJPCTheme {
                var showLaunchScreen by remember { mutableStateOf(true) }

                if (showLaunchScreen) {
                    LaunchScreen(
                        onNavigateToMain = {
                            showLaunchScreen = false
                        }
                    )
                } else {
                    MainScreen(scannerHelper)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerHelper.cleanup()
    }
}
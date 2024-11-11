package com.example.blindnavjpc.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blindnavjpc.R
import com.example.blindnavjpc.helpers.TTSManager
import com.example.blindnavjpc.ui.theme.BlindNavJPCTheme
import com.example.blindnavjpc.ui.theme.fontFamily
import kotlinx.coroutines.time.delay
import java.time.Duration

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchScreen(
    onNavigateToMain: () -> Unit
) {
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
            TTSManager.speak("Selamat datang di BlindNav. Aplikasi ini akan membantu anda menavigasi gedung dengan mudah. Tekan tombol di tengah layar untuk memulai.")
            {
                // Aktifkan kembali TalkBack setelah TTS selesai
                isTalkBackEnabled = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "BlindNav",
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
                Button(
                    onClick = {
                        // Matikan TalkBack sementara
                        isTalkBackEnabled = false
                        TTSManager.speak("Memulai aplikasi") {
                            // Aktifkan kembali TalkBack setelah TTS selesai
                            isTalkBackEnabled = true
                        }
                        onNavigateToMain()
                    },
                    modifier = Modifier
                        .size(250.dp)  // Ukuran tetap untuk membuat lingkaran sempurna
                        .semantics {
                            contentDescription = "Mulai aplikasi BlindNav"
                        },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.secondary),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Mulai",
                        fontSize = 32.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@SuppressLint("NewApi")
@Preview(showBackground = true)
@Composable
fun LaunchScreenPreview() {
    BlindNavJPCTheme {
        LaunchScreen(
            onNavigateToMain = { /* Preview callback */ }
        )
    }
}
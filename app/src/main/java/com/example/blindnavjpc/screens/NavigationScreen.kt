package com.example.blindnavjpc.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blindnavjpc.R
import com.example.blindnavjpc.dataconnection.NavigationState
import com.example.blindnavjpc.helpers.TTSManager
import com.example.blindnavjpc.ui.theme.fontFamily
import kotlinx.coroutines.time.delay
import java.time.Duration
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(navigationState: NavigationState,
                     onBackToHomeClick: () -> Unit,
                     onDiscardClick: () -> Unit,
                     onScanClick: () -> Unit) {
    LaunchedEffect(Unit) {
        TTSManager.speak(
            "Navigasi akan dimulai. Scanner akan terus aktif untuk mendeteksi marker. " +
                    "Anda dapat kembali ke layar sebelumnya atau ke layar awal dengan menekan tombol 'kembali' di kiri bawah dan 'home' di kanan bawah. "
        )
        // Start continuous scanning immediately
        delay(Duration.ofMillis(10000))
        onScanClick()
    }

    LaunchedEffect(navigationState) {
        if (navigationState.direction.isNotEmpty()) {
            TTSManager.speak("Silahkan maju ke arah " + navigationState.direction)
        }
        if (navigationState.distance.isNotEmpty()) {
            TTSManager.speak(navigationState.distance)
        }
        navigationState.error?.let { error ->
            TTSManager.speak("Error: $error")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mulai Navigasi",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = fontFamily
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.primary),
                    titleContentColor = Color.White
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                Button(
                    onClick = {
                        onScanClick.invoke()
                        // Logic untuk memindai ArUco dapat ditambahkan di sini
                    },
                    modifier = Modifier
                        .size(150.dp) // Make the button larger
                        .semantics { contentDescription = "Scan ArUco Marker" }, // Add content description for TalkBack
                    shape = CircleShape, // Circular button
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.secondary),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_navigation_24), // Use a navigation icon
                        contentDescription = null, // No content description for icon
                        modifier = Modifier.size(60.dp) // Make the icon bigger to match the larger button
                    )
                }

//                if (navigationState.isNavigating) {
//                    Text(
//                        text = navigationState.currentLocation,
//                        modifier = Modifier.padding(16.dp),
//                        textAlign = TextAlign.Center
//                    )
//                    Text(
//                        text = navigationState.nextMarker,
//                        modifier = Modifier.padding(16.dp),
//                        textAlign = TextAlign.Center
//                    )
//                    Text(
//                        text = "Arah: ${navigationState.direction}",
//                        modifier = Modifier.padding(16.dp),
//                        textAlign = TextAlign.Center
//                    )
//                    Text(
//                        text = "Jarak: ${navigationState.distance}",
//                        modifier = Modifier.padding(16.dp),
//                        textAlign = TextAlign.Center
//                    )
//                }
            }

            // Bottom navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onDiscardClick,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .semantics { contentDescription = "Kembali ke layar sebelumnya" },
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.tertiary),
                        contentColor = Color.White
                    )
                ) {
                    Text("Kembali")
                }

                Button(
                    onClick = onBackToHomeClick,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .semantics { contentDescription = "Kembali ke tampilan awal" },
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.tertiary),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_home_24),
                        contentDescription = "Home",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewNavigationScreen() {
    NavigationScreen(
        navigationState = NavigationState(),
        onDiscardClick = {},
        onBackToHomeClick = {},
        onScanClick = {}
    )
}
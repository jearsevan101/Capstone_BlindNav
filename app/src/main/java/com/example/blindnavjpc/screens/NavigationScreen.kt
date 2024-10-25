package com.example.blindnavjpc.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blindnavjpc.R
import com.example.blindnavjpc.helpers.TTSManager
import com.example.blindnavjpc.ui.theme.fontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(onBackToHomeClick: () -> Unit, onDiscardClick: () -> Unit) {
    // Voice guidance when screen is shown
    LaunchedEffect(Unit) {
        TTSManager.speak(
            "Navigasi akan dimulai. Silakan tekan tombol navigasi untuk memulai scan ArUco. " +
                    "Untuk kembali ke layar sebelumnya, silakan tekan tombol 'kembali' di kiri bawah. " +
                    "Untuk kembali ke layar awal, silakan klik tombol 'home' di sebelah kanan bawah."
        )
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
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Center the Navigation Button
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center // Center vertically
                ) {
                    Button(
                        onClick = {
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
                }

                // Row for the left and right buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 16 .dp, end = 16 .dp)
                        .align(Alignment.BottomCenter), // Align the buttons at the bottom
                    horizontalArrangement = Arrangement.SpaceBetween // Put buttons on the far left and right
                ) {
                    // Button "Kembali" - Left side
                    Button(
                        onClick = onDiscardClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp) // Small padding to the right
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

                    // Button with Home Icon - Right side
                    Button(
                        onClick = onBackToHomeClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp) // Small padding to the left
                            .semantics { contentDescription = "Kembali ke tampilan awal" },
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.tertiary),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_home_24), // Icon home
                            contentDescription = "Home", // Content description for accessibility
                            modifier = Modifier.size(28.dp) // Adjust icon size
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewNavigationScreen() {
    NavigationScreen(
        onDiscardClick = {},
        onBackToHomeClick = {}
    )
}

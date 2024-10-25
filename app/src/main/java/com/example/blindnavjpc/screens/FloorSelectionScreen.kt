package com.example.blindnavjpc.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.blindnavjpc.ui.theme.fontFamily
import androidx.compose.ui.res.colorResource
import com.example.blindnavjpc.R
import androidx.compose.ui.text.style.TextAlign
import com.example.blindnavjpc.helpers.TTSManager

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

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorSelectionScreen(
    onFloorSelected: (Int) -> Unit,
    onDiscardClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        TTSManager.speak("Silakan pilih lantai yang ingin anda tuju.")
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
            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Using a range to create floor buttons dynamically
                (1..3).forEach { floor ->
                    FloorButton(
                        floorNumber = floor,
                        onClick = onFloorSelected
                    )
                }
            }

            BackButton(
                onClick = onDiscardClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFloorSelectionScreen() {
    FloorSelectionScreen(
        onFloorSelected = {},
        onDiscardClick = {}
    )
}
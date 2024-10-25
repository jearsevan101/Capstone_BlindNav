package com.example.blindnavjpc.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.blindnavjpc.ui.theme.fontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    floor: Int,
    onCategorySelected: (String) -> Unit = {},
    onDiscardClick: () -> Unit = {}
) {
    val categories = getCategoriesForFloor(floor)
    // Voice guidance when screen is shown
    LaunchedEffect(Unit) {
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
fun CategoryButton(category: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, start = 32.dp, end = 32.dp)
            .height(80.dp)
            .semantics { contentDescription = "Pilih kategori $category" },
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.secondary),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Text(
            text = category,
            fontSize = 18.sp,
            fontFamily = fontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun getCategoriesForFloor(floor: Int): List<String> {
    return when (floor) {
        1 -> listOf("Laboratorium", "Ruang Kelas", "Ruang Kantor dan Staff", "Fasilitas Lain")
        2 -> listOf("Laboratorium", "Ruang Kelas", "Ruang Kantor dan Staff", "Fasilitas Lain")
        3 -> listOf("Laboratorium", "Ruang Kelas", "Ruang Kantor dan Staff", "Fasilitas Lain")
        else -> emptyList()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCategorySelectionScreen() {
    CategorySelectionScreen(
        floor = 1,
        onCategorySelected = {},
        onDiscardClick = {}
    )
}
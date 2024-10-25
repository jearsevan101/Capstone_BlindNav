package com.example.blindnavjpc.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.blindnavjpc.ui.theme.fontFamily
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import com.example.blindnavjpc.helpers.TTSManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomSelectionScreen(
    floor: Int,
    category: String,
    onRoomSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {

    // Voice guidance when screen is shown
    LaunchedEffect(Unit) {
        TTSManager.speak("Silakan pilih ruangan pada kategori $category di lantai $floor.")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pilih Ruang - Lantai $floor",
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
                    .fillMaxSize()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text(
                    text = "Kategori: $category",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp, top= 32.dp),
                    textAlign = TextAlign.Center
                )

                // LazyColumn untuk daftar ruangan
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 80.dp), // Beri jarak di bagian bawah agar tidak menutupi tombol "Kembali"
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val rooms = getRoomsForFloorAndCategory(floor, category)
                    items(rooms) { room ->
                        RoomButton(room, onRoomSelected)
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
            Button(
                onClick = onBackClick,
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
fun RoomButton(room: String, onRoomSelected: (String) -> Unit) {
    Button(
        onClick = { onRoomSelected(room) },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .semantics { contentDescription = "Pilih Ruangan $room" },
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.secondary),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Text(
            text = room.substringBefore(':'),
            fontSize = 24.sp,
            fontFamily = fontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            lineHeight = 24.sp
        )
    }
}

fun getRoomsForFloorAndCategory(floor: Int, category: String): List<String> {
    return when (floor) {
        1 -> when (category) {
            "Laboratorium" -> listOf(
                "N103: N103 Common Room",
                "N104: N104 Lab Teknik Tenaga Listrik 2",
                "N106: N106 Electrical Power System & Installation Facility",
                "N106 A: N106 A",
                "N107: N107 Lab Teknik Tenaga Listrik 1 - Doctoral Residency",
                "S105: S105 High Voltage Facility",
                "S108: S108 Transmission and Distribution Facility"
            )
            "Ruang Kelas"-> listOf(
                "S104: S104 Teleconference Room",
                "S107: S107",
            )
            "Ruang Kantor dan Staff" -> listOf(
                "N101: N101 UGM AI Centre",
                "N102: N102 Honeywell",
                "N108: N108 Alumni",
                "N109: N109 Human Resource & Finance",
                "N109 A: N109 A",
                "S101 AB: S101 AB Academic Affairs & Head of Department",
                "S102: S102 Meeting Room",
                "S103 AB: S103 AB Board of Directors",
                "S110: S110 Meeting Room",
                "S105 A: S105 A"
            )
            "Fasilitas Lain" -> listOf(
                "S106: S106 Lecturers Transit Room",
                "Mushola",
                "N105 A: N105 A Toilet Wanita",
                "N105 B: N105 B Toilet Pria",
                "S109 A: S109 A Toilet Pria",
                "S109 B: S109 A Toilet Wanita"

            )
            else -> emptyList()
        }
        2 -> when (category) {
            "Laboratorium" -> listOf(
                "S206: S206 Informatics and Computer Facility (Lab IF)",
                "S203: S203 Schneider Electric Training Center"
            )
            "Ruang Kelas" -> listOf(
                "S201 C: S201 C",
                "S201 D: S201 D",
                "S201 E: S201 E",
                "S204: S204",
                "S205: S205",
                "S210: S210",
                "S211: S211"
            )
            "Ruang Kantor dan Staff" -> listOf(
                "S202: S202 IT, Network & Servers",
                "S201: S201 Student and Internal Affairs (Akademik)",
                "S207: S207 Cisco Networking Academy (Microsoft Innovation Centre)"
            )
            "Fasilitas Lain" -> listOf(
                "S209: S209 Toilet",
                "N204 A: N204 A Toilet Pria",
                "N204 B: N204 B Toilet Wanita"
            )
            else -> emptyList()
        }
        3 -> when (category) {
            "Laboratorium" -> listOf(
                "N301: N301 Telecommunication and High Frequency System Facility",
                "N301 B: N301 B",
                "N303: N303 Biomedical Engineering Facility (Lab Dasar 3 Pintu Timur)",
                "N304: N304 Lab Dasar 3 Fasilitas Pengolahan Isyarat",
                "N305 A: N305 A",
                "N305 B: N305 B",
                "N306: N306 Lab Dasar 2 Pintu Barat",
                "S302: S302 Lab Jaringan Komputer dan Aplikasi Terdistribusi (Doctoral Residency)",
                "S301 A: S301 A Fasilitas Sistem Digital",
                "S301: S301 Digital System Facility - Doctoral Residency"
            )
            "Ruang Kelas" -> listOf(
                "S104: S104 Ruang Kelas E6",
                "S303: S303 Ruang Kelas E5",
                "S305: S305 Electronic Systems Facility",
                "S305 B: S305 B",
                "S307 A: S307 Lab Dasar 1 Pintu 1",
                "S307 B: S307 Lab Dasar 1 Pintu 2",
                "S307 C: S307 Lab Dasar 1 Pintu 3",

            )
            "Ruang Kantor dan Staff" -> listOf(
                "N308: N308 Ruang Laboran"
            )
            "Fasilitas Lain" -> listOf(
                "N302 A: N302　A Toilet Pria",
                "N302 B: N302　B Toilet Wanita",
                "S306 A: S306 A Toilet Pria",
                "S306 B: S306 B Toilet Wanita"
            )
            else -> emptyList()
        }
        else -> emptyList()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRoomSelectionScreen() {
    RoomSelectionScreen(
        floor = 1,
        category = "Laboratorium",
        onRoomSelected = {},
        onBackClick = {}
    )
}

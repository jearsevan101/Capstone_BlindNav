package com.example.blindnavjpc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.GoogleFont.Provider
import com.example.blindnavjpc.R

// Define the font provider for Google Fonts
val provider = Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)


// Define the custom font name and family
val fontName = GoogleFont("Fredoka")

val fontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider)
)

// Set of Material typography styles to start with
val MyTypography = Typography(
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        fontSize = 16.sp // Example size
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp // Example size
    )
    // Add additional styles as needed...
)

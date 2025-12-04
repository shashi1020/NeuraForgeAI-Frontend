package com.ai.neuraforge.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

// Modern Teal Palette
private val PrimaryTeal = Color(0xFF00897B)   // Deep Teal
private val OnPrimaryWhite = Color.White
private val SecondaryAccent = Color(0xFF26C6DA) // Brighter Cyan for accents
private val BackgroundLightGray = Color(0xFFF0F0F0) // Lighter background
private val SurfaceWhite = Color.White // White surface for cards/bubbles
private val OnBackgroundDark = Color(0xFF1F1F1F) // Dark text for contrast

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryTeal.copy(alpha = 0.1f), // Light wash for app bar
    onPrimaryContainer = PrimaryTeal.copy(red = 0.1f, green = 0.1f, blue = 0.1f),
    secondary = SecondaryAccent,
    secondaryContainer = SecondaryAccent.copy(alpha = 0.2f),
    background = BackgroundLightGray,
    onBackground = OnBackgroundDark,
    surface = SurfaceWhite,
    onSurface = OnBackgroundDark,
    surfaceVariant = Color(0xFFE0E0E0), // AI bubble background
    onSurfaceVariant = OnBackgroundDark.copy(alpha = 0.8f)
)

private val AppTypography = Typography(
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp, // Slightly bigger chat text
        lineHeight = 20.sp
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)

@Composable
fun PdfAiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
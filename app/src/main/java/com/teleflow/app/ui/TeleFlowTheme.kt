package com.teleflow.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F5F75),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCBEAF7),
    onPrimaryContainer = Color(0xFF001E28),
    secondary = Color(0xFF4B626B),
    secondaryContainer = Color(0xFFCDE7F0),
    surface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFFE0E3E4)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF97D2EA),
    onPrimary = Color(0xFF003545),
    primaryContainer = Color(0xFF174A5C),
    onPrimaryContainer = Color(0xFFCBEAF7),
    secondary = Color(0xFFB2CBD4),
    secondaryContainer = Color(0xFF334A53),
    surface = Color(0xFF121415),
    surfaceVariant = Color(0xFF40484B)
)

@Composable
fun TeleFlowTheme(darkMode: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkMode) DarkColors else LightColors, content = content)
}

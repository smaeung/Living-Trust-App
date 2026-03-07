package com.livingtrust.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A365D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF4299E1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEBF8FF),
    background = Color(0xFFF5F7FA),
    surface = Color.White,
    onBackground = Color(0xFF1A202C),
    onSurface = Color(0xFF2D3748),
    onSurfaceVariant = Color(0xFF718096),
    error = Color(0xFFE53E3E)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4299E1),
    onPrimary = Color(0xFF1A365D),
    primaryContainer = Color(0xFF1A365D),
    secondary = Color(0xFF63B3ED),
    background = Color(0xFF1A202C),
    surface = Color(0xFF2D3748),
    onBackground = Color(0xFFF7FAFC),
    onSurface = Color(0xFFEDF2F7)
)

@Composable
fun LivingTrustTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

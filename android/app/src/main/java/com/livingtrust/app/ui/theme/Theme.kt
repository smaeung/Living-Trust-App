package com.livingtrust.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Material Design 3 color scheme for light mode.
 *
 * WHY define custom colors instead of using Material3 defaults?
 * - The default Material3 colors are purple-based. A legal/financial app should
 *   convey trust, professionalism, and stability — qualities associated with navy blue.
 * - Custom colors ensure brand consistency across all screens without individual
 *   color overrides in each Composable.
 *
 * WHY use `lightColorScheme()` / `darkColorScheme()` functions?
 * - These Material3 factory functions provide defaults for ALL color slots
 *   (30+ colors like errorContainer, tertiaryContainer, etc.).
 *   We only override the colors we care about; the rest get sensible Material defaults.
 *
 * Color naming explained (Material Design 3 terminology):
 * - primary: main brand color — used for buttons, selected states, FABs
 * - onPrimary: color of content (text/icons) ON TOP OF the primary color
 * - primaryContainer: lighter version of primary — used for backgrounds of selected items
 * - secondary: accent color — used for chips, links, secondary actions
 * - background: the screen background color
 * - surface: card and sheet background color
 * - onBackground/onSurface: text color on top of background/surface
 * - onSurfaceVariant: muted text (hints, labels, secondary info)
 * - error: red for error states
 *
 * Color choices for the Living Trust App:
 * - primary (0xFF1A365D): deep navy blue — trustworthy, professional, legal feel
 * - secondary (0xFF4299E1): medium blue — friendly, digital, complements navy
 * - background (0xFFF5F7FA): very light blue-gray — warm, not harsh white
 * - error (0xFFE53E3E): standard red — universally understood for errors
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A365D),            // deep navy blue (brand primary)
    onPrimary = Color.White,                // white text on navy buttons
    primaryContainer = Color(0xFFD1E4FF),   // light blue for selected states
    secondary = Color(0xFF4299E1),          // medium blue for accents
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEBF8FF), // very light blue background
    background = Color(0xFFF5F7FA),         // off-white screen background
    surface = Color.White,                  // card/sheet background
    onBackground = Color(0xFF1A202C),       // near-black text on background
    onSurface = Color(0xFF2D3748),          // dark gray text on cards
    onSurfaceVariant = Color(0xFF718096),   // medium gray for hints/labels
    error = Color(0xFFE53E3E)              // red for error messages
)

/**
 * Dark mode color scheme.
 *
 * WHY not just auto-generate dark colors from light colors?
 * - Material3 does not automatically invert colors — you control the dark palette.
 * - In dark mode, the primary color lightens (navy → medium blue) so it remains
 *   visible against dark backgrounds. The "on" colors invert accordingly.
 *
 * WHY `darkTheme: Boolean = false` in LivingTrustTheme (not system-dynamic)?
 * - For v1, dark mode is off by default. The system dark mode setting is not
 *   connected yet. A future v2 could use `isSystemInDarkTheme()` from Compose.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4299E1),            // medium blue (more visible on dark background)
    onPrimary = Color(0xFF1A365D),          // dark navy text on blue buttons
    primaryContainer = Color(0xFF1A365D),   // navy container
    secondary = Color(0xFF63B3ED),          // lighter blue for dark mode accents
    background = Color(0xFF1A202C),         // very dark navy background
    surface = Color(0xFF2D3748),            // dark slate for cards/sheets
    onBackground = Color(0xFFF7FAFC),       // near-white text on dark background
    onSurface = Color(0xFFEDF2F7)          // light gray text on dark cards
)

/**
 * The app's root theme composable — wrap all screens in this to apply the design system.
 *
 * WHY a custom theme function instead of MaterialTheme directly?
 * - LivingTrustTheme is our branded wrapper. All screens call LivingTrustTheme,
 *   not MaterialTheme. If we want to add custom typography, shapes, or color tokens
 *   in the future, we change only this file — no changes needed in individual screens.
 *
 * WHY `content: @Composable () -> Unit`?
 * - This is the slot API pattern in Compose. The theme wraps around any Composable
 *   content passed to it. MainActivity passes the entire NavGraph as `content`.
 *
 * Usage:
 * ```kotlin
 * LivingTrustTheme {
 *     NavGraph()
 * }
 * ```
 */
@Composable
fun LivingTrustTheme(
    darkTheme: Boolean = false,       // toggle dark/light mode (v1: always light)
    content: @Composable () -> Unit   // the screen content to theme
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // MaterialTheme is the Material Design 3 system provider.
    // It makes colorScheme and typography available to all descendant Composables
    // via MaterialTheme.colorScheme and MaterialTheme.typography.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,  // defined in Typography.kt
        content = content
    )
}

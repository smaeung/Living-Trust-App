package com.livingtrust.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography (text styles) for the Living Trust App.
 *
 * WHY define custom Typography?
 * - Material Design 3 has a full type scale (displayLarge, headlineMedium, bodySmall, etc.).
 *   Like colors, the Typography() constructor accepts overrides for specific roles
 *   while providing sensible defaults for all others.
 * - Centralizing text styles here ensures consistency — no magic numbers scattered across
 *   individual Composables. If the designer changes bodyLarge to 15.sp, it updates everywhere.
 *
 * WHY FontFamily.Default?
 * - Default uses the system font (Roboto on Android). This is the safe choice for v1:
 *   no custom font files to bundle, smaller APK, and follows Android conventions.
 * - A future version could load a custom font (e.g., from Google Fonts with Coil or bundled).
 *
 * Material3 Typography roles used here:
 * - bodyLarge: main body text — paragraphs, list items, form field content
 * - titleLarge: screen titles and section headers
 * - labelSmall: the smallest text — metadata, badges, timestamps, helper text
 *
 * WHY only define 3 out of ~15 available type roles?
 * - The Typography() constructor provides Material3 defaults for all other roles.
 *   We only override the ones that matter for our specific design language.
 *   Over-specifying every role would add noise without value at this stage.
 *
 * Text size reference (for context):
 * - 11.sp: very small — used for metadata, fine print
 * - 16.sp: normal reading size — comfortable for body content
 * - 22.sp: large heading — prominent but not oversized
 *
 * WHY `sp` (scalable pixels) instead of `dp` for text?
 * - sp units respect the user's system font size preference (Accessibility settings).
 *   Users who set "Large font" in Android settings will see text scale proportionally.
 *   Using dp would ignore this preference and break accessibility.
 */
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,     // 1.5x line height — comfortable for reading long text
        letterSpacing = 0.5.sp  // slight tracking for readability at body size
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,     // tighter line height for headings (they're shorter blocks)
        letterSpacing = 0.sp    // no extra tracking — bold headings read well without it
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // medium weight keeps small text legible
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp  // wider tracking improves readability at very small sizes
    )
)

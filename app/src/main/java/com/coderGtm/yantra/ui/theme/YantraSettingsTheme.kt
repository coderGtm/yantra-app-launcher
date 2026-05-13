package com.coderGtm.yantra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Yantra settings color scheme – terminal monochrome.
 *
 * Pure black/gray palette with no chromatic accent, matching a real terminal:
 *   - Background        : near-black canvas
 *   - surfaceContainerLow : dark-gray group cards
 *   - primary           : light gray  → section headers, action labels
 *   - onSurface         : off-white   → primary text
 *   - onSurfaceVariant  : mid-gray    → description / secondary text
 *   - outline           : dark-gray   → dividers
 */
private val YantraSettingsColorScheme = darkColorScheme(
    primary                = Color(0xFFCCCCCC),   // light gray – headers, action labels
    onPrimary              = Color(0xFF111111),
    primaryContainer       = Color(0xFF2A2A2A),
    onPrimaryContainer     = Color(0xFFE8E8E8),
    secondary              = Color(0xFFAAAAAA),
    onSecondary            = Color(0xFF1A1A1A),
    secondaryContainer     = Color(0xFF242424),
    onSecondaryContainer   = Color(0xFFDDDDDD),
    background             = Color(0xFF0A0A0A),   // near-black canvas
    onBackground           = Color(0xFFE4E4E4),
    surface                = Color(0xFF0A0A0A),
    onSurface              = Color(0xFFE4E4E4),   // primary text
    surfaceVariant         = Color(0xFF1C1C1C),
    onSurfaceVariant       = Color(0xFF888888),   // secondary / description text
    surfaceContainerLowest = Color(0xFF060606),
    surfaceContainerLow    = Color(0xFF141414),   // group card background
    surfaceContainer       = Color(0xFF1A1A1A),
    surfaceContainerHigh   = Color(0xFF202020),
    surfaceContainerHighest= Color(0xFF262626),
    outline                = Color(0xFF2A2A2A),   // dividers
    outlineVariant         = Color(0xFF1E1E1E),
)

@Composable
fun YantraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YantraSettingsColorScheme,
        content     = content
    )
}

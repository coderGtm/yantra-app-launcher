package com.coderGtm.yantra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Yantra settings color scheme – cool charcoal terminal.
 *
 * Slightly blue-tinted grays on rich black — like a premium terminal emulator.
 * No color, just personality through tone:
 *   - Background           : rich black (#0D0D0D)
 *   - surfaceContainerLow  : lifted charcoal (#181820) for group cards
 *   - primary              : silver-blue (#A8A8B8) → headers, action labels, switch ON
 *   - onSurface            : cool off-white (#E8E8F0) → primary text
 *   - onSurfaceVariant     : muted slate (#606070) → descriptions / values
 *   - outline              : dark slate (#26262E) → dividers
 */

// Accent
val CharcoalSilver       = Color(0xFFA8A8B8)   // slightly blue-tinted silver
val CharcoalOnPrimary    = Color(0xFF0D0D12)   // near-black for text on silver bg

// Surfaces
val CharcoalBlack        = Color(0xFF0D0D0D)   // rich black canvas
val CharcoalCard         = Color(0xFF181820)   // cool dark card
val CharcoalSurface2     = Color(0xFF1E1E28)
val CharcoalSurface3     = Color(0xFF23232E)

// Text
val CharcoalText         = Color(0xFFE8E8F0)   // cool off-white
val CharcoalTextMuted    = Color(0xFF606070)   // slate for descriptions

// Borders / dividers
val CharcoalDivider      = Color(0xFF26262E)
val CharcoalDivider2     = Color(0xFF30303C)   // switch track off-state

private val YantraSettingsColorScheme = darkColorScheme(
    primary                = CharcoalSilver,
    onPrimary              = CharcoalOnPrimary,
    primaryContainer       = Color(0xFF2E2E3C),
    onPrimaryContainer     = Color(0xFFD8D8E8),
    secondary              = Color(0xFF8888A0),
    onSecondary            = Color(0xFF0D0D18),
    secondaryContainer     = Color(0xFF222230),
    onSecondaryContainer   = Color(0xFFCCCCDC),
    background             = CharcoalBlack,
    onBackground           = CharcoalText,
    surface                = CharcoalBlack,
    onSurface              = CharcoalText,
    surfaceVariant         = CharcoalSurface2,
    onSurfaceVariant       = CharcoalTextMuted,
    surfaceContainerLowest = Color(0xFF090909),
    surfaceContainerLow    = CharcoalCard,
    surfaceContainer       = CharcoalSurface2,
    surfaceContainerHigh   = CharcoalSurface3,
    surfaceContainerHighest= Color(0xFF28283A),
    outline                = CharcoalDivider,
    outlineVariant         = Color(0xFF1E1E26),
)

@Composable
fun YantraSettingsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YantraSettingsColorScheme,
        content     = content
    )
}
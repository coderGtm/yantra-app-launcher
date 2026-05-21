package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.settingsItem.OptionSetting
import com.coderGtm.yantra.ui.components.containers.SettingsGroup

@Composable
fun FontGroup(
    fontSizeText: String,
    onOpenFontSizeSetter: () -> Unit,
    isProUser: Boolean,
    fontName: String,
    onOpenFontSelector: () -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.font)) {
        OptionSetting(
            title   = stringResource(R.string.terminal_font_size),
            value   = fontSizeText,
            onClick = onOpenFontSizeSetter
        )
        if (isProUser) {
            HorizontalDivider()
            OptionSetting(
                title   = stringResource(R.string.terminal_font),
                value   = fontName,
                onClick = onOpenFontSelector
            )
        }
    }
}


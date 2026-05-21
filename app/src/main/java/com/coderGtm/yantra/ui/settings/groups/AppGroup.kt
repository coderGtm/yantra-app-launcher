package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.settingsItem.ButtonSetting
import com.coderGtm.yantra.ui.components.settingsItem.OptionSetting
import com.coderGtm.yantra.ui.components.containers.SettingsGroup

@Composable
fun AppGroup(
    localeDisplayName: String,
    onOpenLanguagePicker: () -> Unit,
    onOpenLauncherSelection: () -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.app)) {
        OptionSetting(
            title   = stringResource(R.string.app_language),
            value   = localeDisplayName,
            onClick = onOpenLanguagePicker
        )
        HorizontalDivider()
        ButtonSetting(
            title   = stringResource(R.string.select_default_launcher),
            onClick = onOpenLauncherSelection
        )
    }
}


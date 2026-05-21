package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.settingsItem.ButtonSetting
import com.coderGtm.yantra.ui.components.containers.SettingsGroup

/** Shown only for Pro users. Wrap in `if (isProUser)` at call site. */
@Composable
fun TermuxGroup(
    onOpenPathSelector: () -> Unit,
    onOpenWorkingDirSelector: () -> Unit,
    onOpenSessionActionSelector: () -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.termux)) {
        ButtonSetting(
            title   = stringResource(R.string.termux_command_path),
            onClick = onOpenPathSelector
        )
        HorizontalDivider()
        ButtonSetting(
            title   = stringResource(R.string.termux_command_working_directory),
            onClick = onOpenWorkingDirSelector
        )
        HorizontalDivider()
        ButtonSetting(
            title   = stringResource(R.string.termux_command_session_action),
            onClick = onOpenSessionActionSelector
        )
    }
}


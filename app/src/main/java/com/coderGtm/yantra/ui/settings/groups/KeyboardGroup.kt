package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.containers.SettingsGroup
import com.coderGtm.yantra.ui.components.settingsItem.ToggleSetting

@Composable
fun KeyboardGroup(
    oneTapKeyboardActivation: Boolean,
    onOneTapKeyboardActivationChange: (Boolean) -> Unit,
    hideKeyboardOnEnter: Boolean,
    onHideKeyboardOnEnterChange: (Boolean) -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.keyboard)) {
        ToggleSetting(
            title           = stringResource(R.string.tap_anywhere_to_open_keyboard),
            checked         = oneTapKeyboardActivation,
            onCheckedChange = onOneTapKeyboardActivationChange
        )
        HorizontalDivider()
        ToggleSetting(
            title           = stringResource(R.string.hide_keyboard_on_command_enter),
            checked         = hideKeyboardOnEnter,
            onCheckedChange = onHideKeyboardOnEnterChange
        )
    }
}


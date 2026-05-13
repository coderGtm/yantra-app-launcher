package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.settingsItem.ButtonSetting
import com.coderGtm.yantra.ui.components.containers.SettingsGroup

@Composable
fun GesturesGroup(
    isProUser: Boolean,
    onOpenDoubleTapActionSetter: () -> Unit,
    onOpenSwipeRightActionSetter: () -> Unit,
    onOpenSwipeLeftActionSetter: () -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.gestures)) {
        ButtonSetting(
            title   = stringResource(R.string.change_double_tap_command),
            onClick = onOpenDoubleTapActionSetter
        )
        if (isProUser) {
            HorizontalDivider()
            ButtonSetting(
                title   = stringResource(R.string.change_right_swipe_command),
                onClick = onOpenSwipeRightActionSetter
            )
            HorizontalDivider()
            ButtonSetting(
                title   = stringResource(R.string.change_left_swipe_command),
                onClick = onOpenSwipeLeftActionSetter
            )
        }
    }
}


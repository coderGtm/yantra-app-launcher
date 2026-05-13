package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.settingsItem.OptionSetting
import com.coderGtm.yantra.ui.components.containers.SettingsGroup
import com.coderGtm.yantra.ui.components.settingsItem.ToggleSetting

@Composable
fun ArrowKeysGroup(
    showArrowKeys: Boolean,
    onShowArrowKeysChange: (Boolean) -> Unit,
    arrowSizeText: String,
    onOpenArrowSizeSetter: () -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.arrow_keys)) {
        ToggleSetting(
            title           = stringResource(R.string.show_arrow_keys),
            checked         = showArrowKeys,
            onCheckedChange = onShowArrowKeysChange
        )
        HorizontalDivider()
        OptionSetting(
            title   = stringResource(R.string.arrow_keys_size),
            value   = arrowSizeText,
            onClick = onOpenArrowSizeSetter
        )
    }
}


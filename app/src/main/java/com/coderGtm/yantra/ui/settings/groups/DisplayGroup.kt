package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.settingsItem.OptionSetting
import com.coderGtm.yantra.ui.components.containers.SettingsGroup
import com.coderGtm.yantra.ui.components.settingsItem.ToggleSetting

@Composable
fun DisplayGroup(
    useSystemWallpaper: Boolean,
    onUseSystemWallpaperChange: (Boolean) -> Unit,
    fullscreenLauncher: Boolean,
    onFullscreenLauncherChange: (Boolean) -> Unit,
    orientationText: String,
    onOpenOrientationSetter: () -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.display)) {
        ToggleSetting(
            title           = stringResource(R.string.use_system_wallpaper),
            checked         = useSystemWallpaper,
            onCheckedChange = onUseSystemWallpaperChange
        )
        HorizontalDivider()
        ToggleSetting(
            title           = stringResource(R.string.fullscreen_launcher),
            checked         = fullscreenLauncher,
            onCheckedChange = onFullscreenLauncherChange
        )
        HorizontalDivider()
        OptionSetting(
            title   = stringResource(R.string.orientation),
            value   = orientationText,
            onClick = onOpenOrientationSetter
        )
    }
}


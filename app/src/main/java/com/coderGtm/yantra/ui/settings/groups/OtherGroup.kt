package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.settingsItem.ButtonSetting
import com.coderGtm.yantra.ui.components.containers.SettingsGroup
import com.coderGtm.yantra.ui.components.settingsItem.ToggleSetting

/**
 * Merged group combining the former "Feedback" and "Advanced" sections.
 * Always shown last on the settings screen.
 */
@Composable
fun OtherGroup(
    vibrationPermission: Boolean,
    onVibrationPermissionChange: (Boolean) -> Unit,
    isProUser: Boolean,
    onOpenSoundEffectsList: () -> Unit,
    onOpenSysinfoArtSetter: () -> Unit,
    initCmdLog: Boolean,
    onInitCmdLogChange: (Boolean) -> Unit,
    disableAds: Boolean,
    onDisableAdsChange: (Boolean) -> Unit,
    onOpenNewsWebsiteSetter: () -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.other)) {
        // ── Former Feedback ───────────────────────────────────────────────────
        ToggleSetting(
            title           = stringResource(R.string.vibrate_on_error),
            checked         = vibrationPermission,
            onCheckedChange = onVibrationPermissionChange
        )
        if (isProUser) {
            HorizontalDivider()
            ButtonSetting(
                title   = stringResource(R.string.manage_sound_effects),
                onClick = onOpenSoundEffectsList
            )
        }

        // ── Former Advanced ───────────────────────────────────────────────────
        HorizontalDivider()
        ButtonSetting(
            title   = stringResource(R.string.change_sysinfo_art),
            onClick = onOpenSysinfoArtSetter
        )
        if (isProUser) {
            HorizontalDivider()
            ToggleSetting(
                title           = stringResource(R.string.log_commands_in_the_init_script),
                checked         = initCmdLog,
                onCheckedChange = onInitCmdLogChange
            )
            HorizontalDivider()
            ToggleSetting(
                title           = "AdBlocker in GUPT",
                checked         = disableAds,
                onCheckedChange = onDisableAdsChange
            )
            HorizontalDivider()
            ButtonSetting(
                title   = stringResource(R.string.change_news_website),
                onClick = onOpenNewsWebsiteSetter
            )
        }
    }
}


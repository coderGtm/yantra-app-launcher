package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.containers.SettingsGroup
import com.coderGtm.yantra.ui.components.settingsItem.OptionSetting

@Composable
fun PluginsGroup(
    enabledPluginCount: Int,
    installedPluginCount: Int,
    onOpenPluginManager: () -> Unit
) {
    SettingsGroup(title = stringResource(R.string.plugins)) {
        OptionSetting(
            title = stringResource(R.string.manage_your_plugins),
            description = stringResource(R.string.plugin_manager_description),
            value = stringResource(R.string.plugin_count_summary, enabledPluginCount, installedPluginCount),
            onClick = onOpenPluginManager
        )
    }
}

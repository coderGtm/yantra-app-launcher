package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.settingsItem.OptionSetting
import com.coderGtm.yantra.ui.components.containers.SettingsGroup
import com.coderGtm.yantra.ui.components.settingsItem.ToggleSetting

@Composable
fun PromptGroup(
    usernamePrefix: String,
    onOpenUsernamePrefix: () -> Unit,
    isProUser: Boolean,
    useModernPromptDesign: Boolean,
    onModernPromptDesignChange: (Boolean) -> Unit,
    showCurrentFolderInPrompt: Boolean,
    onShowCurrentFolderInPromptChange: (Boolean) -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.prompt)) {
        OptionSetting(
            title   = stringResource(R.string.username_prefix),
            value   = usernamePrefix,
            onClick = onOpenUsernamePrefix
        )
        if (isProUser) {
            HorizontalDivider()
            ToggleSetting(
                title           = stringResource(R.string.modern_prompt_design),
                checked         = useModernPromptDesign,
                onCheckedChange = onModernPromptDesignChange
            )
            HorizontalDivider()
            ToggleSetting(
                title           = stringResource(R.string.show_current_folder_in_prompt),
                checked         = showCurrentFolderInPrompt,
                onCheckedChange = onShowCurrentFolderInPromptChange
            )
        }
    }
}


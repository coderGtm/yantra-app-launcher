package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.settingsItem.ButtonSetting
import com.coderGtm.yantra.ui.components.containers.SettingsGroup

/** Shown only for Pro users. Wrap in `if (isProUser)` at call site. */
@Composable
fun AiGroup(
    onOpenApiProviderSetter: () -> Unit,
    onOpenApiKeySetter: () -> Unit,
    onOpenModelSetter: () -> Unit,
    onOpenSystemPromptSetter: () -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.ai)) {
        ButtonSetting(
            title   = stringResource(R.string.change_ai_api_provider),
            onClick = onOpenApiProviderSetter
        )
        HorizontalDivider()
        ButtonSetting(
            title   = stringResource(R.string.change_ai_api_key),
            onClick = onOpenApiKeySetter
        )
        HorizontalDivider()
        ButtonSetting(
            title   = stringResource(R.string.change_ai_model),
            onClick = onOpenModelSetter
        )
        HorizontalDivider()
        ButtonSetting(
            title   = stringResource(R.string.change_ai_system_prompt),
            onClick = onOpenSystemPromptSetter
        )
    }
}


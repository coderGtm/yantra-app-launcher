package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.ui.components.settingsItem.ButtonSetting
import com.coderGtm.yantra.ui.components.settingsItem.OptionSetting
import com.coderGtm.yantra.ui.components.containers.SettingsGroup
import com.coderGtm.yantra.ui.components.settingsItem.ToggleSetting

@Composable
fun SuggestionsGroup(
    getPrimarySuggestions: Boolean,
    onGetPrimarySuggestionsChange: (Boolean) -> Unit,
    getSecondarySuggestions: Boolean,
    onGetSecondarySuggestionsChange: (Boolean) -> Unit,
    appSugOrderText: String,
    onOpenAppSugOrderingSetter: () -> Unit,
    onOpenPrimarySuggestionsOrderSetter: () -> Unit,
    actOnSuggestionTap: Boolean,
    onActOnSuggestionTapChange: (Boolean) -> Unit,
    actOnLastSecondarySuggestion: Boolean,
    onActOnLastSecondarySuggestionChange: (Boolean) -> Unit,
) {
    SettingsGroup(title = stringResource(R.string.suggestions)) {
        ToggleSetting(
            title           = stringResource(R.string.primary_suggestions),
            checked         = getPrimarySuggestions,
            onCheckedChange = onGetPrimarySuggestionsChange
        )
        HorizontalDivider()
        ToggleSetting(
            title           = stringResource(R.string.secondary_suggestions),
            checked         = getSecondarySuggestions,
            onCheckedChange = onGetSecondarySuggestionsChange
        )
        HorizontalDivider()
        OptionSetting(
            title   = stringResource(R.string.app_suggestions_order),
            value   = appSugOrderText,
            onClick = onOpenAppSugOrderingSetter
        )
        HorizontalDivider()
        ButtonSetting(
            title   = stringResource(R.string.reorder_primary_suggestions),
            onClick = onOpenPrimarySuggestionsOrderSetter
        )
        HorizontalDivider()
        ToggleSetting(
            title           = stringResource(R.string.auto_execute_command_when_secondary_suggestion_is_selected),
            checked         = actOnSuggestionTap,
            onCheckedChange = onActOnSuggestionTapChange
        )
        HorizontalDivider()
        ToggleSetting(
            title           = stringResource(R.string.auto_execute_last_secondary_suggestion),
            checked         = actOnLastSecondarySuggestion,
            onCheckedChange = onActOnLastSecondarySuggestionChange
        )
    }
}


package com.coderGtm.yantra.ui.components.containers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.ui.components.settingsItem.ButtonSetting
import com.coderGtm.yantra.ui.components.settingsItem.ToggleSetting
import com.coderGtm.yantra.ui.theme.YantraTheme

/**
 * Root container for the settings screen.
 * Provides a scrollable [Column] with consistent padding.
 *
 * @param modifier Optional [Modifier].
 * @param content  The settings groups and items to display.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            content = content
        )
    }
}

@Preview(showBackground = true, name = "SettingsScreen")
@Composable
private fun SettingsScreenPreview() {
    YantraTheme {
        SettingsScreen {
            SettingsGroup(title = "Appearance") {
                ToggleSetting(
                    title = "Fullscreen Launcher",
                    checked = true,
                    onCheckedChange = {}
                )
                HorizontalDivider()
                ToggleSetting(
                    title = "Modern Prompt Design",
                    checked = false,
                    onCheckedChange = {}
                )
            }
            SettingsGroup(title = "Gestures") {
                ButtonSetting(title = "Double Tap Command", onClick = {})
                HorizontalDivider()
                ButtonSetting(title = "Right Swipe Command", onClick = {})
            }
        }
    }
}





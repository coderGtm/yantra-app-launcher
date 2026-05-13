package com.coderGtm.yantra.ui.components.containers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.ui.components.settingsItem.ButtonSetting
import com.coderGtm.yantra.ui.components.settingsItem.OptionSetting
import com.coderGtm.yantra.ui.components.settingsItem.ToggleSetting
import com.coderGtm.yantra.ui.theme.YantraTheme

/**
 * A visual grouping of related settings items under a section [title].
 *
 * Items are placed inside a card with consistent styling. Add [HorizontalDivider]
 * between items manually to match the original screen's divider style.
 *
 * Usage:
 * ```
 * SettingsGroup(title = "Appearance") {
 *     ToggleSetting(...)
 *     HorizontalDivider()
 *     OptionSetting(...)
 * }
 * ```
 *
 * @param title   The section header label shown above the group card.
 * @param modifier Optional [Modifier].
 * @param content  The settings items to place inside the group.
 */
@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .padding(bottom = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(content = content)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, name = "SettingsGroup – mixed items")
@Composable
private fun SettingsGroupPreview() {
    YantraTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsGroup(title = "Appearance") {
                ToggleSetting(
                    title = "Fullscreen Launcher",
                    checked = true,
                    onCheckedChange = {}
                )
                HorizontalDivider()
                OptionSetting(
                    title = "Orientation",
                    value = "Portrait",
                    onClick = {}
                )
                HorizontalDivider()
                ButtonSetting(
                    title = "Change Sysinfo Art",
                    onClick = {}
                )
            }
        }
    }
}


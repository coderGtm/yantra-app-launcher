package com.coderGtm.yantra.ui.components.settingsItem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.ui.theme.YantraTheme

/**
 * A reusable settings row with a title (and optional description) on the start
 * and a [Switch] on the end.
 *
 * @param title       The primary label shown for the setting.
 * @param checked     Whether the switch is currently on or off.
 * @param onCheckedChange Called when the user toggles the switch (or taps the row).
 * @param modifier    Optional [Modifier] for the root row.
 * @param description An optional secondary description shown below the title.
 */
@Composable
fun ToggleSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(showBackground = true, name = "ToggleSetting – on, with description")
@Composable
private fun ToggleSettingPreviewOn() {
    var checked by remember { mutableStateOf(true) }
    YantraTheme {
        ToggleSetting(
            title = "Show Arrow Keys",
            checked = checked,
            onCheckedChange = { checked = it },
            description = "Display arrow keys in the terminal"
        )
    }
}

@Preview(showBackground = true, name = "ToggleSetting – off, no description")
@Composable
private fun ToggleSettingPreviewOff() {
    var checked by remember { mutableStateOf(false) }
    YantraTheme {
        ToggleSetting(
            title = "Enable Vibration",
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}


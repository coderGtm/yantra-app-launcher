package com.coderGtm.yantra.ui.components.settingsItem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.ui.theme.YantraTheme

/**
 * A reusable settings row with a title (and optional description) on the start
 * and the currently selected option value on the end.
 *
 * @param title       The primary label shown for the setting.
 * @param value       The currently selected option to display on the end.
 * @param onClick     Called when the user taps the row (e.g. open a picker dialog).
 * @param modifier    Optional [Modifier] for the root row.
 * @param description An optional secondary description shown below the title.
 */
@Composable
fun OptionSetting(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "OptionSetting – with description")
@Composable
private fun OptionSettingPreviewWithDescription() {
    YantraTheme {
        OptionSetting(
            title = "Font Size",
            value = "16sp",
            onClick = {},
            description = "Size of the terminal font"
        )
    }
}

@Preview(showBackground = true, name = "OptionSetting – no description")
@Composable
private fun OptionSettingPreviewNoDescription() {
    YantraTheme {
        OptionSetting(
            title = "Orientation",
            value = "Portrait",
            onClick = {}
        )
    }
}


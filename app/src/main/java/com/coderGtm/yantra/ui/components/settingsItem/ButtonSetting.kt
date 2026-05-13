package com.coderGtm.yantra.ui.components.settingsItem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.ui.theme.YantraTheme

/**
 * A reusable settings row that displays a title and triggers an action on click.
 * No secondary control — the entire row is the button.
 *
 * @param title    The label shown for the setting.
 * @param onClick  Called when the user taps the row.
 * @param modifier Optional [Modifier] for the root row.
 */
@Composable
fun ButtonSetting(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Preview(showBackground = true, name = "ButtonSetting")
@Composable
private fun ButtonSettingPreview() {
    YantraTheme {
        ButtonSetting(
            title = "Clear App Cache",
            onClick = {}
        )
    }
}





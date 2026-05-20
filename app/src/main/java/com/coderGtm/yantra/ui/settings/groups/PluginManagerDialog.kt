package com.coderGtm.yantra.ui.settings.groups

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.R
import com.coderGtm.yantra.plugins.PluginManager
import com.coderGtm.yantra.plugins.YantraPlugin
import com.coderGtm.yantra.ui.theme.CharcoalDivider2
import com.coderGtm.yantra.ui.theme.CharcoalOnPrimary
import com.coderGtm.yantra.ui.theme.CharcoalSilver
import com.coderGtm.yantra.ui.theme.CharcoalTextMuted

@Composable
fun PluginManagerDialog(
    plugins: List<YantraPlugin>,
    isPluginEnabled: (YantraPlugin) -> Boolean,
    onPluginEnabledChange: (YantraPlugin, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(text = stringResource(R.string.manage_your_plugins))
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                plugins.forEachIndexed { index, plugin ->
                    PluginManagerRow(
                        plugin = plugin,
                        checked = isPluginEnabled(plugin),
                        onCheckedChange = { onPluginEnabledChange(plugin, it) }
                    )
                    if (index != plugins.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun PluginManagerRow(
    plugin: YantraPlugin,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val compatibility = PluginManager.compatibilityFor(plugin)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    plugin.author?.takeIf { it.isNotBlank() }?.let { author ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.plugin_author_label, author),
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalTextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = checked && compatibility.canEnable,
                    enabled = compatibility.canEnable,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = CharcoalSilver,
                        checkedThumbColor = CharcoalOnPrimary,
                        checkedBorderColor = CharcoalSilver,
                        uncheckedTrackColor = CharcoalDivider2,
                        uncheckedThumbColor = CharcoalTextMuted,
                        uncheckedBorderColor = CharcoalTextMuted,
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 9.dp))

            Text(
                text = plugin.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 9.dp))

            Row {
                PluginInfoPill(text = stringResource(R.string.plugin_version_pill, plugin.versionName))
                Spacer(modifier = Modifier.width(6.dp))
                PluginInfoPill(
                    text = compatibility.label,
                    isPositive = compatibility.canEnable
                )
            }

            if (plugin.capabilities.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 9.dp))

                Row {
                    plugin.capabilities.forEachIndexed { index, capability ->
                        PluginInfoPill(text = capability)
                        if (index != plugin.capabilities.lastIndex) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PluginInfoPill(
    text: String,
    isPositive: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isPositive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isPositive) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

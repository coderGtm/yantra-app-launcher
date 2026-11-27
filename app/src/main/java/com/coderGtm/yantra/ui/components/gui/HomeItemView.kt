package com.coderGtm.yantra.ui.components.gui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.coderGtm.yantra.R
import com.coderGtm.yantra.SYSTEM_PKG_CLI
import com.coderGtm.yantra.SYSTEM_PKG_SETTINGS

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemView(
    packageName: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    val systemItem = remember(packageName) {
        when (packageName) {
            SYSTEM_PKG_CLI -> "CLI" to R.drawable.ic_terminal
            SYSTEM_PKG_SETTINGS -> "Settings" to null // Use material icon
            else -> null
        }
    }
    
    val clickModifier = when {
        onLongClick != null && onClick != null -> Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
        onClick != null -> Modifier.clickable(onClick = onClick)
        else -> Modifier
    }

    if (systemItem != null) {
        Column(
            modifier = modifier
                .padding(4.dp)
                .then(clickModifier),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (systemItem.second != null) {
                Image(
                    painter = painterResource(systemItem.second!!),
                    contentDescription = systemItem.first,
                    modifier = Modifier.size(56.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = systemItem.first,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
            }
            Text(
                text = systemItem.first,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        return
    }

    val appInfo = remember(packageName) {
        runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            val label = packageManager.getApplicationLabel(info).toString()
            val icon = packageManager.getApplicationIcon(info).toBitmap()
            Pair(label, icon)
        }.getOrNull()
    }
    
    if (appInfo != null) {
        Column(
            modifier = modifier
                .padding(4.dp)
                .then(clickModifier),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                bitmap = appInfo.second.asImageBitmap(),
                contentDescription = appInfo.first,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = appInfo.first,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

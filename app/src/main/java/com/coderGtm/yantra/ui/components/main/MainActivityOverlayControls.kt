package com.coderGtm.yantra.ui.components.main

import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderGtm.yantra.ui.screens.main.ComposeButtonController
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs
import com.coderGtm.yantra.ui.screens.main.MainSuggestionItem
import com.coderGtm.yantra.ui.screens.main.toComposeFontStyle
import com.coderGtm.yantra.ui.screens.main.toComposeFontWeight
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@Composable
internal fun MainActivityOverlayControls(
    uiRefs: MainActivityUiRefs,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 15.dp, bottom = 50.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            MainActivityArrowButton(controller = uiRefs.upBtn)
            MainActivityArrowButton(controller = uiRefs.downBtn)
        }

        if (uiRefs.suggestionsTab.items.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(uiRefs.suggestionsTab.backgroundColorInt))
                    .horizontalScroll(rememberScrollState()),
            ) {
                uiRefs.suggestionsTab.items.forEach { item ->
                    MainActivitySuggestionChip(item = item)
                }
            }
        }
    }
}

@Composable
private fun MainActivityArrowButton(controller: ComposeButtonController) {
    if (controller.visibility != View.VISIBLE) {
        return
    }

    Text(
        text = controller.text,
        color = Color(controller.textColorInt),
        fontSize = controller.textSize.sp,
        modifier = Modifier.combinedClickable(onClick = controller::performClick),
    )
}

@Composable
private fun MainActivitySuggestionChip(item: MainSuggestionItem) {
    Text(
        text = item.text.uppercase(LocalLocale.current.platformLocale),
        color = Color(item.color),
        fontSize = item.fontSize.sp,
        fontFamily = item.typeface?.let { FontFamily(it) },
        fontWeight = item.style.toComposeFontWeight(),
        fontStyle = item.style.toComposeFontStyle(),
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 15.dp)
            .combinedClickable(
                onClick = item.onClick,
                onLongClick = item.onLongClick,
            ),
    )
}




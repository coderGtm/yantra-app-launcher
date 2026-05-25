package com.coderGtm.yantra.ui.components.main

import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderGtm.yantra.ui.components.ModernChatBubble
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs
import com.coderGtm.yantra.ui.screens.main.MainTerminalOutputItem
import com.coderGtm.yantra.ui.screens.main.toComposeFontStyle
import com.coderGtm.yantra.ui.screens.main.toComposeFontWeight
import io.noties.markwon.Markwon
import kotlin.math.abs

@Composable
internal fun MainActivityTerminalPane(
    uiRefs: MainActivityUiRefs,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(uiRefs.scrollView.scrollToBottomNonce, uiRefs.terminalOutput.items.size, uiRefs.luaInputSession) {
        val lastIndex = uiRefs.terminalOutput.items.lastIndex + 2
        if (lastIndex >= 0) {
            listState.scrollToItem(lastIndex)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
            .collect { isAtTop ->
                uiRefs.scrollView.isAtTop = isAtTop
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(uiRefs) {
                detectTapGestures(
                    onPress = {
                        if (tryAwaitRelease()) {
                            uiRefs.scrollView.onSingleTap()
                        }
                    },
                    onDoubleTap = { uiRefs.scrollView.onDoubleTap() },
                )
            }
            .pointerInput(uiRefs) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val velocityTracker = VelocityTracker().apply {
                        addPosition(down.uptimeMillis, down.position)
                    }
                    var currentPosition = down.position

                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Final)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change == null) {
                            break
                        }

                        currentPosition = change.position
                        velocityTracker.addPosition(change.uptimeMillis, currentPosition)

                        if (!change.pressed) {
                            val diffX = currentPosition.x - down.position.x
                            val diffY = currentPosition.y - down.position.y
                            val velocity = velocityTracker.calculateVelocity()
                            val swipeThreshold = 250f
                            val swipeVelocityThreshold = 100f

                            if (abs(diffX) > abs(diffY) && abs(diffX) > swipeThreshold && abs(velocity.x) > swipeVelocityThreshold) {
                                if (diffX > 0) {
                                    uiRefs.scrollView.onSwipeRight()
                                } else {
                                    uiRefs.scrollView.onSwipeLeft()
                                }
                            } else if (abs(diffY) > swipeThreshold && abs(velocity.y) > swipeVelocityThreshold && diffY > 0 && uiRefs.scrollView.isAtTop) {
                                uiRefs.scrollView.expandNotificationPanel()
                            }
                            break
                        }
                    }
                }
            },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp, vertical = 15.dp),
        ) {
            items(
                items = uiRefs.terminalOutput.items,
                key = { it.id },
                contentType = {
                    when (it) {
                        is MainTerminalOutputItem.Text -> "text"
                        is MainTerminalOutputItem.ChatBubble -> "chatBubble"
                        is MainTerminalOutputItem.Action -> "action"
                    }
                },
            ) { item ->
                MainActivityOutputItem(item = item)
            }
            item(key = "inputLine", contentType = "inputLine") {
                MainActivityInputLine(
                    uiRefs = uiRefs,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(key = "bottomSpacer", contentType = "spacer") {
                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }
}

@Composable
private fun MainActivityOutputItem(item: MainTerminalOutputItem) {
    when (item) {
        is MainTerminalOutputItem.Text -> MainActivityTextOutput(item)
        is MainTerminalOutputItem.ChatBubble -> ModernChatBubble(
            username = item.username,
            command = item.command,
            commandColor = Color(item.commandColor),
            fontSize = item.fontSize,
            fontFamily = item.typeface?.let { FontFamily(it) },
        )
        is MainTerminalOutputItem.Action -> Text(
            text = item.text,
            color = Color(item.color),
            fontSize = item.fontSize.sp,
            textDecoration = if (item.underlined) TextDecoration.Underline else null,
            modifier = Modifier
                .padding(vertical = 2.dp)
                .pointerInput(item.id) {
                    detectTapGestures(onTap = { item.onClick() })
                },
        )
    }
}

@Composable
private fun MainActivityTextOutput(item: MainTerminalOutputItem.Text) {
    val baseStyle = SpanStyle(
        color = Color(item.color),
        fontSize = item.fontSize.sp,
        fontFamily = item.typeface?.let { FontFamily(it) },
        fontWeight = item.style.toComposeFontWeight(),
        fontStyle = item.style.toComposeFontStyle(),
    )

    val text = if (item.markdown) {
        rememberMarkdownAnnotatedString(item = item, baseStyle = baseStyle)
    } else {
        remember(item.id, baseStyle) {
            buildAnnotatedString {
                append(item.text)
                addStyle(baseStyle, 0, item.text.length)
            }
        }
    }

    SelectionContainer {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
        )
    }
}

@Composable
private fun rememberMarkdownAnnotatedString(
    item: MainTerminalOutputItem.Text,
    baseStyle: SpanStyle,
): AnnotatedString {
    val context = LocalContext.current
    val markwon = remember(context) { Markwon.create(context) }
    return remember(item.id, baseStyle) {
        spannedToAnnotatedString(
            spanned = markwon.toMarkdown(item.text),
            baseStyle = baseStyle,
        )
    }
}

private fun spannedToAnnotatedString(
    spanned: Spanned,
    baseStyle: SpanStyle,
): AnnotatedString = buildAnnotatedString {
    val rawText = spanned.toString()
    append(rawText)
    if (rawText.isNotEmpty()) {
        addStyle(baseStyle, 0, rawText.length)
    }

    spanned.getSpans(0, rawText.length, Any::class.java).forEach { span ->
        val start = spanned.getSpanStart(span).coerceAtLeast(0)
        val end = spanned.getSpanEnd(span).coerceIn(0, rawText.length)
        if (start >= end) {
            return@forEach
        }

        when (span) {
            is StyleSpan -> addStyle(
                SpanStyle(
                    fontWeight = span.style.toComposeFontWeight(),
                    fontStyle = span.style.toComposeFontStyle(),
                ),
                start,
                end,
            )
            is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
            is StrikethroughSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
            is ForegroundColorSpan -> addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
            is BackgroundColorSpan -> addStyle(SpanStyle(background = Color(span.backgroundColor)), start, end)
            is TypefaceSpan -> {
                span.family?.let { familyName ->
                    val mappedTypeface = when (familyName.lowercase()) {
                        "monospace" -> FontFamily.Monospace
                        "serif" -> FontFamily.Serif
                        "sans-serif" -> FontFamily.SansSerif
                        else -> null
                    }
                    if (mappedTypeface != null) {
                        addStyle(SpanStyle(fontFamily = mappedTypeface), start, end)
                    }
                }
            }
        }
    }
}




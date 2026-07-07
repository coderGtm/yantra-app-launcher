package com.coderGtm.yantra.ui.components.main

import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderGtm.yantra.ui.components.ModernInputPrompt
import com.coderGtm.yantra.ui.screens.main.ComposeInputController
import com.coderGtm.yantra.ui.screens.main.LuaInputSession
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs
import com.coderGtm.yantra.ui.screens.main.toComposeFontStyle
import com.coderGtm.yantra.ui.screens.main.toComposeFontWeight
import kotlin.math.max
import kotlin.math.min

@Composable
internal fun MainActivityInputLine(
    uiRefs: MainActivityUiRefs,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 200.dp)
            .clickable { uiRefs.inputLineLayout.performClick() },
        horizontalArrangement = Arrangement.Start,
    ) {
        if (uiRefs.modernPrompt.visible) {
            ModernInputPrompt(
                username = uiRefs.modernPrompt.username,
                fontSize = uiRefs.cmdInput.textSize,
                fontFamily = uiRefs.modernPrompt.fontFamily,
            )
        } else if (uiRefs.username.visibility == View.VISIBLE) {
            Text(
                text = uiRefs.username.text,
                color = Color(uiRefs.username.textColorInt),
                fontSize = uiRefs.username.textSize.sp,
                fontFamily = uiRefs.username.typeface?.let { FontFamily(it) },
                fontWeight = uiRefs.username.style.toComposeFontWeight(),
                fontStyle = uiRefs.username.style.toComposeFontStyle(),
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        val session = uiRefs.luaInputSession
        if (session != null) {
            LuaInputField(
                session = session,
                onValueChange = uiRefs::updateLuaInputValue,
            )
        } else if (uiRefs.cmdInput.visibility == View.VISIBLE) {
            CommandInputField(controller = uiRefs.cmdInput)
        }
    }
}

@Composable
private fun RowScope.CommandInputField(controller: ComposeInputController) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val cursorColor = Color(controller.cursorColorInt)
    val cursorAlpha = rememberTerminalCursorAlpha(
        isVisible = isFocused && controller.value.selection.collapsed,
    )

    LaunchedEffect(controller.focusRequestNonce) {
        if (controller.focusRequestNonce == 0) {
            return@LaunchedEffect
        }
        focusRequester.requestFocus()
    }
    LaunchedEffect(controller.showKeyboardNonce) {
        if (controller.showKeyboardNonce == 0) {
            return@LaunchedEffect
        }
        focusRequester.requestFocus()
        withFrameNanos { }
        keyboardController?.show()
    }
    LaunchedEffect(controller.hideKeyboardNonce) {
        if (controller.hideKeyboardNonce == 0) {
            return@LaunchedEffect
        }
        keyboardController?.hide()
    }

    BasicTextField(
        value = controller.value,
        onValueChange = controller::onValueChanged,
        enabled = controller.isEnabled,
        cursorBrush = SolidColor(Color.Transparent),
        textStyle = TextStyle(
            color = Color(controller.textColorInt),
            fontSize = controller.textSize.sp,
            fontFamily = controller.typeface?.let { FontFamily(it) },
        ),
        modifier = Modifier
            .weight(1f)
            .padding(start = 5.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .drawTerminalCursor(
                value = controller.value,
                textLayoutResult = textLayoutResult,
                cursorColor = cursorColor,
                cursorAlpha = cursorAlpha,
            ),
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = true, imeAction = androidx.compose.ui.text.input.ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = {
            controller.dispatchEditorAction(EditorInfo.IME_ACTION_SEND)
        }),
        singleLine = false,
        onTextLayout = { textLayoutResult = it },
    )
}

@Composable
private fun RowScope.LuaInputField(
    session: LuaInputSession,
    onValueChange: (TextFieldValue) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val cursorColor = Color(session.cursorColor)
    val cursorAlpha = rememberTerminalCursorAlpha(
        isVisible = isFocused && session.value.selection.collapsed,
    )

    LaunchedEffect(session.requestFocusNonce) {
        focusRequester.requestFocus()
        withFrameNanos { }
        keyboardController?.show()
    }

    BasicTextField(
        value = session.value,
        onValueChange = onValueChange,
        cursorBrush = SolidColor(Color.Transparent),
        textStyle = TextStyle(
            color = Color(session.color),
            fontSize = session.fontSize.sp,
            fontFamily = session.typeface?.let { FontFamily(it) },
        ),
        modifier = Modifier
            .weight(1f)
            .padding(start = 5.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .drawTerminalCursor(
                value = session.value,
                textLayoutResult = textLayoutResult,
                cursorColor = cursorColor,
                cursorAlpha = cursorAlpha,
            ),
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = true, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            session.onSubmit(session.value.text)
        }),
        singleLine = true,
        onTextLayout = { textLayoutResult = it },
        decorationBox = { innerTextField ->
            if (session.value.text.isEmpty()) {
                Text(
                    text = session.placeholder,
                    color = Color(session.color).copy(alpha = 0.6f),
                    fontSize = session.fontSize.sp,
                )
            }
            innerTextField()
        },
    )
}

@Composable
private fun rememberTerminalCursorAlpha(isVisible: Boolean): Float {
    if (!isVisible) {
        return 0f
    }

    val transition = rememberInfiniteTransition(label = "terminalCursorBlink")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1f at 0
                1f at 499
                0f at 500
                0f at 999
            },
        ),
        label = "terminalCursorAlpha",
    )
    return alpha
}

private fun Modifier.drawTerminalCursor(
    value: TextFieldValue,
    textLayoutResult: TextLayoutResult?,
    cursorColor: Color,
    cursorAlpha: Float,
): Modifier = this.drawWithContent {
    drawContent()

    if (cursorAlpha <= 0f || !value.selection.collapsed) {
        return@drawWithContent
    }

    val layout = textLayoutResult ?: return@drawWithContent
    val maxOffset = layout.layoutInput.text.length
    val cursorOffset = value.selection.start.coerceIn(0, maxOffset)
    val cursorRect = layout.getCursorRect(cursorOffset)
    val cursorWidth = 6.dp.toPx()
    val availableWidth = size.width - cursorRect.left
    val drawnWidth = min(cursorWidth, max(availableWidth, 1f))
    val drawnHeight = max(cursorRect.height, 1.dp.toPx())

    drawRect(
        color = cursorColor.copy(alpha = cursorAlpha),
        topLeft = Offset(cursorRect.left, cursorRect.top),
        size = Size(drawnWidth, drawnHeight),
    )
}




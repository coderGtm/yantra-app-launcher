package com.coderGtm.yantra.ui.components.main

import android.view.View
import android.view.inputmethod.EditorInfo
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderGtm.yantra.ui.components.ModernInputPrompt
import com.coderGtm.yantra.ui.screens.main.ComposeInputController
import com.coderGtm.yantra.ui.screens.main.LuaInputSession
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs
import com.coderGtm.yantra.ui.screens.main.toComposeFontStyle
import com.coderGtm.yantra.ui.screens.main.toComposeFontWeight

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
        cursorBrush = SolidColor(Color(controller.cursorColorInt)),
        textStyle = TextStyle(
            color = Color(controller.textColorInt),
            fontSize = controller.textSize.sp,
            fontFamily = controller.typeface?.let { FontFamily(it) },
        ),
        modifier = Modifier
            .weight(1f)
            .padding(start = 5.dp)
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = true, imeAction = androidx.compose.ui.text.input.ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = {
            controller.dispatchEditorAction(EditorInfo.IME_ACTION_SEND)
        }),
        singleLine = false,
    )
}

@Composable
private fun RowScope.LuaInputField(
    session: LuaInputSession,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(session.requestFocusNonce) {
        focusRequester.requestFocus()
        withFrameNanos { }
        keyboardController?.show()
    }

    BasicTextField(
        value = session.value,
        onValueChange = onValueChange,
        cursorBrush = SolidColor(Color(session.cursorColor)),
        textStyle = TextStyle(
            color = Color(session.color),
            fontSize = session.fontSize.sp,
            fontFamily = session.typeface?.let { FontFamily(it) },
        ),
        modifier = Modifier
            .weight(1f)
            .padding(start = 5.dp)
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = true, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            session.onSubmit(session.value.text)
        }),
        singleLine = true,
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




package com.coderGtm.yantra.ui.screens.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.graphics.drawable.DrawableCompat
import com.coderGtm.yantra.R
import com.coderGtm.yantra.interfaces.TerminalGestureListenerCallback
import com.coderGtm.yantra.terminal.TerminalEditText
import java.util.UUID

sealed class MainTerminalOutputItem(open val id: String) {
    data class Text(
        override val id: String = UUID.randomUUID().toString(),
        val text: String,
        val color: Int,
        val style: Int?,
        val markdown: Boolean = false,
        val typeface: Typeface? = null,
        val fontSize: Float = 16f,
    ) : MainTerminalOutputItem(id)

    data class ChatBubble(
        override val id: String = UUID.randomUUID().toString(),
        val username: String,
        val command: String,
        val commandColor: Int,
        val fontSize: Float,
        val typeface: Typeface? = null,
    ) : MainTerminalOutputItem(id)

    data class Action(
        override val id: String = UUID.randomUUID().toString(),
        val text: String,
        val color: Int,
        val underlined: Boolean = false,
        val fontSize: Float = 16f,
        val onClick: () -> Unit,
    ) : MainTerminalOutputItem(id)
}

data class MainSuggestionItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val color: Int,
    val fontSize: Float,
    val typeface: Typeface? = null,
    val style: Int? = null,
    val onClick: () -> Unit,
    val onLongClick: (() -> Unit)? = null,
)

data class LuaInputSession(
    val scriptName: String,
    val originalUsernameText: String,
    val placeholder: String,
    val onSubmit: (String) -> Unit,
    val onTerminate: () -> Unit,
    val color: Int,
    val fontSize: Float,
    val cursorColor: Int,
    val typeface: Typeface? = null,
    val requestFocusNonce: Int = 0,
    val value: TextFieldValue = TextFieldValue(""),
)

class MainActivityUiRefs(
    val context: Context,
) {
    var backgroundBitmap by mutableStateOf<Bitmap?>(null)
    var backgroundColorInt by mutableIntStateOf(Color.TRANSPARENT)

    val scrollView = ComposeScrollController(context)
    val terminalOutput = ComposeOutputController()
    val inputLineLayout = ComposeClickController()
    val cmdInput = ComposeInputController()
    val username = ComposeTextController("root>")
    val upBtn = ComposeButtonController("▲")
    val downBtn = ComposeButtonController("▼")
    val suggestionsTab = ComposeSuggestionsController()
    val modernPrompt = ComposeModernPromptController()

    var luaInputSession by mutableStateOf<LuaInputSession?>(null)

    fun clearTerminalOutput() = terminalOutput.removeAllViews()

    fun addTextOutput(
        text: String,
        color: Int,
        style: Int?,
        markdown: Boolean,
        typeface: Typeface?,
        fontSize: Float,
    ): String {
        val item = MainTerminalOutputItem.Text(
            text = text,
            color = color,
            style = style,
            markdown = markdown,
            typeface = typeface,
            fontSize = fontSize,
        )
        terminalOutput.add(item)
        return item.id
    }

    fun updateTextOutput(
        id: String,
        text: String,
        color: Int,
        style: Int?,
        markdown: Boolean,
        typeface: Typeface?,
        fontSize: Float,
    ) {
        terminalOutput.updateText(
            id = id,
            text = text,
            color = color,
            style = style,
            markdown = markdown,
            typeface = typeface,
            fontSize = fontSize,
        )
    }

    fun addChatBubbleOutput(
        username: String,
        command: String,
        commandColor: Int,
        fontSize: Float,
        typeface: Typeface?,
    ): String {
        val item = MainTerminalOutputItem.ChatBubble(
            username = username,
            command = command,
            commandColor = commandColor,
            fontSize = fontSize,
            typeface = typeface,
        )
        terminalOutput.add(item)
        return item.id
    }

    fun addActionOutput(
        text: String,
        color: Int,
        underlined: Boolean,
        fontSize: Float,
        onClick: () -> Unit,
    ): String {
        val item = MainTerminalOutputItem.Action(
            text = text,
            color = color,
            underlined = underlined,
            fontSize = fontSize,
            onClick = onClick,
        )
        terminalOutput.add(item)
        return item.id
    }

    fun removeOutputItem(id: String) {
        terminalOutput.removeById(id)
    }

    fun requestCommandInputFocus(showKeyboard: Boolean = true) {
        cmdInput.requestFocus(showKeyboard)
    }

    fun addSuggestion(
        text: String,
        color: Int,
        fontSize: Float,
        typeface: Typeface? = null,
        style: Int? = null,
        onClick: () -> Unit,
        onLongClick: (() -> Unit)? = null,
    ): String {
        val item = MainSuggestionItem(
            text = text,
            color = color,
            fontSize = fontSize,
            typeface = typeface,
            style = style,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        suggestionsTab.add(item)
        return item.id
    }

    fun removeSuggestion(id: String) {
        suggestionsTab.removeById(id)
    }

    fun hideKeyboard() {
        cmdInput.hideKeyboard()
    }

    fun showLuaInput(session: LuaInputSession) {
        luaInputSession = session.copy(requestFocusNonce = session.requestFocusNonce + 1)
    }

    fun updateLuaInputValue(value: TextFieldValue) {
        val current = luaInputSession ?: return
        luaInputSession = current.copy(value = value)
    }

    fun requestLuaInputFocus() {
        val current = luaInputSession ?: return
        luaInputSession = current.copy(requestFocusNonce = current.requestFocusNonce + 1)
    }

    fun clearLuaInput() {
        luaInputSession = null
    }
}

fun createMainActivityUiRefs(context: Context): MainActivityUiRefs = MainActivityUiRefs(context)

class ComposeClickController {
    private var clickListener: (() -> Unit)? = null

    fun setOnClickListener(listener: () -> Unit) {
        clickListener = listener
    }

    fun performClick() {
        clickListener?.invoke()
    }
}

class ComposeTextController(initialText: String) {
    var text by mutableStateOf(initialText)
    var visibility by mutableIntStateOf(View.VISIBLE)
    var textColorInt by mutableIntStateOf(Color.WHITE)
    var textSize by mutableStateOf(16f)
    var typeface by mutableStateOf<Typeface?>(null)
    var style by mutableIntStateOf(Typeface.NORMAL)

    fun setTextColor(color: Int) {
        textColorInt = color
    }

    fun setTypeface(typeface: Typeface?, style: Int) {
        this.typeface = typeface
        this.style = style
    }
}

internal fun Int?.toComposeFontWeight(): FontWeight? = when (this) {
    Typeface.BOLD, Typeface.BOLD_ITALIC -> FontWeight.Bold
    else -> null
}

internal fun Int?.toComposeFontStyle(): FontStyle? = when (this) {
    Typeface.ITALIC, Typeface.BOLD_ITALIC -> FontStyle.Italic
    else -> null
}

class ComposeButtonController(initialText: String) {
    var text by mutableStateOf(initialText)
    var visibility by mutableIntStateOf(View.GONE)
    var textColorInt by mutableIntStateOf(Color.WHITE)
    var textSize by mutableStateOf(16f)

    private var clickListener: (() -> Unit)? = null

    fun setOnClickListener(listener: () -> Unit) {
        clickListener = listener
    }

    fun performClick() {
        clickListener?.invoke()
    }

    fun setTextColor(color: Int) {
        textColorInt = color
    }
}

class ComposeModernPromptController {
    var visible by mutableStateOf(false)
    var username by mutableStateOf("root")
    var fontFamily by mutableStateOf<FontFamily?>(null)
}

class EditableString(private val raw: String) {
    val length: Int
        get() = raw.length

    override fun toString(): String = raw
}

/**
 * Facade over the native [TerminalEditText] hosted in the Compose input row.
 *
 * The command field is deliberately a framework EditText rather than a Compose
 * BasicTextField: the native view owns the [android.view.inputmethod.InputConnection],
 * so programmatic clears via [setText] finish any active composing region and IMEs
 * with composing buffers (e.g. SwiftKey) cannot push stale text back. A Compose
 * state-only reset does not end the IME session, which caused submitted commands
 * to stick in the input line on such keyboards.
 *
 * Text flows imperatively (never through recomposition): callers use [setText] /
 * [text], and typing is mirrored into [inputText] for Compose readers.
 */
class ComposeInputController {
    // Compose-observable mirror of the native field's text (e.g. scroll-snapping
    // on typing). Never fed into an IME.
    var inputText by mutableStateOf("")
        private set
    var isEnabled by mutableStateOf(true)
    var visibility by mutableIntStateOf(View.VISIBLE)
    var textColorInt by mutableIntStateOf(Color.WHITE)
    var textSize by mutableStateOf(16f)
    var typeface by mutableStateOf<Typeface?>(null)
    var cursorColorInt by mutableIntStateOf(Color.WHITE)

    private var editText: TerminalEditText? = null
    private var pendingText: String = ""
    private var pendingSelection: Int = 0
    private var pendingFocusRequest: Boolean = false
    private var pendingShowKeyboard: Boolean = false

    private val textChangedListeners = mutableListOf<(CharSequence?) -> Unit>()
    private var editorActionListener: ((Any?, Int, Any?) -> Boolean)? = null

    val text: EditableString?
        get() = EditableString(editText?.text?.toString() ?: pendingText)

    fun setText(text: String) {
        val view = editText
        if (view == null) {
            pendingText = text
            pendingSelection = text.length
            inputText = text
            notifyTextChanged(text)
            return
        }
        // Framework setText(): finishes any active composing region, so the IME
        // cannot restore the old text afterwards. Mirror + listeners update via
        // the TextWatcher registered in [attach].
        view.setText(text)
        view.setSelection(text.length)
    }

    fun setSelection(index: Int) {
        val view = editText
        if (view == null) {
            pendingSelection = index.coerceIn(0, pendingText.length)
            return
        }
        view.setSelection(index.coerceIn(0, view.text?.length ?: 0))
    }

    fun requestFocus(showKeyboard: Boolean = true) {
        val view = editText
        if (view == null) {
            pendingFocusRequest = true
            pendingShowKeyboard = showKeyboard
            return
        }
        view.requestFocus()
        if (showKeyboard) {
            showKeyboard(view)
        }
    }

    fun hideKeyboard() {
        val view = editText ?: return
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun setTextColor(color: Int) {
        textColorInt = color
    }

    fun setOnEditorActionListener(listener: (Any?, Int, Any?) -> Boolean) {
        editorActionListener = listener
        editText?.setOnEditorActionListener { _, actionId, event ->
            editorActionListener?.invoke(null, actionId, event) ?: false
        }
    }

    fun addTextChangedListener(listener: (CharSequence?) -> Unit) {
        textChangedListeners += listener
    }

    fun attach(view: TerminalEditText, onFocusGained: () -> Unit) {
        editText = view
        applyStyle(view)
        applyCursor(view)
        view.setText(pendingText)
        view.setSelection(pendingSelection.coerceIn(0, pendingText.length))
        inputText = pendingText
        view.setOnEditorActionListener { _, actionId, event ->
            editorActionListener?.invoke(null, actionId, event) ?: false
        }
        view.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                onFocusGained()
            }
        }
        view.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val current = s?.toString().orEmpty()
                pendingText = current
                pendingSelection = view.selectionStart.coerceAtLeast(0)
                inputText = current
                notifyTextChanged(current)
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        if (pendingFocusRequest) {
            pendingFocusRequest = false
            view.post {
                view.requestFocus()
                if (pendingShowKeyboard) {
                    showKeyboard(view)
                }
                pendingShowKeyboard = false
            }
        }
    }

    fun detach(view: TerminalEditText) {
        if (editText === view) {
            pendingText = view.text?.toString().orEmpty()
            pendingSelection = view.selectionStart.coerceAtLeast(0)
            editText = null
        }
    }

    fun applyStyle(view: TerminalEditText) {
        view.setTextColor(textColorInt)
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        view.typeface = typeface
        view.isEnabled = isEnabled
    }

    private fun applyCursor(view: TerminalEditText) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return
        }
        val drawable = AppCompatResources.getDrawable(view.context, R.drawable.cursor_drawable)
            ?.mutate() ?: return
        DrawableCompat.setTint(drawable, cursorColorInt)
        view.textCursorDrawable = drawable
    }

    private fun showKeyboard(view: TerminalEditText) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    private fun notifyTextChanged(text: CharSequence?) {
        textChangedListeners.forEach { it(text) }
    }
}

class ComposeSuggestionsController {
    val items = mutableStateListOf<MainSuggestionItem>()
    var backgroundColorInt by mutableIntStateOf(Color.TRANSPARENT)

    fun removeAllViews() {
        items.clear()
    }

    fun add(item: MainSuggestionItem) {
        items.add(item)
    }

    fun removeById(id: String) {
        items.removeAll { it.id == id }
    }
}

class ComposeOutputController {
    val items = mutableStateListOf<MainTerminalOutputItem>()

    fun add(item: MainTerminalOutputItem) {
        items.add(item)
    }

    fun removeAllViews() {
        items.clear()
    }

    fun removeById(id: String) {
        items.removeAll { it.id == id }
    }

    fun updateText(
        id: String,
        text: String,
        color: Int,
        style: Int?,
        markdown: Boolean,
        typeface: Typeface?,
        fontSize: Float,
    ) {
        val index = items.indexOfFirst { it.id == id }
        if (index == -1) return
        items[index] = MainTerminalOutputItem.Text(
            id = id,
            text = text,
            color = color,
            style = style,
            markdown = markdown,
            typeface = typeface,
            fontSize = fontSize,
        )
    }
}

class ComposeScrollController(
    private val context: Context,
) {
    private var gestureCallback: TerminalGestureListenerCallback? = null
    var scrollToBottomNonce by mutableIntStateOf(0)
    var isAtTop by mutableStateOf(true)

    fun setGestureListenerCallback(callback: TerminalGestureListenerCallback) {
        gestureCallback = callback
    }

    fun onSingleTap() {
        gestureCallback?.onSingleTap()
    }

    fun onDoubleTap() {
        gestureCallback?.onDoubleTap()
    }

    fun onSwipeRight() {
        gestureCallback?.onSwipeRight()
    }

    fun onSwipeLeft() {
        gestureCallback?.onSwipeLeft()
    }

    fun scrollToBottom() {
        scrollToBottomNonce++
    }

    fun expandNotificationPanel() {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        } catch (_: Exception) {
        }
    }
}



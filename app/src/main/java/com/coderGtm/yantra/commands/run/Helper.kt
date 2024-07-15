package com.coderGtm.yantra.commands.run

import android.os.Build
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.coderGtm.yantra.LuaExecutor
import com.coderGtm.yantra.R
import com.coderGtm.yantra.terminal.Terminal


fun requestInput(luaExecutor: LuaExecutor, terminal: Terminal, scriptName: String, callback: (String) -> Unit) {
    val luaInput = EditText(terminal.activity)
    val terminateBtn = TextView(terminal.activity)
    val originalUsernameText = terminal.binding.username.text.toString()
    switchToLuaInput(terminal, luaExecutor, scriptName, luaInput, terminateBtn, originalUsernameText)
    luaInput.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            val input = luaInput.text.toString()
            switchToCmdInput(terminal, luaInput, terminateBtn, originalUsernameText)
            callback(input)
        }
        false
    }
}

private fun switchToLuaInput(terminal: Terminal, luaExecutor: LuaExecutor, scriptName: String, luaInput: EditText, terminateBtn: TextView, originalUsernameText: String) {
    terminal.cmdInput.isEnabled = false
    terminal.binding.username.text = "$scriptName>"

    luaInput.imeOptions = EditorInfo.IME_ACTION_DONE
    luaInput.isSingleLine = true
    luaInput.hint = "Enter your input here"
    luaInput.textSize = terminal.preferenceObject.getInt("fontSize", 16).toFloat()
    val param = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        1.0f
    )
    luaInput.setLayoutParams(param)
    luaInput.background = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        luaInput.textCursorDrawable = AppCompatResources.getDrawable(terminal.activity.baseContext, R.drawable.cursor_drawable)
    }

    val spannable = SpannableString("[Terminate Script]")
    spannable.setSpan(UnderlineSpan(), 0, spannable.length, 0)
    terminateBtn.text = spannable
    terminateBtn.textSize = terminal.preferenceObject.getInt("fontSize", 16).toFloat()
    terminateBtn.setTextColor(terminal.theme.errorTextColor)
    // button should not go to next line
    terminateBtn.isSingleLine = true
    terminateBtn.setOnClickListener {
        terminal.binding.terminalOutput.removeView(terminateBtn)
        terminal.binding.inputLineLayout.removeView(luaInput)
        terminal.cmdInput.visibility = View.VISIBLE
        terminal.cmdInput.isEnabled = true
        terminal.binding.username.text = originalUsernameText
        terminal.cmdInput.requestFocus()
        // terminate the lua script
        luaExecutor.terminate()
    }
    terminal.cmdInput.visibility = View.GONE
    terminal.binding.inputLineLayout.addView(luaInput)
    terminal.binding.terminalOutput.addView(terminateBtn)
    luaInput.requestFocus()
}

private fun switchToCmdInput(terminal: Terminal, luaInput: EditText, terminateBtn: TextView, originalUsernameText: String) {
    terminal.binding.terminalOutput.removeView(terminateBtn)
    terminal.binding.inputLineLayout.removeView(luaInput)
    terminal.cmdInput.visibility = View.VISIBLE
    terminal.cmdInput.isEnabled = true
    terminal.binding.username.text = originalUsernameText
    terminal.cmdInput.requestFocus()
}
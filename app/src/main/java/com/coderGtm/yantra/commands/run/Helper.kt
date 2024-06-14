package com.coderGtm.yantra.commands.run

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.coderGtm.yantra.blueprints.LuaExecutor
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.button.MaterialButton

fun requestInput(luaExecutor: LuaExecutor, terminal: Terminal, scriptName: String, callback: (String) -> Unit) {
    // disable the cmdInput and create a new edittext for input
    terminal.binding.cmdInput.isEnabled = false
    val originalUsernameText = terminal.binding.username.text.toString()
    terminal.binding.username.text = "$scriptName>"

    val luaInput = EditText(terminal.activity)
    luaInput.imeOptions = EditorInfo.IME_ACTION_DONE
    luaInput.isSingleLine = true
    luaInput.hint = "Enter your input here"
    val terminateBtn = TextView(terminal.activity)
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
        terminal.binding.cmdInput.visibility = View.VISIBLE
        terminal.binding.cmdInput.isEnabled = true
        terminal.binding.username.text = originalUsernameText
        terminal.binding.cmdInput.requestFocus()
        // terminate the lua script
        luaExecutor.terminate()
    }
    terminal.binding.cmdInput.visibility = View.GONE
    terminal.binding.inputLineLayout.addView(luaInput)
    terminal.binding.terminalOutput.addView(terminateBtn)
    luaInput.requestFocus()
    luaInput.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            val input = luaInput.text.toString()
            terminal.binding.terminalOutput.removeView(terminateBtn)
            terminal.binding.inputLineLayout.removeView(luaInput)
            terminal.binding.cmdInput.visibility = View.VISIBLE
            terminal.binding.cmdInput.isEnabled = true
            terminal.binding.username.text = originalUsernameText
            terminal.binding.cmdInput.requestFocus()
            callback(input)
        }
        false
    }
}
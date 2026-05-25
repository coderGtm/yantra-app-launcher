package com.coderGtm.yantra.commands.run

import android.view.View
import com.coderGtm.yantra.LuaExecutor
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.ui.screens.main.LuaInputSession


fun requestInput(luaExecutor: LuaExecutor, terminal: Terminal, scriptName: String, callback: (String) -> Unit) {
    val originalUsernameText = terminal.username.text
    switchToLuaInput(terminal, luaExecutor, scriptName, originalUsernameText, callback)
}

private fun switchToLuaInput(
    terminal: Terminal,
    luaExecutor: LuaExecutor,
    scriptName: String,
    originalUsernameText: String,
    callback: (String) -> Unit,
) {
    terminal.binding.cmdInput.isEnabled = false
    terminal.username.text = "$scriptName>"

    var terminateActionId = ""
    terminateActionId = terminal.binding.addActionOutput(
        text = "[Terminate Script]",
        color = terminal.theme.errorTextColor,
        underlined = true,
        fontSize = terminal.preferenceObject.getInt("fontSize", 16).toFloat(),
    ) {
        switchToCmdInput(terminal, terminateActionId = terminateActionId, originalUsernameText = originalUsernameText)
        luaExecutor.terminate()
    }

    terminal.binding.cmdInput.visibility = View.GONE
    terminal.binding.showLuaInput(
        LuaInputSession(
            scriptName = scriptName,
            originalUsernameText = originalUsernameText,
            placeholder = "Enter your input here",
            onSubmit = { input ->
                switchToCmdInput(terminal, terminateActionId = terminateActionId, originalUsernameText = originalUsernameText)
                callback(input)
            },
            onTerminate = {
                switchToCmdInput(terminal, terminateActionId = terminateActionId, originalUsernameText = originalUsernameText)
                luaExecutor.terminate()
            },
            color = terminal.theme.inputLineTextColor,
            fontSize = terminal.preferenceObject.getInt("fontSize", 16).toFloat(),
            cursorColor = terminal.theme.inputLineTextColor,
            typeface = terminal.typeface,
        ),
    )
    terminal.binding.requestLuaInputFocus()
}

private fun switchToCmdInput(terminal: Terminal, terminateActionId: String, originalUsernameText: String) {
    terminal.binding.removeOutputItem(terminateActionId)
    terminal.binding.clearLuaInput()
    terminal.binding.cmdInput.visibility = View.VISIBLE
    terminal.binding.cmdInput.isEnabled = true
    terminal.username.text = originalUsernameText
    terminal.binding.requestCommandInputFocus()
}
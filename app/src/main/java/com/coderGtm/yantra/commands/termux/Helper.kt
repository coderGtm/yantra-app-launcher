package com.coderGtm.yantra.commands.termux

import com.coderGtm.yantra.terminal.Terminal
import org.json.JSONObject

fun handleTermuxResult(result: JSONObject, primaryTerminal: Terminal) {
    if (result.getInt("errCode") != -1) {
        primaryTerminal.output(result.getString("errmsg"), primaryTerminal.theme.errorTextColor, null)
        return
    }
    if (result.getInt("exitCode") != 0) {
        primaryTerminal.output(result.getString("stderr"), primaryTerminal.theme.errorTextColor, null)
        return
    }
    val stdout = result.getString("stdout")
    primaryTerminal.output(stdout, primaryTerminal.theme.resultTextColor, null)
}
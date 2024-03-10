package com.coderGtm.yantra.commands.gupt

import android.content.Intent
import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.activities.WebViewActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "gupt",
        helpTitle = terminal.activity.getString(R.string.cmd_gupt_title),
        description = terminal.activity.getString(R.string.cmd_gupt_help)
    )

    override fun execute(command: String) {
        output(terminal.activity.getString(R.string.initializing_g_u_p_t),terminal.theme.warningTextColor)
        if (!terminal.preferenceObject.getBoolean("gupt___purchased",true)) {
            output("[-] G.U.P.T is a paid add-on feature. Consider buying it to enable it.",terminal.theme.errorTextColor)
            output("Salient features of G.U.P.T:",terminal.theme.warningTextColor, Typeface.BOLD)
            output("--------------------------",terminal.theme.warningTextColor)
            output("1. Launch a private browsing tab inside Yantra Launcher.")
            output("2. All the data is cleared after closing the tab.")
            output("3. You can also open a specific url in the private tab.")
            output("4. Hidden from the recent apps list.")
            output("5. No history is saved.")
            output("6. No cookies are saved.")
            output("7. No more going through the hassle of opening an incognito tab in your browser.")
            output("--------------------------",terminal.theme.warningTextColor)
            val mainAct = terminal.activity as MainActivity
            mainAct.initializeProductPurchase("gupt")
            return
        }
        output(terminal.activity.getString(R.string.getting_undercover_private_tab),terminal.theme.resultTextColor, Typeface.ITALIC)
        val cmdArray = command.split(" ")
        var url = "https://www.google.com"
        if (cmdArray.size > 1) {
            if (cmdArray.size > 2) {
                output(terminal.activity.getString(R.string.gupt_many_params), terminal.theme.errorTextColor)
                return
            }
            url = cmdArray[1]
        }
        terminal.activity.startActivity(Intent(terminal.activity, WebViewActivity::class.java).putExtra("url", url))
        output(terminal.activity.getString(R.string.launched_g_u_p_t),terminal.theme.successTextColor)
    }
}
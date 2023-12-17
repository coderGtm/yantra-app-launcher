package com.coderGtm.yantra.commands.internet

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat.startActivityForResult
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal


class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "internet",
        helpTitle = "internet",
        description = "Opens a panel containing settings to enable internet connection."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'internet' command takes no arguments.", terminal.theme.errorTextColor)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            startActivityForResult(terminal.activity, panelIntent, 0, null)
        }
        else {
            output("Uh oh! I am sorry but it's time to upgrade to Android 10 atleast to use this command :(", terminal.theme.warningTextColor)
        }
    }
}
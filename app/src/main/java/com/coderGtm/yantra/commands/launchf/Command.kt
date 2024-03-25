package com.coderGtm.yantra.commands.launchf

import android.graphics.Typeface
import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.findSimilarity
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "launchf",
        helpTitle = terminal.activity.getString(R.string.cmd_launchf_title),
        description = terminal.activity.getString(R.string.cmd_launchf_help)
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.launchf_no_string), terminal.theme.errorTextColor)
            return
        }
        val name = command.removePrefix(args[0]).trim().lowercase()

        output(terminal.activity.getString(R.string.getting_nearest_match_for, name), terminal.theme.resultTextColor, Typeface.ITALIC)

        val candidates = mutableListOf<Double>()
        val currentPackage = terminal.activity.packageName
        for (app in terminal.appList) {
            val score = if (app.packageName == currentPackage) {
                0.0
            }
            else {
                findSimilarity(app.appName.lowercase(), name)
            }
            candidates.add(score)
        }
        val maxIndex = candidates.indexOf(candidates.max())
        val appBlock = terminal.appList[maxIndex]
        output(terminal.activity.getString(R.string.found_with_max_score, appBlock.appName, candidates.max().toFloat()))
        output(terminal.activity.getString(R.string.launching_app, appBlock.appName, appBlock.packageName), terminal.theme.successTextColor)
        launchApp(this@Command, appBlock)
        if (terminal.preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value) == AppSortMode.RECENT.value) {
            terminal.appList.remove(appBlock)
            terminal.appList.add(0, appBlock)
        }
    }
}
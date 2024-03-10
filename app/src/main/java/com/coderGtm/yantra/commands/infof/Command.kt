package com.coderGtm.yantra.commands.infof

import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.findSimilarity
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "infof",
        helpTitle = terminal.activity.getString(R.string.cmd_infof_title),
        description = terminal.activity.getString(R.string.cmd_infof_help)
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.infof_no_string), terminal.theme.errorTextColor)
            return
        }
        val name = command.removePrefix(args[0]).trim().lowercase()

        output(terminal.activity.getString(R.string.getting_nearest_match_for, name), terminal.theme.resultTextColor, Typeface.ITALIC)

        val candidates = mutableListOf<Double>()
        for (app in terminal.appList) {
            val score = findSimilarity(app.appName.lowercase(), name)
            candidates.add(score)
            //addToPrevTxt(app.appName+" ---> "+score.toString(),4)
        }
        val maxIndex = candidates.indexOf(candidates.max())
        val appBlock = terminal.appList[maxIndex]
        output(terminal.activity.getString(R.string.found_with_max_score, appBlock.appName, candidates.max().toFloat()))
        output(terminal.activity.getString(R.string.launching_settings_for, appBlock.appName, appBlock.packageName), terminal.theme.successTextColor)
        launchAppInfo(this@Command, appBlock)
    }
}
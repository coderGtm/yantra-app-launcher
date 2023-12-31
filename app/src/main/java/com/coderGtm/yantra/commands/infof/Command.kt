package com.coderGtm.yantra.commands.infof

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.findSimilarity
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "infof",
        helpTitle = "infof [approx app name]",
        description = "Opens app settings by matching given app name string using fuzzy search algorithm (Levenshtein distance). Example: 'openf tube' may open system settings for YouTube."
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please give a string to open app settings with fuzzy search.", terminal.theme.errorTextColor)
            return
        }
        val name = command.removePrefix(args[0]).trim().lowercase()
        val candidates = mutableListOf<Double>()
        for (app in terminal.appList) {
            val score = findSimilarity(app.appName.lowercase(), name)
            candidates.add(score)
            //addToPrevTxt(app.appName+" ---> "+score.toString(),4)
        }
        val maxIndex = candidates.indexOf(candidates.max())
        val appBlock = terminal.appList[maxIndex]
        launchAppInfo(this@Command, appBlock)
        output("Opened settings for ${terminal.appList[maxIndex].appName}", terminal.theme.successTextColor)
    }
}
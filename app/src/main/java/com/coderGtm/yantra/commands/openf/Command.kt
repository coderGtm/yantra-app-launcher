package com.coderGtm.yantra.commands.openf

import android.graphics.Typeface
import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.findSimilarity
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "openf",
        helpTitle = "openf [approx app name]",
        description = "Opens app by matching given app name string using fuzzy search algorithm (Levenshtein distance). Example: 'openf tube' may open YouTube."
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please give a string to open app with fuzzy search.", terminal.theme.errorTextColor)
            return
        }
        val name = command.removePrefix(args[0]).trim().lowercase()

        output("Getting nearest match for '$name'...", terminal.theme.resultTextColor, Typeface.ITALIC)

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
        output("+ Found ${appBlock.appName} with max score (${candidates.max()})")
        output("Opening ${appBlock.appName} (${appBlock.packageName})", terminal.theme.successTextColor)
        launchApp(this@Command, appBlock)
        if (terminal.preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value) == AppSortMode.RECENT.value) {
            terminal.appList.remove(appBlock)
            terminal.appList.add(0, appBlock)
        }
    }
}
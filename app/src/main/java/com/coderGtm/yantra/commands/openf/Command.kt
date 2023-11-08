package com.coderGtm.yantra.commands.openf

import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.findSimilarity
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "openf",
        helpTitle = "openf [approx app name]",
        description = "Opens app by matching given app name string using fuzzy search algorithm (Levenshtein distance). Example: 'openf tube' may open YouTube."
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        val name = command.removePrefix(args[0]).trim().lowercase()
        val candidates = mutableListOf<Double>()
        for (app in terminal.appList) {
            val score = findSimilarity(app.appName.lowercase(), name)
            candidates.add(score)
            //addToPrevTxt(app.appName+" ---> "+score.toString(),4)
        }
        val maxIndex = candidates.indexOf(candidates.max())
        val appBlock = terminal.appList[maxIndex]
        terminal.activity.applicationContext.startActivity(terminal.activity.applicationContext.packageManager.getLaunchIntentForPackage(appBlock.packageName))
        output("Opened ${terminal.appList[maxIndex].appName}",terminal.theme.successTextColor)
        if (terminal.preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value) == AppSortMode.RECENT.value) {
            terminal.appList.remove(appBlock)
            terminal.appList.add(0, appBlock)
        }
    }
}
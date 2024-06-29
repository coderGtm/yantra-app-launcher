package com.coderGtm.yantra.commands.bg

import android.app.WallpaperManager
import android.graphics.drawable.ColorDrawable
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.setSystemWallpaper
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "bg",
        helpTitle = terminal.activity.getString(R.string.cmd_bg_title),
        description = terminal.activity.getString(R.string.cmd_bg_help)
    )

    override fun execute(command: String) {
        if (command.trim().lowercase() == "bg") {
            val mainAct = terminal.activity as MainActivity
            mainAct.pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        else if (command.trim().split(" ")[1] == "-1") {
            if (command.trim().split(" ").size > 2) {
                output(terminal.activity.getString(R.string.bg_too_many_args), terminal.theme.warningTextColor)
                return
            }
            val wallpaperManager = WallpaperManager.getInstance(terminal.activity.applicationContext)
            val colorDrawable = ColorDrawable(terminal.theme.bgColor)
            setSystemWallpaper(wallpaperManager, colorDrawable.toBitmap(terminal.activity.resources.displayMetrics.widthPixels, terminal.activity.resources.displayMetrics.heightPixels))
            terminal.preferenceObject.edit().putBoolean("defaultWallpaper",true).apply()
            output(terminal.activity.getString(R.string.removed_wallpaper), terminal.theme.successTextColor)
        }
        else if (command.trim().split(" ")[1] == "random") {
            // -id=123
            // -grayscale
            // -blur=5
            var id = -1
            var grayscale = false
            var blur = 0
            if (command.trim().split(" ").size > 2) {
                for (arg in command.trim().split(" ").subList(2,command.trim().split(" ").size)) {
                    if (arg.startsWith("-id=")) {
                        id = arg.split("=")[1].toInt()
                    }
                    else if (arg == "-grayscale") {
                        grayscale = true
                    }
                    else if (arg.startsWith("-blur=")) {
                        blur = arg.split("=")[1].toInt()
                    }
                    else {
                        output(terminal.activity.getString(R.string.bg_invalid_args),terminal.theme.errorTextColor)
                        return
                    }
                }
            }
            getRandomWallpaper(id,grayscale,blur,this)
        }
        else {
            output(terminal.activity.getString(R.string.bg_invalid_args),terminal.theme.errorTextColor)
        }
    }
}
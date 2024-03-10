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
        if (command.trim() == "bg") {
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
            var query = "wallpaper"
            if (command.trim().split(" ").size == 3) {
                query = command.trim().split(" ")[2]
            }
            else if (command.trim().split(" ").size > 3) {
                output(terminal.activity.getString(R.string.bg_random_too_many_args), terminal.theme.warningTextColor)
                return
            }
            getRandomWallpaper(query, this)
        }
        else {
            output(terminal.activity.getString(R.string.bg_invalid_args),terminal.theme.errorTextColor)
        }
    }
}
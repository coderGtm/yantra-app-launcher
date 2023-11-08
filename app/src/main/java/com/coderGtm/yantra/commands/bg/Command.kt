package com.coderGtm.yantra.commands.bg

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.setSystemWallpaper
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "bg",
        helpTitle = "bg | bg random | bg random [query]",
        description = "'bg' is used to set custom Wallpaper from the Albums. Use 'bg -1' to remove custom Wallpaper and set to solid theme background. Use 'bg random' to fetch a random Wallpaper from the Internet. An optional [query] can be specified with the random command, like 'bg random mountains' or 'bg random texture,dark' to fetch related Wallpaper. Note that this command only changes the Home screen wallpaper, not the Lock screen one."
    )

    override fun execute(command: String) {
        if (command.trim() == "bg") {
            setupPermissions(terminal.activity)
            // select image
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            terminal.activity.startActivityForResult(intent, 0)
        }
        else if (command.trim().split(" ")[1] == "-1") {
            val wallpaperManager = WallpaperManager.getInstance(terminal.activity.applicationContext)
            val colorDrawable = ColorDrawable(terminal.theme.bgColor)
            setSystemWallpaper(wallpaperManager, colorDrawable.toBitmap(terminal.activity.resources.displayMetrics.widthPixels, terminal.activity.resources.displayMetrics.heightPixels))
            terminal.preferenceObject.edit().putBoolean("defaultWallpaper",true).apply()
            output("Removed Wallpaper", terminal.theme.successTextColor)
        }
        else if (command.trim().split(" ")[1] == "random") {
            var query = "wallpaper"
            if (command.trim().split(" ").size == 3) {
                query = command.trim().split(" ")[2]
            }
            else if (command.trim().split(" ").size > 3) {
                output("Too many arguments. Use ',' to separate topics like 'bg random city,night-life'", terminal.theme.warningTextColor)
                return
            }
            getRandomWallpaper(query)
        }
        else {
            output("Invalid argument passed for bg",terminal.theme.errorTextColor)
        }
    }
}
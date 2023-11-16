package com.coderGtm.yantra.commands.theme

import android.app.WallpaperManager
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.setSystemWallpaper
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "theme",
        helpTitle = "theme [index]",
        description = "Applies specified theme to Yantra Launcher. Example: 'theme 3'"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify a theme id", terminal.theme.errorTextColor)
            return
        }
        if (args.size > 2) {
            output("Invalid command usage. 'theme' command takes a single parameter: the theme id", terminal.theme.errorTextColor)
            return
        }
        val id = args[1].trim().toIntOrNull()
        if (id != null) {
            if (id.toInt() in Themes.entries.indices || id.toInt() == -1) {
                if (id.toInt() == -1) {
                    if (!terminal.preferenceObject.getBoolean("customtheme___purchased",false)) {
                        printCustomThemeFeatures(this)
                        val mainAct = terminal.activity as MainActivity
                        mainAct.initializeProductPurchase("customtheme")
                        return
                    }
                    else {
                        output("[+] Opening Custom Theme Designer",terminal.theme.resultTextColor, Typeface.ITALIC)
                        openCustomThemeDesigner(terminal)
                    }
                    return
                }
                terminal.preferenceObject.edit().putInt("theme", id.toInt()).commit()
                if (terminal.preferenceObject.getBoolean("defaultWallpaper",true)) {
                    val wallpaperManager = WallpaperManager.getInstance(terminal.activity.applicationContext)
                    val colorDrawable = ColorDrawable(terminal.theme.bgColor)
                    setSystemWallpaper(wallpaperManager, colorDrawable.toBitmap(terminal.activity.resources.displayMetrics.widthPixels, terminal.activity.resources.displayMetrics.heightPixels))
                }
                toast(terminal.activity.baseContext, "Setting theme to ${Themes.entries[id.toInt()].name}")
                terminal.activity.recreate()
            }
            else output("Theme id out of range(-1 to ${Themes.entries.size-1})", terminal.theme.warningTextColor)
        }
        else {
            output("Theme id must be an integer", terminal.theme.errorTextColor)
        }
    }
}
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
        helpTitle = "theme [name]",
        description = "Applies specified theme to Yantra Launcher. Example: 'theme Tokyonight'"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify a theme name", terminal.theme.errorTextColor)
            return
        }
        if (args.size > 2) {
            output("Invalid command usage. 'theme' command takes a single parameter: the theme name!", terminal.theme.errorTextColor)
            return
        }
        val name = args[1].trim().lowercase()
        if (Themes.entries.any { it.name.lowercase() == name } || name == "custom") {
            if (name == "custom") {
                if (!terminal.preferenceObject.getBoolean("customtheme___purchased",false)) {
                    printCustomThemeFeatures(this)
                    val mainAct = terminal.activity as MainActivity
                    mainAct.initializeProductPurchase("customtheme")
                    return
                }
                else {
                    output("[+] Launching Custom Theme Designer",terminal.theme.resultTextColor, Typeface.ITALIC)
                    openCustomThemeDesigner(terminal)
                }
                return
            }
            val theme = Themes.entries.first { it.name.lowercase() == name }
            terminal.preferenceObject.edit().putInt("theme", theme.ordinal).apply()
            if (terminal.preferenceObject.getBoolean("defaultWallpaper",true)) {
                val wallpaperManager = WallpaperManager.getInstance(terminal.activity.applicationContext)
                val colorDrawable = ColorDrawable(terminal.theme.bgColor)
                setSystemWallpaper(wallpaperManager, colorDrawable.toBitmap(terminal.activity.resources.displayMetrics.widthPixels, terminal.activity.resources.displayMetrics.heightPixels))
            }
            toast(terminal.activity.baseContext, "Setting theme to ${theme.name}")
            terminal.activity.recreate()
        }
        else output("$name is not a theme I know of! Please use 'list themes' to get the list of themes.", terminal.theme.errorTextColor)
    }
}
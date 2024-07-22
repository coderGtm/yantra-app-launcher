package com.coderGtm.yantra.commands.theme

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import com.coderGtm.yantra.R
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
        helpTitle = terminal.activity.getString(R.string.cmd_theme_title),
        description = terminal.activity.getString(R.string.cmd_theme_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.specify_theme), terminal.theme.errorTextColor)
            return
        }
        if (args.size > 3) {
            output(terminal.activity.getString(R.string.only_theme_name_reqd), terminal.theme.errorTextColor)
            return
        }
        val name = args[1].trim().lowercase()

        if (name == "-s") {
            if (args.size < 3) {
                output("Please, specify a name of saving theme", terminal.theme.errorTextColor)
                return
            }

            if (terminal.preferenceObject.getInt("theme", 0) != -1) {
                output("Please, select a custom theme first", terminal.theme.errorTextColor)
                return
            }

            val themeName = args[2].trim().lowercase()
            val saved = terminal.preferenceObject.getString("theme$themeName",null)
            if (Themes.entries.any { it.name.lowercase() == themeName } || themeName == "custom" || saved != null || themeName == "-s" || themeName == "-d") {
                output("Theme already exists", terminal.theme.errorTextColor)
                return
            }

            terminal.preferenceObject.edit().putString(
                "theme$themeName", terminal.preferenceObject.getString(
                "customThemeClrs",
                "#FF000000,#FFA0A0A0,#FFE1BEE7,#FFFAEBD7,#FFEBEBEB,#FFF00000,#FF00C853,#FFFFD600"
            )).apply()

            val savedThemesList = terminal.preferenceObject.getStringSet("savedThemesList", emptySet())?.toMutableSet() ?: mutableSetOf()

            savedThemesList.add(themeName)

            terminal.preferenceObject.edit().putStringSet("savedThemesList", savedThemesList).apply()
            output("Theme saved as $themeName", terminal.theme.resultTextColor)
            return
        }

        if (name == "-d") {
            if (args.size < 3) {
                output("Please, specify a name of deleting theme", terminal.theme.errorTextColor)
                return
            }

            val themeName = args[2].trim().lowercase()
            val saved = terminal.preferenceObject.getString("theme$themeName",null)
            if (saved == null) {
                output("Theme not exist", terminal.theme.errorTextColor)
                return
            }

            val savedThemesList = terminal.preferenceObject.getStringSet("savedThemesList", emptySet())?.toMutableSet() ?: mutableSetOf()
            savedThemesList.remove(themeName)
            terminal.preferenceObject.edit().putStringSet("savedThemesList", savedThemesList).apply()
            terminal.preferenceObject.edit().remove("theme$themeName").apply()
            output("Theme deleted", terminal.theme.resultTextColor)
            return
        }

        val saved = terminal.preferenceObject.getString("theme$name",null)

        if (Themes.entries.any { it.name.lowercase() == name } || name == "custom") {
            if (name == "custom") {
                output(terminal.activity.getString(R.string.launching_custom_theme_designer),terminal.theme.resultTextColor, Typeface.ITALIC)
                openCustomThemeDesigner(terminal)
                return
            }
            val theme = Themes.entries.first { it.name.lowercase() == name }
            terminal.preferenceObject.edit().putInt("theme", theme.ordinal).apply()
            if (terminal.preferenceObject.getBoolean("defaultWallpaper",true)) {
                val wallpaperManager = WallpaperManager.getInstance(terminal.activity.applicationContext)
                val colorDrawable = ColorDrawable(terminal.theme.bgColor)
                setSystemWallpaper(wallpaperManager, colorDrawable.toBitmap(terminal.activity.resources.displayMetrics.widthPixels, terminal.activity.resources.displayMetrics.heightPixels))
            }
            toast(terminal.activity.baseContext,
                terminal.activity.getString(R.string.setting_theme_to, theme.name))
            terminal.activity.recreate()
        }
        else if (saved != null) {
            if (args.size == 3 && args[2].trim().lowercase() == "-e") {

                val fileName = encrypt(this@Command, name, saved)

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_TITLE, fileName)
                }

                val mainAct = terminal.activity as MainActivity
                mainAct.sendThemeLauncher.launch(
                    Intent.createChooser(intent,
                    "Save theme"))
                return
            }

            terminal.preferenceObject.edit().putString("customThemeClrs", saved).apply()
            terminal.preferenceObject.edit().putInt("theme",-1).apply()
            toast(terminal.activity.baseContext,
                terminal.activity.getString(R.string.setting_theme_to, name))
            terminal.activity.recreate()
        }
        else output(terminal.activity.getString(R.string.theme_not_found, name), terminal.theme.errorTextColor)
    }
}
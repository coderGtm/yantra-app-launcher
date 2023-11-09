package com.coderGtm.yantra.commands.theme

import android.app.WallpaperManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.core.graphics.drawable.toBitmap
import com.coderGtm.yantra.R
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.misc.CustomFlag
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.setSystemWallpaper
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

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
                        output("[-] Custom Theme Design is a paid add-on feature. Consider buying it to enable it.",terminal.theme.errorTextColor)
                        output("Salient Features of Custom Theme Design:",terminal.theme.warningTextColor, Typeface.BOLD)
                        output("--------------------------",terminal.theme.warningTextColor)
                        output("1. You can customize the colors of the Terminal to your liking.")
                        output("2. All Customizable options: - Background - Input - Command - Normal Text and Arrow - Error Text - Positive Text - Warning Text - Suggestions")
                        output("3. Fine-tune the CLI to your liking and make it your own!")
                        output("--------------------------",terminal.theme.warningTextColor)
                        //initializeProductPurchase("customtheme")
                        return
                    }
                    else {
                        output("[+] Opening Custom Theme Designer",terminal.theme.resultTextColor, Typeface.ITALIC)
                        val dialog = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                            .setTitle("Customize Your Theme")
                            .setView(R.layout.custom_theme_dialog)
                        val dialogView = LayoutInflater.from(terminal.activity).inflate(R.layout.custom_theme_dialog, null)
                        val bgColorBtn = dialogView?.findViewById<ImageButton>(R.id.bgColorBtn)
                        val cmdColorBtn = dialogView?.findViewById<ImageButton>(R.id.cmdColorBtn)
                        val suggestionsColorBtn = dialogView?.findViewById<ImageButton>(R.id.suggestionsColorBtn)
                        val inputAndBtnsColorBtn = dialogView?.findViewById<ImageButton>(R.id.inputAndBtnsColorBtn)
                        val resultColorBtn = dialogView?.findViewById<ImageButton>(R.id.resultColorBtn)
                        val errorColorBtn = dialogView?.findViewById<ImageButton>(R.id.errorColorBtn)
                        val successColorBtn = dialogView?.findViewById<ImageButton>(R.id.successColorBtn)
                        val warnColorBtn = dialogView?.findViewById<ImageButton>(R.id.warnColorBtn)
                        val customThemeColors = terminal.preferenceObject.getString("customThemeClrs", "#000000,#A0A0A0,#E1BEE7,#FAEBD7,#EBEBEB,#F00000,#00C853,#FFD600")!!.split(",").toMutableList() as ArrayList<String>
                        var i = 0
                        listOf(bgColorBtn, cmdColorBtn, suggestionsColorBtn, inputAndBtnsColorBtn, resultColorBtn, errorColorBtn, successColorBtn, warnColorBtn).forEach { imgBtn ->
                            imgBtn?.setImageDrawable(ColorDrawable(Color.parseColor(customThemeColors[i])))
                            imgBtn?.tag = customThemeColors[i]
                            imgBtn?.setOnClickListener {
                                val colorDialogBuilder = ColorPickerDialog.Builder(terminal.activity, R.style.Theme_AlertDialog)
                                    .setTitle("Select Color")
                                    .setPositiveButton("Set", ColorEnvelopeListener(){ envelope, _->
                                        toast(terminal.activity.baseContext, envelope.hexCode.drop(2).prependIndent("#"))
                                        imgBtn.setImageDrawable(ColorDrawable(Color.parseColor(envelope.hexCode.drop(2).prependIndent("#"))))
                                        imgBtn.tag = envelope.hexCode.drop(2).prependIndent("#")
                                    })
                                    .setNegativeButton("Cancel") { dialogInterface, i ->
                                        dialogInterface.dismiss()
                                    }
                                    .attachAlphaSlideBar(false) // the default value is true.
                                    .attachBrightnessSlideBar(true) // the default value is true.
                                    .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                                //val bubbleFlag = BubbleFlag(this)
                                //bubbleFlag.flagMode = FlagMode.FADE
                                colorDialogBuilder.colorPickerView.flagView = CustomFlag(terminal.activity,
                                    R.layout.color_picker_flag_view
                                )
                                colorDialogBuilder.colorPickerView.setInitialColor(Color.parseColor("#FF"+imgBtn.tag.toString().drop(1)))
                                colorDialogBuilder.show()
                            }
                            i++
                        }
                        dialog.setView(dialogView)
                        dialog.setPositiveButton("Apply") { _, _ ->
                            //get all colors in hex format
                            val bgColor = bgColorBtn?.tag.toString()
                            val cmdColor = cmdColorBtn?.tag.toString()
                            val suggestionsColor = suggestionsColorBtn?.tag.toString()
                            val inputAndBtnsColor = inputAndBtnsColorBtn?.tag.toString()
                            val resultColor = resultColorBtn?.tag.toString()
                            val errorColor = errorColorBtn?.tag.toString()
                            val successColor = successColorBtn?.tag.toString()
                            val warnColor = warnColorBtn?.tag.toString()
                            val customTheme = listOf(bgColor, cmdColor, suggestionsColor, inputAndBtnsColor, resultColor, errorColor, successColor, warnColor)
                            //addToPrevTxt(customTheme.toString().drop(1).dropLast(1),4)
                            //return@setPositiveButton
                            terminal.preferenceObject.edit().putString("customThemeClrs", customTheme.toString().drop(1).dropLast(1).replace(" ","")).commit()
                            terminal.preferenceObject.edit().putInt("theme",-1).apply()
                            toast(terminal.activity.baseContext, "Setting theme to Custom")
                            terminal.activity.recreate()
                        }
                        dialog.show()
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
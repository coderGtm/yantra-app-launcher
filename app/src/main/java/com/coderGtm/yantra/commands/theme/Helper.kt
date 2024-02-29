package com.coderGtm.yantra.commands.theme

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import com.coderGtm.yantra.R
import com.coderGtm.yantra.getCustomThemeColors
import com.coderGtm.yantra.misc.CustomFlag
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

fun printCustomThemeFeatures(command: Command) {
    with(command) {
        output("[-] Custom Theme Design is a paid add-on feature. Consider buying it to enable it.",terminal.theme.errorTextColor)
        output("Salient Features of Custom Theme Design:",terminal.theme.warningTextColor, Typeface.BOLD)
        output("--------------------------",terminal.theme.warningTextColor)
        output("1. You can customize the colors of the Terminal to your liking.")
        output("2. All Customizable options: - Background - Input - Command - Normal Text and Arrow - Error Text - Positive Text - Warning Text - Suggestions")
        output("3. Fine-tune the CLI to your liking and make it your own!")
        output("--------------------------",terminal.theme.warningTextColor)
    }
}
fun openCustomThemeDesigner(terminal: Terminal) {
    val dialog = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
        .setTitle(terminal.activity.getString(R.string.customize_your_theme))
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
    val customThemeColors = getCustomThemeColors(terminal.preferenceObject)
    var i = 0
    listOf(bgColorBtn, cmdColorBtn, suggestionsColorBtn, inputAndBtnsColorBtn, resultColorBtn, errorColorBtn, successColorBtn, warnColorBtn).forEach { imgBtn ->
        imgBtn?.setImageDrawable(ColorDrawable(Color.parseColor(customThemeColors[i])))
        imgBtn?.tag = customThemeColors[i]
        imgBtn?.setOnClickListener {
            MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                .setItems(arrayOf(terminal.activity.getString(R.string.color_picker), terminal.activity.getString(R.string.hex_code))) { _, which ->
                    when (which) {
                        0 -> {
                            val colorDialogBuilder = ColorPickerDialog.Builder(terminal.activity, R.style.Theme_AlertDialog)
                                .setTitle(terminal.activity.getString(R.string.select_color))
                                .setPositiveButton(terminal.activity.getString(R.string.set), ColorEnvelopeListener { envelope, _->
                                    toast(terminal.activity.baseContext, envelope.hexCode.drop(2).prependIndent("#"))
                                    imgBtn.setImageDrawable(ColorDrawable(Color.parseColor(envelope.hexCode.drop(2).prependIndent("#"))))
                                    imgBtn.tag = envelope.hexCode.drop(2).prependIndent("#")
                                })
                                .setNegativeButton(terminal.activity.getString(R.string.cancel)) { dialogInterface, i ->
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
                            terminal.activity.runOnUiThread { colorDialogBuilder.show() }
                        }
                        1 -> {
                            terminal.activity.runOnUiThread {
                                val hexDialog = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                                    .setTitle(terminal.activity.getString(R.string.enter_6_digit_hex_code_without_hash))
                                    .setView(R.layout.dialog_singleline_input)
                                    .setPositiveButton(terminal.activity.getString(R.string.set)) { dialog, _ ->
                                        val hexCode = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString().trim()
                                        if (!isValidHexCode(hexCode)) {
                                            toast(terminal.activity.baseContext, terminal.activity.getString(R.string.invalid_hex_code))
                                            return@setPositiveButton
                                        }
                                        imgBtn.setImageDrawable(ColorDrawable(Color.parseColor("#$hexCode")))
                                        imgBtn.tag = "#$hexCode"
                                    }
                                    .setNegativeButton(terminal.activity.getString(R.string.cancel)) { dialogInterface, i ->
                                        dialogInterface.dismiss()
                                    }
                                    .show()
                                // prefilled hex code, without the #
                                hexDialog.findViewById<EditText>(R.id.bodyText)?.setText(imgBtn.tag.toString().drop(1))
                            }
                        }
                    }
                }
                .show()
        }
        i++
    }
    dialog.setView(dialogView)
    dialog.setPositiveButton(terminal.activity.getString(R.string.apply)) { _, _ ->
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
        toast(terminal.activity.baseContext, terminal.activity.getString(R.string.setting_theme_to, "Custom"))
        terminal.activity.recreate()
    }
    terminal.activity.runOnUiThread { dialog.show() }
}

fun isValidHexCode(hexCode: String): Boolean {
    return try {
        Color.parseColor("#FF$hexCode")
        true
    }
    catch (e: Exception) {
        false
    }
}
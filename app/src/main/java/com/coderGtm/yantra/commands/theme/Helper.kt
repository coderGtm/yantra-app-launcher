package com.coderGtm.yantra.commands.theme

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.graphics.drawable.toBitmap
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.commands.backup.Command
import com.coderGtm.yantra.getCurrentTheme
import com.coderGtm.yantra.getCustomThemeColors
import com.coderGtm.yantra.isValidHexCode
import com.coderGtm.yantra.misc.CustomFlag
import com.coderGtm.yantra.setSystemWallpaper
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import androidx.core.graphics.drawable.toDrawable
import org.json.JSONObject

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
    val suggestionsBgColorBtn = dialogView?.findViewById<ImageButton>(R.id.suggestionsBgColorBtn)
    val suggestionsColorBtn = dialogView?.findViewById<ImageButton>(R.id.suggestionsColorBtn)
    val inputAndBtnsColorBtn = dialogView?.findViewById<ImageButton>(R.id.inputAndBtnsColorBtn)
    val resultColorBtn = dialogView?.findViewById<ImageButton>(R.id.resultColorBtn)
    val errorColorBtn = dialogView?.findViewById<ImageButton>(R.id.errorColorBtn)
    val successColorBtn = dialogView?.findViewById<ImageButton>(R.id.successColorBtn)
    val warnColorBtn = dialogView?.findViewById<ImageButton>(R.id.warnColorBtn)
    val customThemeColors = getCustomThemeColors(terminal.preferenceObject)
    var i = 0
    listOf(bgColorBtn, cmdColorBtn, suggestionsBgColorBtn, suggestionsColorBtn, inputAndBtnsColorBtn, resultColorBtn, errorColorBtn, successColorBtn, warnColorBtn).forEach { imgBtn ->
        imgBtn?.setImageDrawable(ColorDrawable(Color.parseColor(customThemeColors[i])))
        imgBtn?.tag = customThemeColors[i]
        imgBtn?.setOnClickListener {
            YantraLauncherDialog(terminal.activity).selectItem(
                title = terminal.activity.getString(R.string.select_color),
                items = arrayOf(terminal.activity.getString(R.string.color_picker), terminal.activity.getString(R.string.hex_code)),
                clickAction = { which ->
                    when (which) {
                        0 -> {
                            val colorDialogBuilder = ColorPickerDialog.Builder(terminal.activity, R.style.Theme_AlertDialog)
                                .setTitle(terminal.activity.getString(R.string.select_color))
                                .setPositiveButton(terminal.activity.getString(R.string.set), ColorEnvelopeListener { envelope, _->
                                    toast(terminal.activity.baseContext, envelope.hexCode.prependIndent("#"))
                                    imgBtn.setImageDrawable(ColorDrawable(Color.parseColor(envelope.hexCode.prependIndent("#"))))
                                    imgBtn.tag = envelope.hexCode.prependIndent("#")
                                })
                                .setNegativeButton(terminal.activity.getString(R.string.cancel)) { dialogInterface, i ->
                                    dialogInterface.dismiss()
                                }
                                .attachAlphaSlideBar(true) // the default value is true.
                                .attachBrightnessSlideBar(true) // the default value is true.
                                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                            //val bubbleFlag = BubbleFlag(this)
                            //bubbleFlag.flagMode = FlagMode.FADE
                            colorDialogBuilder.colorPickerView.flagView = CustomFlag(terminal.activity,
                                R.layout.color_picker_flag_view
                            )
                            colorDialogBuilder.colorPickerView.setInitialColor(Color.parseColor(imgBtn.tag.toString()))
                            terminal.activity.runOnUiThread { colorDialogBuilder.show() }
                        }
                        1 -> {
                            terminal.activity.runOnUiThread {
                                YantraLauncherDialog(terminal.activity).takeInput(
                                    title = terminal.activity.getString(R.string.enter_8_digit_hex_code_without_hash),
                                    message = terminal.activity.getString(R.string.enter_8_digit_hex_code_without_hash),
                                    initialInput = imgBtn.tag.toString().drop(1),
                                    positiveButton = terminal.activity.getString(R.string.set),
                                    negativeButton = terminal.activity.getString(R.string.cancel),
                                    positiveAction = {
                                        val hexCode = it.trim()
                                        if (!isValidHexCode(hexCode)) {
                                            toast(terminal.activity.baseContext, terminal.activity.getString(R.string.invalid_hex_code))
                                            return@takeInput
                                        }
                                        imgBtn.setImageDrawable(ColorDrawable(Color.parseColor("#$hexCode")))
                                        imgBtn.tag = "#$hexCode"
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
        i++
    }
    dialog.setView(dialogView)
    dialog.setPositiveButton(terminal.activity.getString(R.string.apply)) { _, _ ->
        //get all colors in hex format
        val bgColor = bgColorBtn?.tag.toString()
        val cmdColor = cmdColorBtn?.tag.toString()
        val suggestionsBgColor = suggestionsBgColorBtn?.tag.toString()
        val suggestionsColor = suggestionsColorBtn?.tag.toString()
        val inputAndBtnsColor = inputAndBtnsColorBtn?.tag.toString()
        val resultColor = resultColorBtn?.tag.toString()
        val errorColor = errorColorBtn?.tag.toString()
        val successColor = successColorBtn?.tag.toString()
        val warnColor = warnColorBtn?.tag.toString()
        val customTheme = listOf(bgColor, cmdColor, suggestionsBgColor, suggestionsColor, inputAndBtnsColor, resultColor, errorColor, successColor, warnColor)
        //addToPrevTxt(customTheme.toString().drop(1).dropLast(1),4)
        //return@setPositiveButton
        terminal.preferenceObject.edit().putString("customThemeClrs", customTheme.toString().drop(1).dropLast(1).replace(" ","")).commit()
        terminal.preferenceObject.edit().putInt("theme",-1).apply()
        toast(terminal.activity.baseContext, terminal.activity.getString(R.string.setting_theme_to, "Custom"))
        terminal.activity.recreate()
    }
    terminal.activity.runOnUiThread { dialog.show() }
}

fun showNameInputDialog(terminal: Terminal, onResult: (String) -> Unit) {
    val input = EditText(terminal.activity).apply {
        hint = "Enter name"
        inputType = InputType.TYPE_CLASS_TEXT
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        filters = arrayOf(InputFilter.LengthFilter(15))
        setSelection(text.length)
    }

    val container = LinearLayout(terminal.activity).apply {
        setPadding(50, 20, 50, 10)
        addView(input)
    }

    MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
        .setTitle("Enter name")
        .setMessage("Use no more than 15 characters, and at least 3 characters")
        .setView(container)
        .setPositiveButton("Apply") { _, _ ->
            val enteredName = input.text.toString().trim()
            if (enteredName.isNotEmpty() && enteredName.length > 2) {
                onResult(enteredName.lowercase())
            } else {
                toast(terminal.activity, "Invalid name")
            }
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()

    input.requestFocus()
    val imm = terminal.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
}

fun getTheme(preferenceObject: SharedPreferences, name: String): String {
    return preferenceObject.getString("theme$name", "").toString()
}

fun saveCurrentTheme(terminal: Terminal) {
    showNameInputDialog(terminal) { enteredName ->
        val themes = terminal.preferenceObject.getString("savedThemeList", null)?.split(",")?.toMutableList() ?: mutableListOf()
        themes.add(enteredName)

        terminal.preferenceObject.edit().putString("theme$enteredName", terminal.preferenceObject.getString("customThemeClrs", "")).apply()
        terminal.preferenceObject.edit().putString("savedThemeList", themes.joinToString(",")).apply()
        toast(terminal.activity, "Saved as $enteredName")
    }
}

fun showThemesListDialog(terminal: Terminal, allThemes: MutableList<String>) {
    MaterialAlertDialogBuilder(terminal.activity,  R.style.Theme_AlertDialog)
        .setTitle("Select theme for export")
        .setItems(allThemes.toTypedArray()) { _, which ->
            val selectedTheme = allThemes[which]
            val fileName = packTheme(terminal, selectedTheme)

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_TITLE, fileName)
            }

            ExportState.pendingFileName = fileName

            terminal.output("Export...", terminal.theme.commandColor, null)

            val mainAct = terminal.activity as MainActivity
            mainAct.exportThemeLauncher.launch(Intent.createChooser(intent,
                terminal.activity.getString(R.string.save_backup_file)))
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun packTheme(terminal: Terminal, themeName: String): String {
    val plainFile = File("${terminal.activity.filesDir}", "$themeName.ytf")

    val theme = terminal.preferenceObject.getString("theme$themeName", "")?.split(",")
    val json = JSONObject().apply {
        put("name", themeName)
        put("bgColor", theme?.getOrNull(0))
        put("cmdColor", theme?.getOrNull(1))
        put("suggestionsBgColor", theme?.getOrNull(2))
        put("suggestionsColor", theme?.getOrNull(3))
        put("inputAndBtnsColor", theme?.getOrNull(4))
        put("resultColor", theme?.getOrNull(5))
        put("errorColor", theme?.getOrNull(6))
        put("successColor", theme?.getOrNull(7))
        put("warnColor", theme?.getOrNull(8))
    }

    plainFile.writeText(json.toString())

    return plainFile.name
}

object ExportState {
    var pendingFileName: String? = null
}

fun exportTheme(terminal: Terminal) {
    val themes = terminal.preferenceObject.getString("savedThemeList", "")?.split(",")?.toMutableList() ?: mutableListOf()
    themes.remove("")

    showThemesListDialog(terminal, themes)
}

fun copyFileToInternalStorage(act: Activity, uri: Uri) {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
        inputStream = act.contentResolver.openInputStream(uri) ?: return

        val fileName = getFileNameFromUri(uri) ?: return
        val outputFile = File(act.filesDir, fileName)
        outputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush()
    } catch (_: IOException) {
    } finally {
        try {
            inputStream?.close()
            outputStream?.close()
        } catch (_: IOException) {
        }

        importThemeFromFile(act, uri)
    }
}

fun renameFile(oldFile: File, newFileName: String): File {
    val directory = oldFile.parentFile
    val newFile = File(directory, newFileName)
    oldFile.renameTo(newFile)
    return newFile
}

fun getText(file: File): String {
    return try {
        val text = file.readText()
        val json = JSONObject(text)

        val requiredKeys = listOf(
            "bgColor", "cmdColor", "suggestionsBgColor",
            "suggestionsColor", "inputAndBtnsColor", "resultColor",
            "errorColor", "successColor", "warnColor"
        )

        val name = json.getString("name")
        if (name.length > 15 || name.length < 3 || name.contains("\n")) return "invalid"
        val values = mutableListOf<String>()

        for (key in requiredKeys) {
            if (!json.has(key)) return "invalid"
            val value = json.getString(key)
            if (!isValidColor(value)) return "invalid"
            values.add(value)
        }

        "$name\n${values.joinToString(",")}"
    } catch (_: Exception) {
        "invalid"
    }
}

fun isValidColor(colorString: String): Boolean {
    return try {
        Color.parseColor(colorString)
        true
    } catch (_: IllegalArgumentException) {
        false
    }
}

fun importThemeFromFile(activity: Activity, uri: Uri) {
    val fileName = getFileNameFromUri(uri) ?: return
    val file = File(activity.filesDir, fileName)
    val fileToExtract = renameFile(file, "theme.txt")

    val extractedText = getText(fileToExtract)
    if (extractedText == "invalid") {
        toast(activity, "File is corrupted")
        return
    }
    val splitOfText = extractedText.split("\n");

    val mainAct = activity as MainActivity

    if (mainAct.getPreferenceObject().getString("theme${splitOfText[0]}", "") != "") {
        toast(activity, "Theme ${splitOfText[0]} already exists")
        fileToExtract.delete()
        return
    }

    val themes = mainAct.getPreferenceObject().getString("savedThemeList", null)?.split(",")?.toMutableList() ?: mutableListOf()
    themes.add(splitOfText[0])

    mainAct.getPreferenceObject().edit().putString("theme${splitOfText[0]}", splitOfText[1]).apply()
    mainAct.getPreferenceObject().edit().putString("savedThemeList", themes.joinToString(",")).apply()
    fileToExtract.delete()

    setCustomTheme(activity, splitOfText[0], splitOfText[1])
    toast(activity, "Imported ${splitOfText[0]}")
}

private fun getFileNameFromUri(uri: Uri): String? {
    val path = uri.lastPathSegment ?: return null
    val index = path.lastIndexOf('/')
    val index2 = path.lastIndexOf(':')
    return when {
        index != -1 -> path.substring(index + 1)
        index2 != -1 -> path.substring(index2 + 1)
        else -> path
    }
}

fun importTheme(terminal: Terminal) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, null as Uri?)
        }
    }
    val mainAct = terminal.activity as MainActivity
    mainAct.getThemeFile.launch(Intent.createChooser(intent, "Choose YTF file"))
}

fun setCustomTheme(activity: Activity, name: String, savedTheme: String){
    val activity = activity as MainActivity
    val preferenceObject = activity.getPreferenceObject()
    preferenceObject.edit().putInt("theme", -1).apply()
    preferenceObject.edit().putString("customThemeClrs", savedTheme).commit()
    if (preferenceObject.getBoolean("defaultWallpaper",true)) {
        val wallpaperManager = WallpaperManager.getInstance(activity.applicationContext)
        val theme = getCurrentTheme(activity, preferenceObject)
        val colorDrawable = theme.bgColor.toDrawable()
        setSystemWallpaper(wallpaperManager, colorDrawable.toBitmap(activity.resources.displayMetrics.widthPixels, activity.resources.displayMetrics.heightPixels))
    }
    toast(activity.baseContext,
        activity.getString(R.string.setting_theme_to, name))
    activity.recreate()
}

fun showThemesDeleteDialog(terminal: Terminal, allThemes: MutableList<String>) {
    MaterialAlertDialogBuilder(terminal.activity,  R.style.Theme_AlertDialog)
        .setTitle("Select theme to delete")
        .setItems(allThemes.toTypedArray()) { _, which ->
            val selectedTheme = allThemes[which]
            terminal.preferenceObject.edit().remove("theme$selectedTheme").apply()
            val themes = terminal.preferenceObject.getString("savedThemeList", null)?.split(",")?.toMutableList() ?: mutableListOf()
            themes.remove(selectedTheme)

            if (themes.isEmpty()) {
                terminal.preferenceObject.edit().remove("savedThemeList").apply()
            } else {
                terminal.preferenceObject.edit()
                    .putString("savedThemeList", themes.joinToString(",")).apply()
            }
            terminal.output("Removed Successfully", terminal.theme.commandColor, null)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun removeTheme(terminal: Terminal) {
    val themes = terminal.preferenceObject.getString("savedThemeList", null)?.split(",")?.toMutableList() ?: mutableListOf()
    if (terminal.preferenceObject.getString("savedThemeList", null) == null) {
        toast(terminal.activity, "No themes to remove")
        return
    }
    showThemesDeleteDialog(terminal, themes)
}
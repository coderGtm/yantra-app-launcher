package com.coderGtm.yantra.commands.theme

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.core.content.edit
import com.coderGtm.yantra.R
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.commands.backup.Command
import com.coderGtm.yantra.getCustomThemeColors
import com.coderGtm.yantra.isValidHexCode
import com.coderGtm.yantra.misc.CustomFlag
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

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
        terminal.preferenceObject.edit(commit = true) {
            putString(
                "customThemeClrs",
                customTheme.toString().drop(1).dropLast(1).replace(" ", "")
            )
        }
        activateCustomTheme(terminal.activity)
    }
    terminal.activity.runOnUiThread { dialog.show() }
}

/**
 * Activates the custom theme by setting the theme to -1 and recreating the activity.
 *
 * @param activity The activity context.
 * @param name The name of the custom theme, default is "Custom".
 */
fun activateCustomTheme(activity: Activity, name: String = "Custom") {
    val mainActivity = activity as MainActivity
    val preferenceObject = mainActivity.getPreferenceObject()
    preferenceObject.edit { putInt("theme", -1) }
    preferenceObject.edit { putString("customThemeName", name) }
    toast(activity.baseContext, activity.getString(R.string.setting_theme_to, name))
    activity.recreate()
}

fun saveCurrentTheme(terminal: Terminal) {
    showThemeNameInputDialog(terminal) { enteredName ->
        val themes = getSavedThemeNames(terminal.preferenceObject)

        if (!checkThemeNameAvailability(terminal.preferenceObject, enteredName)) {
            terminal.output("Theme name '$enteredName' is not available. Please choose a different name.", terminal.theme.errorTextColor, null)
            return@showThemeNameInputDialog
        }

        themes.add(enteredName)

        terminal.preferenceObject.edit {
            putString(
                "theme_$enteredName",
                terminal.preferenceObject.getString("customThemeClrs", "")
            )
        }
        terminal.preferenceObject.edit { putString("savedThemeList", themes.joinToString(",")) }
        terminal.output("Theme saved as '$enteredName'. You can now use it with the command 'theme $enteredName'.", terminal.theme.successTextColor, null)
    }
}

fun exportTheme(terminal: Terminal) {
    val themes = getSavedThemeNames(terminal.preferenceObject)

    if (themes.isEmpty()) {
        terminal.output("No themes to export. Please save a theme first to export it.", terminal.theme.errorTextColor, null)
        return
    }

    showThemesExportDialog(terminal, themes)
}

fun importTheme(terminal: Terminal) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/json"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, null as Uri?)
        }
    }
    val mainAct = terminal.activity as MainActivity
    mainAct.getThemeFile.launch(Intent.createChooser(intent, "Choose a JSON theme file."))
}

fun removeTheme(terminal: Terminal) {
    val themes = getSavedThemeNames(terminal.preferenceObject)

    if (themes.isEmpty()) {
        terminal.output("No saved themes to remove.", terminal.theme.errorTextColor, null)
        return
    }

    showThemesDeleteDialog(terminal, themes)
}

fun getSavedThemeNames(preferenceObject: SharedPreferences): MutableList<String> {
    val savedThemeList = preferenceObject.getString("savedThemeList", null)
    return if (savedThemeList.isNullOrEmpty()) {
        mutableListOf()
    } else {
        savedThemeList.split(",").filter { it.isNotEmpty() }.toMutableList()
    }
}

fun showThemeNameInputDialog(terminal: Terminal, onResult: (String) -> Unit) {
    YantraLauncherDialog(terminal.activity).takeInput(
        title = "Enter Theme name",
        message = "Theme names can be between 3 to 15 characters long and must not contain spaces.",
        positiveButton = terminal.activity.getString(R.string.apply),
        negativeButton = terminal.activity.getString(R.string.cancel),
        positiveAction = { enteredName ->
            val trimmedName = enteredName.trim().lowercase()    // silently trim and lowercase the input
            if (validateThemeName(trimmedName)) {
                onResult(trimmedName)
            } else {
                terminal.output("Invalid theme name.", terminal.theme.errorTextColor, null)
            }
        }
    )
}

/**
 * Validates the theme name.
 *
 * Rules for a valid theme name:
 * - Must be between 3 and 15 characters long.
 * - Must not contain spaces.
 *
 * @param name The name of the theme to validate.
 *
 * @return True if the name is valid, false otherwise.
 */
fun validateThemeName(name: String): Boolean {
    return name.length in 3..15 && !name.contains(' ')
}

/**
 * Checks if the entered theme name is available.
 *
 * A theme name is available if:
 * - It is not already a saved theme.
 * - It is not one of the built-in themes.
 * - It is not "custom".
 *
 * @param preferenceObject The SharedPreferences object to check saved themes.
 * @param enteredName The name entered by the user.
 *
 * @return True if the name is available, false otherwise.
 */
fun checkThemeNameAvailability(preferenceObject: SharedPreferences, enteredName: String): Boolean {
    val name = enteredName.trim().lowercase()
    val savedThemeList = getSavedThemeNames(preferenceObject).map { it.lowercase() }
    val inBuiltThemes = Themes.entries.map { it.name.lowercase() }
    return !savedThemeList.contains(name) && !inBuiltThemes.contains(name) && (name != "custom")

}

fun getSavedTheme(preferenceObject: SharedPreferences, name: String): String {
    return preferenceObject.getString("theme_$name", "").toString()
}

fun showThemesExportDialog(terminal: Terminal, allThemes: MutableList<String>) {
    YantraLauncherDialog(terminal.activity).selectItem(
        title = "Select a Theme to Export",
        items = allThemes.toTypedArray(),
        clickAction = { which ->
            val selectedTheme = allThemes[which]
            val fileName = packTheme(terminal, selectedTheme)

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, fileName)
            }

            terminal.output("Exporting '$selectedTheme' Theme...", terminal.theme.successTextColor, null)

            val mainAct = terminal.activity as MainActivity
            mainAct.pendingThemeFileName = fileName
            mainAct.exportThemeLauncher.launch(Intent.createChooser(intent, "Export Theme File"))
        }
    )
}

/**
 * Packs the theme into a file with the given name.
 *
 * Creates a JSON object with the theme properties and writes it to a file
 * in the internal storage of the application.
 *
 * @param terminal The terminal instance.
 * @param themeName The name of the theme to pack.
 *
 * @return The name of the packed theme file.
 */
fun packTheme(terminal: Terminal, themeName: String): String {
    val plainFile = File("${terminal.activity.filesDir}", "$themeName.json")

    val theme = terminal.preferenceObject.getString("theme_$themeName", "")?.split(",")
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

/**
 * Copies a file from the given URI to the internal storage of the application.
 *
 * This function is used to import a theme file from external storage.
 *
 * @param act The activity context.
 * @param uri The URI of the file to copy.
 */
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

/**
 * Imports a theme from a file.
 *
 * This function reads the theme file, extracts the values, and saves the theme
 * to the preferences. If the theme already exists, it shows a toast message.
 *
 * @param activity The activity context.
 * @param uri The URI of the theme file to import.
 */
fun importThemeFromFile(activity: Activity, uri: Uri) {
    val fileName = getFileNameFromUri(uri) ?: return
    val file = File(activity.filesDir, fileName)
    val fileToExtract = renameFile(file, "theme.txt")

    val extractedValues = getThemeFileValues(fileToExtract)
    if (extractedValues == "invalid") {
        toast(activity, "The Theme File is Corrupted!")
        return
    }
    val splitOfValues = extractedValues.split("\n");

    val themeName = splitOfValues[0].trim()

    val mainAct = activity as MainActivity

    if (mainAct.getPreferenceObject().getString("theme_$themeName", "") != "") {
        toast(activity, "Theme $themeName already exists")
        fileToExtract.delete()
        return
    }

    val themes = mainAct.getPreferenceObject().getString("savedThemeList", null)?.split(",")?.toMutableList() ?: mutableListOf()
    themes.add(themeName)

    mainAct.getPreferenceObject().edit { putString("theme_$themeName", splitOfValues[1]) }
    mainAct.getPreferenceObject().edit { putString("savedThemeList", themes.joinToString(",")) }
    fileToExtract.delete()

    setSavedTheme(activity, themeName, splitOfValues[1])
    toast(activity, "Imported $themeName")
}

/**
 * Renames the file to the new name in the same directory.
 *
 * @param oldFile The file to rename.
 * @param newFileName The new name for the file.
 *
 * @return The renamed file.
 */
fun renameFile(oldFile: File, newFileName: String): File {
    val directory = oldFile.parentFile
    val newFile = File(directory, newFileName)
    oldFile.renameTo(newFile)
    return newFile
}

/**
 * Reads the theme file and extracts the values.
 * Returns a string with the theme name and colors, or "invalid" if the file is not valid.
 *
 * @param file The theme file to read.
 *
 * @return A string with the theme name and colors, or "invalid" if the file is not valid.
 */
fun getThemeFileValues(file: File): String {
    return try {
        val text = file.readText()
        val json = JSONObject(text)

        val requiredKeys = listOf(
            "bgColor", "cmdColor", "suggestionsBgColor",
            "suggestionsColor", "inputAndBtnsColor", "resultColor",
            "errorColor", "successColor", "warnColor"
        )

        val name = json.getString("name")
        if (!validateThemeName(name)) return "invalid"
        val values = mutableListOf<String>()

        for (key in requiredKeys) {
            if (!json.has(key)) return "invalid"
            val value = json.getString(key).removePrefix("#")
            if (!isValidHexCode(value)) return "invalid"
            values.add("#$value")
        }

        if (values.size + 1 != json.keys().asSequence().count()) return "invalid"

        "$name\n${values.joinToString(",")}"
    } catch (_: Exception) {
        "invalid"
    }
}

/**
 * Extracts the file name from the URI.
 * Handles both content and file URIs.
 *
 * @param uri The URI to extract the file name from.
 *
 * @return The file name as a string, or null if it cannot be determined.
 */
private fun getFileNameFromUri(uri: Uri): String? {
    val path = uri.lastPathSegment ?: return null

    // For content URIs, we use the last index of '/'
    val contentIndex = path.lastIndexOf('/')
    // For file URIs, we can use the last index of ':'
    val fileIndex = path.lastIndexOf(':')

    return when {
        // Handles content URIs
        contentIndex != -1 -> path.substring(contentIndex + 1)
        // Handles file URIs
        fileIndex != -1 -> path.substring(fileIndex + 1)
        // If neither index is found, return the whole path
        else -> path
    }
}

/**
 * Sets the custom theme with the given name and saved theme colors.
 *
 * The custom theme colors are updated to the saved theme colors,
 * and then the theme is set to Custom internally.
 *
 * @param activity The activity context.
 * @param name The name of the custom theme.
 * @param savedTheme The saved theme colors in a string format.
 */
fun setSavedTheme(activity: Activity, name: String, savedTheme: String){
    val mainActivity = activity as MainActivity
    val preferenceObject = mainActivity.getPreferenceObject()
    preferenceObject.edit { putInt("theme", -1) }
    preferenceObject.edit(commit = true) { putString("customThemeClrs", savedTheme) }
    activateCustomTheme(activity, name)
}

fun showThemesDeleteDialog(terminal: Terminal, allThemes: MutableList<String>) {
    YantraLauncherDialog(terminal.activity).selectItem(
        title = "Select theme to delete",
        items = allThemes.toTypedArray(),
        clickAction = { which ->
            val selectedTheme = allThemes[which]
            terminal.preferenceObject.edit { remove("theme_$selectedTheme") }
            allThemes.remove(selectedTheme)

            if (allThemes.isEmpty()) {
                terminal.preferenceObject.edit { remove("savedThemeList") }
            } else {
                terminal.preferenceObject.edit {
                    putString("savedThemeList", allThemes.joinToString(","))
                }
            }
            terminal.output("Removed Successfully", terminal.theme.commandColor, null)
        }
    )
}
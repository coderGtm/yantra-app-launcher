package com.coderGtm.yantra.misc

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.coderGtm.yantra.AI_SYSTEM_PROMPT
import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.DEFAULT_AI_API_DOMAIN
import com.coderGtm.yantra.R
import com.coderGtm.yantra.databinding.ActivitySettingsBinding
import com.coderGtm.yantra.getUserNamePrefix
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.setUserNamePrefix
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun setOrientationTvText(binding: ActivitySettingsBinding, orientation: Int) {
    binding.tvOrientation.text = when (orientation) {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
            "Portrait"
        }
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
            "Landscape"
        }
        ActivityInfo.SCREEN_ORIENTATION_USER -> {
            "System"
        }
        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR -> {
            "Full Sensor"
        }
        else -> {
            "Unspecified"
        }
    }
}
fun setAppSugOrderTvText(binding: ActivitySettingsBinding, appSugOrderingMode: Int) {
    binding.tvAppSugOrder.text = when (appSugOrderingMode) {
        AppSortMode.A_TO_Z.value -> {
            "Alphabetically"
        }
        AppSortMode.RECENT.value -> {
            "Recency"
        }
        else -> {
            "Alphabetically"
        }
    }
}
fun changedSettingsCallback(activity: Activity) {
    val finishIntent = Intent()
    finishIntent.putExtra("settingsChanged",true)
    activity.setResult(AppCompatActivity.RESULT_OK,finishIntent)
}

fun openUsernamePrefixSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val usernamePrefixBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Username Prefix")
        .setMessage("Enter a prefix for your username. This will be displayed before your username in the terminal.")
        .setView(R.layout.username_prefix_dialog)
        .setPositiveButton("Save") { dialog, _ ->
            val prefix = (dialog as AlertDialog).findViewById<EditText>(R.id.usernameET)?.text.toString()
            setUserNamePrefix(prefix, preferenceEditObject)
            binding.usernamePrefix.text = getUserNamePrefix(preferenceObject)
            Toast.makeText(activity, "Username Prefix updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    usernamePrefixBuilder.findViewById<EditText>(R.id.usernameET)?.setText(getUserNamePrefix(preferenceObject))
}

fun openDoubleTapActionSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val doubleTapActionBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Double Tap Command")
        .setMessage("Enter a command to be executed when you double tap the terminal.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val command = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("doubleTapCommand",command.trim()).apply()
            Toast.makeText(activity, "Double-tap command updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    doubleTapActionBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("doubleTapCommand","lock"))
}

fun openSwipeRightActionSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val swipeRightActionBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Swipe Right Command")
        .setMessage("Enter a command to be executed when you swipe right on the terminal.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val command = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("swipeRightCommand",command.trim()).apply()
            Toast.makeText(activity, "Swipe right command updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    swipeRightActionBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("swipeRightCommand","echo Right Swipe detected! You can change the command in settings.")!!)
}

fun openSwipeLeftActionSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val swipeLeftActionBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Swipe Left Command")
        .setMessage("Enter a command to be executed when you swipe left on the terminal.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val command = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("swipeLeftCommand",command.trim()).apply()
            Toast.makeText(activity, "Swipe left command updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    swipeLeftActionBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("swipeLeftCommand","echo Left Swipe detected! You can change the command in settings.")!!)
}

fun openNewsWebsiteSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val newsWebsiteBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Change News Website")
        .setMessage("Enter a website URL to be opened when you enter the \"news\" command.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val website = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("newsWebsite",website.trim()).apply()
            Toast.makeText(activity, "News website changed!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    newsWebsiteBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("newsWebsite","https://news.google.com/"))
}

fun openFontSizeSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val fontSizeBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Font Size")
        .setMessage("Enter a font size for the terminal.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val size = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            if (size.toIntOrNull() == null) {
                Toast.makeText(activity, "Invalid font size!", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            preferenceEditObject.putInt("fontSize",size.toInt()).apply()
            binding.fontSizeBtn.text = size
            Toast.makeText(activity, "Font size updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    fontSizeBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getInt("fontSize",16).toString())
}

fun openOrientationSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceEditObject: SharedPreferences.Editor) {
    MaterialAlertDialogBuilder(activity)
        .setTitle("Set Terminal Orientation")
        .setItems(arrayOf("Portrait", "Landscape", "System", "Full Sensor")) { dialog, which ->
            when (which) {
                0 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    ).apply()
                    binding.tvOrientation.text = "Portrait"
                }

                1 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    ).apply()
                    binding.tvOrientation.text = "Landscape"
                }

                2 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_USER
                    ).apply()
                    binding.tvOrientation.text = "System"
                }

                3 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                    ).apply()
                    binding.tvOrientation.text = "Full Sensor"
                }
            }
            Toast.makeText(activity, "Orientation updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .show()
}

fun openAppSugOrderingSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceEditObject: SharedPreferences.Editor) {
    MaterialAlertDialogBuilder(activity)
        .setTitle("App Suggestions Ordering")
        .setItems(arrayOf("Alphabetically", "Recency")) { dialog, which ->
            when (which) {
                0 -> {
                    preferenceEditObject.putInt("appSortMode", AppSortMode.A_TO_Z.value).apply()
                    binding.tvAppSugOrder.text = "Alphabetically"
                }
                1 -> {
                    preferenceEditObject.putInt("appSortMode", AppSortMode.RECENT.value).apply()
                    binding.tvAppSugOrder.text = "Recency"
                }
            }
            Toast.makeText(activity, "App Suggestions Ordering updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .show()
}

fun openTermuxCmdPathSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val termuxCmdPathBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Termux Command Path")
        .setMessage("Enter the path to the directory where the Termux command is located.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val path = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("termuxCmdPath",path.trim()).apply()
            Toast.makeText(activity, "Termux command path updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    termuxCmdPathBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("termuxCmdPath","/data/data/com.termux/files/usr/bin/")!!)
}

fun openTermuxCmdWorkingDirSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val termuxCmdWorkDirBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Termux Command Working Directory")
        .setMessage("Enter the path to the directory where the Termux command will be executed.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val path = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("termuxCmdWorkDir",path.trim()).apply()
            Toast.makeText(activity, "Termux command working directory updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    termuxCmdWorkDirBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("termuxCmdWorkDir","/data/data/com.termux/files/home/")!!)
}

fun openTermuxCmdSessionActionSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val termuxCmdSessionActionBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Termux Command Session Action")
        .setMessage("Enter integer for Termux Session Action. Available : 0,1,2,3. See TermuxConstants.java on GitHub for more info.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val action = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString().toIntOrNull()
            if (action == null || action !in 0..3) {
                Toast.makeText(activity, "Invalid action!", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            preferenceEditObject.putInt("termuxCmdSessionAction",action).apply()
            Toast.makeText(activity, "Termux command session action updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .setNeutralButton("TermuxConstants.java") { _, _ ->
            openURL("https://github.com/termux/termux-app/blob/master/termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java#L1052-L1083", activity)
        }
        .show()
    termuxCmdSessionActionBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getInt("termuxCmdSessionAction",0).toString())
}

fun openAiApiProviderSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val aiApiProviderBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("AI API Provider")
        .setMessage("Enter your AI provider's domain for use in the 'ai' command. Do not provide the entire URL, just the domain is required (For example: api.openai.com")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val domain = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("aiApiDomain",domain.trim()).apply()
            Toast.makeText(activity, "AI API Provider updated! Make sure you use the corresponding API Key", Toast.LENGTH_LONG).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    aiApiProviderBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("aiApiDomain",
        DEFAULT_AI_API_DOMAIN)!!)
}

fun openAiApiKeySetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val aiApiKeyBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("AI API Key")
        .setMessage("Enter your API Key for use in the 'ai' command, corresponding to your AI provider.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val key = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("aiApiKey",key.trim()).apply()
            Toast.makeText(activity, "AI API Key updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    aiApiKeyBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("aiApiKey","")!!)
}

fun openAiSystemPromptSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val aiSystemPromptBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("System Prompt")
        .setMessage("Enter The 'system' prompt for use in the 'ai' command. It dictates the behaviour of the model.")
        .setView(R.layout.dialog_multiline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val prompt = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("aiSystemPrompt",prompt.trim()).apply()
            Toast.makeText(activity, "AI System Prompt updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    aiSystemPromptBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("aiSystemPrompt",
        AI_SYSTEM_PROMPT)!!)
}
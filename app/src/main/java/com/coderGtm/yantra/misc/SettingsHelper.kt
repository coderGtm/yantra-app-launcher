package com.coderGtm.yantra.misc

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.text.InputType
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
        .setTitle(activity.getString(R.string.username_prefix))
        .setMessage(activity.getString(R.string.username_prefix_description))
        .setView(R.layout.username_prefix_dialog)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val prefix = (dialog as AlertDialog).findViewById<EditText>(R.id.usernameET)?.text.toString()
            setUserNamePrefix(prefix, preferenceEditObject)
            binding.usernamePrefix.text = getUserNamePrefix(preferenceObject)
            Toast.makeText(activity,
                activity.getString(R.string.username_prefix_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    usernamePrefixBuilder.findViewById<EditText>(R.id.usernameET)?.setText(getUserNamePrefix(preferenceObject))
}

fun openDoubleTapActionSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val doubleTapActionBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.change_double_tap_command))
        .setMessage(activity.getString(R.string.double_tap_command_description))
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val command = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("doubleTapCommand",command.trim()).apply()
            Toast.makeText(activity,
                activity.getString(R.string.double_tap_command_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    doubleTapActionBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("doubleTapCommand","lock"))
}

fun openSwipeRightActionSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val swipeRightActionBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.change_right_swipe_command))
        .setMessage(activity.getString(R.string.right_swipe_command_description))
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val command = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("swipeRightCommand",command.trim()).apply()
            Toast.makeText(activity,
                activity.getString(R.string.swipe_right_command_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    swipeRightActionBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("swipeRightCommand",activity.getString(R.string.default_right_swipe_text))!!)
}

fun openSwipeLeftActionSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val swipeLeftActionBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.change_left_swipe_command))
        .setMessage(activity.getString(R.string.left_swipe_command_description))
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val command = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("swipeLeftCommand",command.trim()).apply()
            Toast.makeText(activity,
                activity.getString(R.string.swipe_left_command_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    swipeLeftActionBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("swipeLeftCommand",activity.getString(R.string.default_left_swipe_text))!!)
}

fun openNewsWebsiteSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val newsWebsiteBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.change_news_website))
        .setMessage(activity.getString(R.string.news_description))
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val website = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("newsWebsite",website.trim()).apply()
            Toast.makeText(activity,
                activity.getString(R.string.news_website_changed), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    newsWebsiteBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("newsWebsite","https://news.google.com/"))
}

fun openFontSizeSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val fontSizeBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.terminal_font_size))
        .setMessage(activity.getString(R.string.font_size_description))
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val size = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            if (size.toIntOrNull() == null || size.toInt() <= 0 ) {
                Toast.makeText(activity,
                    activity.getString(R.string.invalid_font_size), Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            preferenceEditObject.putInt("fontSize",size.toInt()).apply()
            binding.fontSizeBtn.text = size
            Toast.makeText(activity,
                activity.getString(R.string.font_size_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    fontSizeBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getInt("fontSize",16).toString())
    fontSizeBuilder.findViewById<EditText>(R.id.bodyText)?.inputType = InputType.TYPE_CLASS_NUMBER
}

fun openArrowSizeSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val arrowSizeBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.arrow_keys_size))
        .setMessage(activity.getString(R.string.arrow_size_description))
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val size = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            if (size.toIntOrNull() == null || size.toInt() <= 0 ) {
                Toast.makeText(activity,
                    activity.getString(R.string.invalid_arrow_size), Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            preferenceEditObject.putInt("arrowSize",size.toInt()).apply()
            binding.arrowSizeBtn.text = size
            Toast.makeText(activity,
                activity.getString(R.string.arrow_size_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    arrowSizeBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getInt("arrowSize",65).toString())
    arrowSizeBuilder.findViewById<EditText>(R.id.bodyText)?.inputType = InputType.TYPE_CLASS_NUMBER
}

fun openOrientationSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceEditObject: SharedPreferences.Editor) {
    MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.orientation))
        .setItems(arrayOf(activity.getString(R.string.portrait),
            activity.getString(R.string.landscape),
            activity.getString(R.string.system), activity.getString(R.string.full_sensor))) { dialog, which ->
            when (which) {
                0 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    ).apply()
                    binding.tvOrientation.text = activity.getString(R.string.portrait)
                }

                1 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    ).apply()
                    binding.tvOrientation.text = activity.getString(R.string.landscape)
                }

                2 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_USER
                    ).apply()
                    binding.tvOrientation.text = activity.getString(R.string.system)
                }

                3 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                    ).apply()
                    binding.tvOrientation.text = activity.getString(R.string.full_sensor)
                }
            }
            Toast.makeText(activity,
                activity.getString(R.string.orientation_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .show()
}

fun openAppSugOrderingSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceEditObject: SharedPreferences.Editor) {
    MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.app_suggestions_order))
        .setItems(arrayOf(activity.getString(R.string.alphabetically),
            activity.getString(R.string.recency))) { dialog, which ->
            when (which) {
                0 -> {
                    preferenceEditObject.putInt("appSortMode", AppSortMode.A_TO_Z.value).apply()
                    binding.tvAppSugOrder.text = activity.getString(R.string.alphabetically)
                }
                1 -> {
                    preferenceEditObject.putInt("appSortMode", AppSortMode.RECENT.value).apply()
                    binding.tvAppSugOrder.text = activity.getString(R.string.recency)
                }
            }
            Toast.makeText(activity,
                activity.getString(R.string.app_suggestions_ordering_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .show()
}

fun openTermuxCmdPathSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val termuxCmdPathBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.termux_command_path))
        .setMessage(activity.getString(R.string.termux_cmd_path_description))
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val path = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("termuxCmdPath",path.trim()).apply()
            Toast.makeText(activity,
                activity.getString(R.string.termux_command_path_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    termuxCmdPathBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("termuxCmdPath","/data/data/com.termux/files/usr/bin/")!!)
}

fun openTermuxCmdWorkingDirSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val termuxCmdWorkDirBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.termux_command_working_directory))
        .setMessage(activity.getString(R.string.termux_cmd_working_dir_description))
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val path = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("termuxCmdWorkDir",path.trim()).apply()
            Toast.makeText(activity,
                activity.getString(R.string.termux_command_working_directory_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    termuxCmdWorkDirBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("termuxCmdWorkDir","/data/data/com.termux/files/home/")!!)
}

fun openTermuxCmdSessionActionSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val termuxCmdSessionActionBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.termux_command_session_action))
        .setMessage(activity.getString(R.string.termux_command_session_action_description))
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val action = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString().toIntOrNull()
            if (action == null || action !in 0..3) {
                Toast.makeText(activity,
                    activity.getString(R.string.invalid_action), Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            preferenceEditObject.putInt("termuxCmdSessionAction",action).apply()
            Toast.makeText(activity,
                activity.getString(R.string.termux_command_session_action_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
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
        .setTitle(activity.getString(R.string.change_ai_api_provider))
        .setMessage(activity.getString(R.string.ai_api_provider_description))
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val domain = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("aiApiDomain",domain.trim()).apply()
            Toast.makeText(activity,
                activity.getString(R.string.ai_api_provider_updated), Toast.LENGTH_LONG).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    aiApiProviderBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("aiApiDomain",
        DEFAULT_AI_API_DOMAIN)!!)
}

fun openAiApiKeySetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val aiApiKeyBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.change_ai_api_key))
        .setMessage(activity.getString(R.string.ai_api_key_description))
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val key = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("aiApiKey",key.trim()).apply()
            Toast.makeText(activity,
                activity.getString(R.string.ai_api_key_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    aiApiKeyBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("aiApiKey","")!!)
}

fun openAiSystemPromptSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val aiSystemPromptBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.change_ai_system_prompt))
        .setMessage(activity.getString(R.string.ai_system_prompt_description))
        .setView(R.layout.dialog_multiline_input)
        .setPositiveButton(activity.getString(R.string.save)) { dialog, _ ->
            val prompt = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("aiSystemPrompt",prompt.trim()).apply()
            Toast.makeText(activity,
                activity.getString(R.string.ai_system_prompt_updated), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    aiSystemPromptBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("aiSystemPrompt",
        AI_SYSTEM_PROMPT)!!)
}
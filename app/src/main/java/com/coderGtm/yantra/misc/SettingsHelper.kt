package com.coderGtm.yantra.misc

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.coderGtm.yantra.AI_SYSTEM_PROMPT
import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.DEFAULT_AI_API_DOMAIN
import com.coderGtm.yantra.DEFAULT_SYSINFO_ART
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.databinding.ActivitySettingsBinding
import com.coderGtm.yantra.getUserNamePrefix
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.setUserNamePrefix
import com.coderGtm.yantra.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun setOrientationTvText(activity: Activity, binding: ActivitySettingsBinding, orientation: Int) {
    binding.tvOrientation.text = when (orientation) {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
            activity.getString(R.string.portrait)
        }
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
            activity.getString(R.string.landscape)
        }
        ActivityInfo.SCREEN_ORIENTATION_USER -> {
            activity.getString(R.string.system)
        }
        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR -> {
            activity.getString(R.string.full_sensor)
        }
        else -> {
            "Unspecified"
        }
    }
}
fun setAppSugOrderTvText(activity: Activity, binding: ActivitySettingsBinding, appSugOrderingMode: Int) {
    binding.tvAppSugOrder.text = when (appSugOrderingMode) {
        AppSortMode.A_TO_Z.value -> {
            activity.getString(R.string.alphabetically)
        }
        AppSortMode.RECENT.value -> {
            activity.getString(R.string.recency)
        }
        else -> {
            activity.getString(R.string.alphabetically)
        }
    }
}
fun changedSettingsCallback(activity: Activity) {
    val finishIntent = Intent()
    finishIntent.putExtra("settingsChanged",true)
    activity.setResult(AppCompatActivity.RESULT_OK,finishIntent)
}

fun openUsernamePrefixSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val usernamePrefixDialog = YantraLauncherDialog(activity)
    usernamePrefixDialog.takeInput(
        title = activity.getString(R.string.username_prefix),
        message = activity.getString(R.string.username_prefix_description),
        initialInput = getUserNamePrefix(preferenceObject),
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val prefix = it
            setUserNamePrefix(prefix, preferenceEditObject)
            binding.usernamePrefix.text = getUserNamePrefix(preferenceObject)
            toast(activity, activity.getString(R.string.username_prefix_updated))
            changedSettingsCallback(activity)
        },
    )
}

fun openDoubleTapActionSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val doubleTapActionDialog = YantraLauncherDialog(activity)
    doubleTapActionDialog.takeInput(
        title = activity.getString(R.string.change_double_tap_command),
        message = activity.getString(R.string.double_tap_command_description),
        initialInput = preferenceObject.getString("doubleTapCommand","lock")!!,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val command = it
            preferenceEditObject.putString("doubleTapCommand",command.trim()).apply()
            toast(activity, activity.getString(R.string.double_tap_command_updated))
            changedSettingsCallback(activity)
        },
    )
}

fun openSwipeRightActionSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val swipeRightActionDialog = YantraLauncherDialog(activity)
    swipeRightActionDialog.takeInput(
        title = activity.getString(R.string.change_right_swipe_command),
        message = activity.getString(R.string.right_swipe_command_description),
        initialInput = preferenceObject.getString("swipeRightCommand",activity.getString(R.string.default_right_swipe_text))!!,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val command = it
            preferenceEditObject.putString("swipeRightCommand",command.trim()).apply()
            toast(activity, activity.getString(R.string.swipe_right_command_updated))
            changedSettingsCallback(activity)
        },
    )
}

fun openSwipeLeftActionSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val swipeLeftActionDialog = YantraLauncherDialog(activity)
    swipeLeftActionDialog.takeInput(
        title = activity.getString(R.string.change_left_swipe_command),
        message = activity.getString(R.string.left_swipe_command_description),
        initialInput = preferenceObject.getString("swipeLeftCommand",activity.getString(R.string.default_left_swipe_text))!!,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val command = it
            preferenceEditObject.putString("swipeLeftCommand",command.trim()).apply()
            toast(activity, activity.getString(R.string.swipe_left_command_updated))
            changedSettingsCallback(activity)
        },
    )
}

fun openNewsWebsiteSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val newsWebsiteDialog = YantraLauncherDialog(activity)
    newsWebsiteDialog.takeInput(
        title = activity.getString(R.string.change_news_website),
        message = activity.getString(R.string.news_description),
        initialInput = preferenceObject.getString("newsWebsite","https://news.google.com/")!!,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val website = it
            preferenceEditObject.putString("newsWebsite",website.trim()).apply()
            toast(activity, activity.getString(R.string.news_website_changed))
            changedSettingsCallback(activity)
        },
    )
}

fun openFontSizeSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val fontSizeDialog = YantraLauncherDialog(activity)
    fontSizeDialog.takeInput(
        title = activity.getString(R.string.terminal_font_size),
        message = activity.getString(R.string.font_size_description),
        initialInput = preferenceObject.getInt("fontSize",16).toString(),
        inputType = InputType.TYPE_CLASS_NUMBER,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val size = it
            if (size.toIntOrNull() == null || size.toInt() <= 0 ) {
                toast(activity, activity.getString(R.string.invalid_font_size))
                return@takeInput
            }
            preferenceEditObject.putInt("fontSize",size.toInt()).apply()
            binding.fontSizeBtn.text = size
            toast(activity, activity.getString(R.string.font_size_updated))
            changedSettingsCallback(activity)
        },
    )
}

fun openArrowSizeSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val arrowSizeDialog = YantraLauncherDialog(activity)
    arrowSizeDialog.takeInput(
        title = activity.getString(R.string.arrow_keys_size),
        message = activity.getString(R.string.arrow_size_description),
        initialInput = preferenceObject.getInt("arrowSize",65).toString(),
        inputType = InputType.TYPE_CLASS_NUMBER,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val size = it
            if (size.toIntOrNull() == null || size.toInt() <= 0 ) {
                toast(activity, activity.getString(R.string.invalid_arrow_size))
                return@takeInput
            }
            preferenceEditObject.putInt("arrowSize",size.toInt()).apply()
            binding.arrowSizeBtn.text = size
            toast(activity, activity.getString(R.string.arrow_size_updated))
            changedSettingsCallback(activity)
        },
    )
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

fun openSysinfoArtSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val sysinfoArtDialog = YantraLauncherDialog(activity)
    sysinfoArtDialog.takeInput(
        title = activity.getString(R.string.change_sysinfo_art),
        message = activity.getString(R.string.sysinfo_art_change_description),
        initialInput = preferenceObject.getString("sysinfoArt",DEFAULT_SYSINFO_ART) ?: DEFAULT_SYSINFO_ART,
        inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val art = it
            preferenceEditObject.putString("sysinfoArt",art.trim()).apply()
            toast(activity, activity.getString(R.string.sysinfo_art_updated))
            changedSettingsCallback(activity)
        },
    )
}

fun openTermuxCmdPathSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val termuxCmdPathDialog = YantraLauncherDialog(activity)
    termuxCmdPathDialog.takeInput(
        title = activity.getString(R.string.termux_command_path),
        message = activity.getString(R.string.termux_cmd_path_description),
        initialInput = preferenceObject.getString("termuxCmdPath","/data/data/com.termux/files/usr/bin/")!!,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val path = it
            preferenceEditObject.putString("termuxCmdPath",path.trim()).apply()
            toast(activity, activity.getString(R.string.termux_command_path_updated))
            changedSettingsCallback(activity)
        },
    )
}

fun openTermuxCmdWorkingDirSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val termuxCmdWorkDirDialog = YantraLauncherDialog(activity)
    termuxCmdWorkDirDialog.takeInput(
        title = activity.getString(R.string.termux_command_working_directory),
        message = activity.getString(R.string.termux_cmd_working_dir_description),
        initialInput = preferenceObject.getString("termuxCmdWorkDir","/data/data/com.termux/files/home/")!!,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val path = it
            preferenceEditObject.putString("termuxCmdWorkDir",path.trim()).apply()
            toast(activity, activity.getString(R.string.termux_command_working_directory_updated))
            changedSettingsCallback(activity)
        },
    )
}

fun openTermuxCmdSessionActionSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val termuxCmdSessionActionDialog = YantraLauncherDialog(activity)
    termuxCmdSessionActionDialog.takeInput(
        title = activity.getString(R.string.termux_command_session_action),
        message = activity.getString(R.string.termux_command_session_action_description),
        initialInput = preferenceObject.getInt("termuxCmdSessionAction",0).toString(),
        inputType = InputType.TYPE_CLASS_NUMBER,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val action = it.toIntOrNull()
            if (action == null || action !in 0..3) {
                toast(activity, activity.getString(R.string.invalid_action))
                return@takeInput
            }
            preferenceEditObject.putInt("termuxCmdSessionAction",action).apply()
            toast(activity, activity.getString(R.string.termux_command_session_action_updated))
            changedSettingsCallback(activity)
        },
        negativeButton = "GitHub",
        negativeAction = {
            openURL("https://github.com/termux/termux-app/blob/master/termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java#L1052-L1083", activity)
        }
    )
}

fun openAiApiProviderSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val aiApiProviderDialog = YantraLauncherDialog(activity)
    aiApiProviderDialog.takeInput(
        title = activity.getString(R.string.change_ai_api_provider),
        message = activity.getString(R.string.ai_api_provider_description),
        initialInput = preferenceObject.getString("aiApiDomain",DEFAULT_AI_API_DOMAIN)!!,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val domain = it
            preferenceEditObject.putString("aiApiDomain",domain.trim()).apply()
            toast(activity, activity.getString(R.string.ai_api_provider_updated))
            changedSettingsCallback(activity)
        },
    )
}

fun openAiApiKeySetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val aiApiKeyDialog = YantraLauncherDialog(activity)
    aiApiKeyDialog.takeInput(
        title = activity.getString(R.string.change_ai_api_key),
        message = activity.getString(R.string.ai_api_key_description),
        initialInput = preferenceObject.getString("aiApiKey","")!!,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val key = it
            preferenceEditObject.putString("aiApiKey",key.trim()).apply()
            toast(activity, activity.getString(R.string.ai_api_key_updated))
            changedSettingsCallback(activity)
        },
    )
}

fun openAiSystemPromptSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor) {
    val aiSystemPromptDialog = YantraLauncherDialog(activity)
    aiSystemPromptDialog.takeInput(
        title = activity.getString(R.string.change_ai_system_prompt),
        message = activity.getString(R.string.ai_system_prompt_description),
        initialInput = preferenceObject.getString("aiSystemPrompt",AI_SYSTEM_PROMPT)!!,
        inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE,
        positiveButton = activity.getString(R.string.save),
        positiveAction = {
            val prompt = it
            preferenceEditObject.putString("aiSystemPrompt",prompt.trim()).apply()
            toast(activity, activity.getString(R.string.ai_system_prompt_updated))
            changedSettingsCallback(activity)
        },
    )
}
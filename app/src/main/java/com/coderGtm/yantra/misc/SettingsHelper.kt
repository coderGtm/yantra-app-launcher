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
import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.R
import com.coderGtm.yantra.databinding.ActivitySettingsBinding
import com.coderGtm.yantra.getUserNamePrefix
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

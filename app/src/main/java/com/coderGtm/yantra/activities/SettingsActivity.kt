package com.coderGtm.yantra.activities

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.R
import com.coderGtm.yantra.databinding.ActivitySettingsBinding
import com.coderGtm.yantra.getUserNamePrefix
import com.coderGtm.yantra.misc.changedSettingsCallback
import com.coderGtm.yantra.misc.openAppSugOrderingSetter
import com.coderGtm.yantra.misc.openArrowSizeSetter
import com.coderGtm.yantra.misc.openDoubleTapActionSetter
import com.coderGtm.yantra.misc.openFontSizeSetter
import com.coderGtm.yantra.misc.openOrientationSetter
import com.coderGtm.yantra.misc.openSwipeLeftActionSetter
import com.coderGtm.yantra.misc.openSwipeRightActionSetter
import com.coderGtm.yantra.misc.openUsernamePrefixSetter
import com.coderGtm.yantra.misc.setAppSugOrderTvText
import com.coderGtm.yantra.misc.setOrientationTvText
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SettingsActivity : AppCompatActivity() {

    private var getPrimarySuggestions = true
    private var getSecondarySuggestions = true
    private var fullscreenLauncher = false
    private var vibrationPermission = true  // permission to vibrate on error
    private var showArrowKeys = true
    private var oneTapKeyboardActivation = true
    private var hideKeyboardOnEnter = true
    private var actOnSuggestionTap = false
    private var fontSize = 16
    private var arrowSize = 65
    private var orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    private var appSugOrderingMode = AppSortMode.A_TO_Z.value
    private var appLocale = "en"

    private lateinit var binding: ActivitySettingsBinding

    private val supportedLocales = mapOf(
        "English" to "en",
        "Italiano" to "it",
        "Русский" to "ru",
    )

    private val prefFile = "yantraSP"
    private val preferenceObject: SharedPreferences
        get() = applicationContext.getSharedPreferences(prefFile,0)

    private val preferenceEditObject: SharedPreferences.Editor
        get() {
            val pref: SharedPreferences = applicationContext.getSharedPreferences(prefFile,0)
            return pref.edit()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPrimarySuggestions = preferenceObject.getBoolean("getPrimarySuggestions",true)
        getSecondarySuggestions = preferenceObject.getBoolean("getSecondarySuggestions",true)
        fullscreenLauncher = preferenceObject.getBoolean("fullScreen",false)
        vibrationPermission = preferenceObject.getBoolean("vibrationPermission",true)
        showArrowKeys = preferenceObject.getBoolean("showArrowKeys",true)
        oneTapKeyboardActivation = preferenceObject.getBoolean("oneTapKeyboardActivation",true)
        hideKeyboardOnEnter = preferenceObject.getBoolean("hideKeyboardOnEnter", true)
        actOnSuggestionTap = preferenceObject.getBoolean("actOnSuggestionTap", false)
        fontSize = preferenceObject.getInt("fontSize",16)
        arrowSize = preferenceObject.getInt("arrowSize", 65)
        orientation = preferenceObject.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        appSugOrderingMode = preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value)
        appLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()


        binding.usernamePrefix.text = getUserNamePrefix(preferenceObject)
        binding.fontSizeBtn.text = fontSize.toString()
        binding.arrowSizeBtn.text = arrowSize.toString()
        setOrientationTvText(binding, orientation)
        setAppSugOrderTvText(binding, appSugOrderingMode)
        binding.currentLocale.text = supportedLocales.filterValues { it == appLocale }.keys.firstOrNull() ?: "English"
        binding.prefixLayout.setOnClickListener { openUsernamePrefixSetter(this@SettingsActivity, binding, preferenceObject, preferenceEditObject) }
        binding.doubleTapActionLayout.setOnClickListener { openDoubleTapActionSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.rightSwipeActionLayout.setOnClickListener { openSwipeRightActionSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.leftSwipeActionLayout.setOnClickListener { openSwipeLeftActionSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.fontSizeBtn.setOnClickListener { openFontSizeSetter(this@SettingsActivity, binding, preferenceObject, preferenceEditObject) }
        binding.arrowSizeBtn.setOnClickListener { openArrowSizeSetter(this@SettingsActivity, binding, preferenceObject, preferenceEditObject) }
        binding.orientationLay.setOnClickListener { openOrientationSetter(this@SettingsActivity, binding, preferenceEditObject) }
        binding.appSugOrderingLay.setOnClickListener { openAppSugOrderingSetter(this@SettingsActivity, binding, preferenceEditObject) }

        binding.languageLay.setOnClickListener {
            val keys = supportedLocales.keys.toTypedArray()
            val values = supportedLocales.values.toTypedArray()
            MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.attention))
                .setMessage(getString(R.string.language_change_disclaimer))
                .setPositiveButton(getString(R.string.i_understand)) { dialog, _ ->
                    dialog.dismiss()
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.select_a_language))
                        .setItems(keys) { _, which ->
                            appLocale = values[which]
                            binding.currentLocale.text = keys[which]
                            changedSettingsCallback(this@SettingsActivity)
                            Toast.makeText(this, getString(R.string.changed_app_language_to, keys[which]), Toast.LENGTH_LONG).show()
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(appLocale))
                        }
                        .show()
                }
                .show()
        }

        binding.primarySugSwitch.isChecked = getPrimarySuggestions
        binding.primarySugSwitch.setOnCheckedChangeListener { _, isChecked ->
            getPrimarySuggestions = isChecked
            preferenceEditObject.putBoolean("getPrimarySuggestions",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.secondarySugSwitch.isChecked = getSecondarySuggestions
        binding.secondarySugSwitch.setOnCheckedChangeListener { _, isChecked ->
            getSecondarySuggestions = isChecked
            preferenceEditObject.putBoolean("getSecondarySuggestions",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.fullscreenSwitch.isChecked = fullscreenLauncher
        binding.fullscreenSwitch.setOnCheckedChangeListener { _, isChecked ->
            fullscreenLauncher = isChecked
            preferenceEditObject.putBoolean("fullScreen", isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.vibrationSwitch.isChecked = vibrationPermission
        binding.vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            vibrationPermission = isChecked
            preferenceEditObject.putBoolean("vibrationPermission",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.showArrowSwitch.isChecked = showArrowKeys
        binding.showArrowSwitch.setOnCheckedChangeListener { _, isChecked ->
            showArrowKeys = isChecked
            preferenceEditObject.putBoolean("showArrowKeys",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.oneTapKeyboardActivationSwitch.isChecked = oneTapKeyboardActivation
        binding.oneTapKeyboardActivationSwitch.setOnCheckedChangeListener { _, isChecked ->
            oneTapKeyboardActivation = isChecked
            preferenceEditObject.putBoolean("oneTapKeyboardActivation",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.hideKeyboardOnEnterSwitch.isChecked = hideKeyboardOnEnter
        binding.hideKeyboardOnEnterSwitch.setOnCheckedChangeListener { _, isChecked ->
            hideKeyboardOnEnter = isChecked
            preferenceEditObject.putBoolean("hideKeyboardOnEnter",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.actOnSuggestionTapSwitch.isChecked = actOnSuggestionTap
        binding.actOnSuggestionTapSwitch.setOnCheckedChangeListener { _, isChecked ->
            actOnSuggestionTap = isChecked
            preferenceEditObject.putBoolean("actOnSuggestionTap", isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
    }
}
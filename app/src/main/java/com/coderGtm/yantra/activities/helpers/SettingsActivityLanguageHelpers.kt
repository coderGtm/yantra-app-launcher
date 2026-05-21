package com.coderGtm.yantra.activities.helpers

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.SettingsActivity
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.misc.changedSettingsCallback
import com.google.android.play.core.splitinstall.SplitInstallRequest
import java.util.Locale

internal fun SettingsActivity.openLanguagePicker() {
    val keys   = supportedLocales.keys.toTypedArray()
    val values = supportedLocales.values.toTypedArray()
    YantraLauncherDialog(this).showInfo(
        title          = getString(R.string.attention),
        message        = getString(R.string.language_change_disclaimer),
        positiveButton = getString(R.string.i_understand),
        positiveAction = {
            YantraLauncherDialog(this).selectItem(
                title       = getString(R.string.select_a_language),
                items       = keys,
                clickAction = { which -> downloadLanguage(values[which]) }
            )
        }
    )
}

internal fun SettingsActivity.downloadLanguage(code: String) {
    if (splitInstallManager.installedLanguages.contains(code)) {
        val keys = supportedLocales.keys.toTypedArray()
        localeDisplayName = keys[supportedLocales.values.indexOf(code)]
        changedSettingsCallback(this)
        Toast.makeText(this, getString(R.string.changed_app_language_to, localeDisplayName), Toast.LENGTH_LONG).show()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
        return
    }
    val request = SplitInstallRequest.newBuilder().addLanguage(Locale.forLanguageTag(code)).build()
    splitInstallManager.registerListener(splitInstallStateListener)
    splitInstallManager.startInstall(request)
        .addOnSuccessListener { splitInstallSessionId = it }
        .addOnFailureListener { e ->
            Log.e("SplitInstallManager", "Failed to install language: $code", e)
            Toast.makeText(this, getString(R.string.an_error_occurred_please_try_again), Toast.LENGTH_LONG).show()
        }
        .addOnCompleteListener { splitInstallManager.unregisterListener(splitInstallStateListener) }
    Toast.makeText(this, "Downloading language...", Toast.LENGTH_LONG).show()
}


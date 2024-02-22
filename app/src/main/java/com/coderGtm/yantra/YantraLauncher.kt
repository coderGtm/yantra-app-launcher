package com.coderGtm.yantra

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import org.acra.ReportField
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

class YantraLauncher : Application() {

    lateinit var preferenceObject: SharedPreferences

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        initAcra {
            // core configuration
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON

            reportContent = listOf(ReportField.APP_VERSION_CODE,ReportField.APP_VERSION_NAME,ReportField.ANDROID_VERSION,ReportField.BRAND,ReportField.PHONE_MODEL,ReportField.USER_COMMENT,ReportField.STACK_TRACE,ReportField.DISPLAY,ReportField.DEVICE_FEATURES)

            dialog {
                //required
                text = getString(R.string.crash_message)
                //optional, enables the dialog title
                title = getString(R.string.crash_title)
                //defaults to android.R.string.ok
                positiveButtonText = getString(R.string.ok)
                //defaults to android.R.string.cancel
                negativeButtonText = getString(R.string.cancel)
                //optional, enables the comment input
                commentPrompt = getString(R.string.crash_comment_prompt)
                //optional, enables the email input
                //emailPrompt = getString(R.string.dialog_email)
                //defaults to android.R.drawable.ic_dialog_alert
                //resIcon = R.drawable.dialog_icon
                //optional, defaults to @android:style/Theme.Dialog
                //resTheme = R.style.Theme_AlertDialog
                //allows other customization
                //reportDialogClass = MyCustomDialog::class.java
            }

            mailSender {
                mailTo = "coderGtm@gmail.com"
                reportFileName = "crash.txt"
                subject = "Crash Report for Yantra Launcher"
                body = "Yantra Launcher Crashed. Crash Report is attached."
            }
        }
    }
}
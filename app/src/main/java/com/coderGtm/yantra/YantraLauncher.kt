package com.coderGtm.yantra

import android.app.Application
import android.content.Context
import org.acra.ReportField
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

class YantraLauncher : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        initAcra {
            // core configuration
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON

            reportContent = listOf(ReportField.APP_VERSION_CODE,ReportField.APP_VERSION_NAME,ReportField.ANDROID_VERSION,ReportField.BRAND,ReportField.PHONE_MODEL,ReportField.USER_COMMENT,ReportField.STACK_TRACE,ReportField.DISPLAY,ReportField.DEVICE_FEATURES)

            dialog {
                //required
                text = "Sorry, Yantra Launcher seems to have crashed! Please send a report to the developer to help debug the crash. Pressing Ok will take yo to your email client."
                //optional, enables the dialog title
                title = "An unexpected error has occurred!"
                //defaults to android.R.string.ok
                positiveButtonText = "Ok"
                //defaults to android.R.string.cancel
                negativeButtonText = "Cancel"
                //optional, enables the comment input
                commentPrompt = "You can add an optional comment here to describe the crash:"
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
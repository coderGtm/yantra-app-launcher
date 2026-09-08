package com.coderGtm.yantra

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import org.acra.ReportField
import org.acra.config.dialog
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender

class YantraLauncher : Application() {

    lateinit var preferenceObject: SharedPreferences

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        val metaData = packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        ).metaData
        val crashReportEndpoint = metaData?.getString("CRASH_REPORT_ENDPOINT")
        val crashApiKey = metaData?.getString("CRASH_API_KEY")

        initAcra {
            // core configuration
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON

            reportContent = listOf(ReportField.REPORT_ID, ReportField.PACKAGE_NAME, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.BRAND, ReportField.PHONE_MODEL, ReportField.PRODUCT, ReportField.STACK_TRACE, ReportField.STACK_TRACE_HASH, ReportField.USER_COMMENT, ReportField.USER_EMAIL, ReportField.USER_APP_START_DATE, ReportField.USER_CRASH_DATE, ReportField.INSTALLATION_ID, ReportField.THREAD_DETAILS)

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
                emailPrompt = getString(R.string.dialog_email)
                //defaults to android.R.drawable.ic_dialog_alert
                //resIcon = R.drawable.dialog_icon
                //optional, defaults to @android:style/Theme.Dialog
                //resTheme = R.style.Theme_AlertDialog
                //allows other customization
                //reportDialogClass = MyCustomDialog::class.java
            }

            httpSender {
                uri = crashReportEndpoint
                httpMethod = HttpSender.Method.POST
                httpHeaders = mapOf("X-API-Key" to crashApiKey.orEmpty())
                connectionTimeout = 5000
                socketTimeout = 10000
            }
        }
    }
}
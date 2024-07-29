package com.coderGtm.yantra.commands.backup

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.coderGtm.yantra.R
import com.coderGtm.yantra.SHARED_PREFS_FILE_NAME
import com.coderGtm.yantra.copyFileToInternalStorage
import com.coderGtm.yantra.getFullName
import com.coderGtm.yantra.toast
import java.io.File
import java.util.Locale


fun packFile(command: Command): String {
    val packageManager = command.terminal.activity.packageManager
    val applicationInfo = packageManager.getApplicationInfo(command.terminal.activity.packageName, PackageManager.GET_META_DATA)
    val metaData = applicationInfo.metaData
    val password = metaData.getString("BACKUP_PASSWORD")?.toCharArray()

    val plainFile = File("${command.terminal.activity.filesDir.parent}/shared_prefs/",
        "$SHARED_PREFS_FILE_NAME.xml"
    )
    val date = java.text.SimpleDateFormat("HHmm_dd_MM_yyyy", Locale.getDefault()).format(java.util.Date())
    val fileName = "backup_$date.yantra"
    val encryptedFile = File(command.terminal.activity.filesDir, fileName)

    if (password != null) {
        AESSecurity.encryptFile(plainFile, password, encryptedFile)
    }

    return fileName
}

fun extractZip(activity: Activity, name: String): Boolean {
    if (!getFileExtension(name).equals("yantra", ignoreCase = true)) {
        return false
    }

    val packageManager = activity.packageManager
    val applicationInfo = packageManager.getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
    val metaData = applicationInfo.metaData
    val password = metaData?.getString("BACKUP_PASSWORD")?.toCharArray()

    val oldFile = File(activity.filesDir, name)
    val decryptedFile = File("${activity.filesDir.parent}/shared_prefs/", "$SHARED_PREFS_FILE_NAME.xml")

    if (password != null) {
        if (!AESSecurity.decryptFile(oldFile, password, decryptedFile)) {
            return false
        }
    }

    File("${activity.filesDir}/$oldFile").delete()

    return true
}


private fun getFileExtension(fileName: String?): String {
    return fileName?.substringAfterLast('.', "") ?: ""
}

fun copyFile(activity: Activity, selectedFileUri: Uri) {
    copyFileToInternalStorage(activity, selectedFileUri)
    toast(activity,activity.getString(R.string.loading_please_wait))
    if (!extractZip(activity, getFullName(selectedFileUri, activity) ?: return)) {
        Toast.makeText(activity, activity.getString(R.string.incorrect_file), Toast.LENGTH_SHORT).show()
        return
    }
    toast(activity, activity.getString(R.string.backup_restored))

    restartApp(activity)
}

private fun restartApp(activity: Activity) {
    val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
    val mainIntent = Intent.makeRestartActivityTask(intent?.component)
    activity.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}
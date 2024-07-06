package com.coderGtm.yantra.commands.backup

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import com.coderGtm.yantra.SHARED_PREFS_FILE_NAME
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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
        AESSecurity.decryptFile(oldFile, password, decryptedFile)
    }

    File("${activity.filesDir}/$oldFile").delete()

    return true
}


private fun getFileExtension(fileName: String?): String {
    return fileName?.substringAfterLast('.', "") ?: ""
}

private fun copyFileToInternalStorage(activity: Activity, uri: Uri) {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
        inputStream = activity.contentResolver.openInputStream(uri) ?: return
        val fileName = getFullName(uri, activity) ?: return
        val outputFile = File(activity.filesDir, fileName)
        outputStream = FileOutputStream(outputFile)
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush()
    } catch (ignored: IOException) {
    } finally {
        try {
            inputStream?.close()
            outputStream?.close()
        } catch (ignored: IOException) {
        }
    }
}

fun getFullName(uri: Uri, activity: Activity): String? {
    val contentResolver = activity.contentResolver
    val cursor: Cursor? = contentResolver.query(
        uri, null, null, null, null, null)

    cursor?.use {
        if (it.moveToFirst()) {
            val name = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (name >= 0) {
                val displayName: String =
                    it.getString(name)
                return displayName
            }
        }
    }

    return null
}

fun copyFile(activity: Activity, selectedFileUri: Uri) {
    copyFileToInternalStorage(activity, selectedFileUri)
    Toast.makeText(activity,"please wait", Toast.LENGTH_SHORT).show()
    if (!extractZip(activity, getFullName(selectedFileUri, activity) ?: return)) {
        Toast.makeText(activity, "incorrect_file", Toast.LENGTH_SHORT).show()
        return
    }
    Toast.makeText(activity, "success", Toast.LENGTH_SHORT).show()

    restartApp(activity)
}

private fun restartApp(activity: Activity) {
    val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
    val mainIntent = Intent.makeRestartActivityTask(intent?.component)
    activity.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}
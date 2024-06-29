package com.coderGtm.yantra.commands.backup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.coderGtm.yantra.R
import com.coderGtm.yantra.SHARED_PREFS_FILE_NAME
import ir.mahdi.mzip.zip.ZipArchive
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


fun packFile(command: Command) {
    val files = arrayOf(
        SHARED_PREFS_FILE_NAME
    )

    val zipFileName = "${command.terminal.activity.filesDir}/YantraBackup.zip"

    try {
        val parameters = ZipParameters().apply {
            compressionMethod = Zip4jConstants.COMP_STORE
            compressionLevel = Zip4jConstants.DEFLATE_LEVEL_FASTEST
            isEncryptFiles = true
            encryptionMethod = Zip4jConstants.ENC_METHOD_AES
            aesKeyStrength = Zip4jConstants.AES_STRENGTH_256
            password = command.terminal.activity.resources.getString(R.string.PASS_FOR_ZIP).toCharArray()
        }

        val zipFile = ZipFile(zipFileName)

        files.forEach { targetPath ->
            val targetFile = File("${command.terminal.activity.filesDir.parent}/shared_prefs/", "$targetPath.xml")
            if (!targetFile.exists()) return@forEach
            if (targetFile.isFile) {
                zipFile.addFile(targetFile, parameters)
            } else if (targetFile.isDirectory) {
                zipFile.addFolder(targetFile, parameters)
            }
        }
    } catch (ignored: Exception) {
    }
}

fun renameFile(oldFile: File, newFileName: String): Boolean {
    val newFile = File(oldFile.parent, newFileName)
    return oldFile.renameTo(newFile)
}

fun extractZip(activity: Activity, name: String): Boolean {
    if (!getFileExtension(name).equals("yle", ignoreCase = true)) {
        return false
    }

    val oldFile = File(activity.filesDir, name)
    val newName = name.replace(".yle", ".zip")
    renameFile(oldFile, newName)

    if (!isProtected("${activity.filesDir}/$newName")) {
        return false
    }

    if (!containsFile(activity, newName)) {
        return false
    }

    deleteFolder(File("${activity.filesDir.parent}/shared_prefs"))

    ZipArchive.unzip("${activity.filesDir}/$newName", "${activity.filesDir.parent}/shared_prefs/",activity.resources.getString(R.string.PASS_FOR_ZIP))

    File("${activity.filesDir}/$newName").delete()

    return true
}

private fun isProtected(zipFilePath: String): Boolean {
    return try {
        val zipFile = ZipFile(zipFilePath)
        zipFile.isEncrypted
    } catch (ignore: ZipException) {
        false
    }
}

private fun getFileExtension(fileName: String?): String {
    return fileName?.substringAfterLast('.', "") ?: ""
}

private fun containsFile(activity: Activity, newName: String): Boolean {
    ZipArchive.unzip("${activity.filesDir}/$newName", "${activity.filesDir.parent}/temp/", activity.resources.getString(R.string.PASS_FOR_ZIP))
    val fileExists = File("${activity.filesDir.parent}/temp/$SHARED_PREFS_FILE_NAME.xml").exists()
    deleteFolder(File("${activity.filesDir.parent}/temp"))
    return fileExists
}

private fun deleteFolder(folder: File): Boolean {
    if (folder.isDirectory) {
        folder.listFiles()?.forEach { file ->
            if (!deleteFolder(file)) {
                return false
            }
        }
    }
    return folder.delete()
}

private fun copyFileToInternalStorage(activity: Activity, uri: Uri) {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
        inputStream = activity.contentResolver.openInputStream(uri) ?: return
        val fileName = getFileNameFromUri(uri) ?: return
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

private fun getFileNameFromUri(uri: Uri): String? {
    println(uri.toString())
    val path = uri.lastPathSegment ?: return null
    val index = path.lastIndexOf('/')
    val index2 = path.lastIndexOf(':')
    return when {
        index != -1 -> path.substring(index + 1)
        index2 != -1 -> path.substring(index2 + 1)
        else -> path
    }
}

fun copyFile(activity: Activity, selectedFileUri: Uri) {
    copyFileToInternalStorage(activity, selectedFileUri)
    Toast.makeText(activity, activity.getString(R.string.pleaseWait), Toast.LENGTH_SHORT).show()
    if (!extractZip(activity, getFileNameFromUri(selectedFileUri) ?: return)) {
        Toast.makeText(activity, activity.getString(R.string.incorrect_file), Toast.LENGTH_SHORT).show()
        return
    }
    Toast.makeText(activity, activity.getString(R.string.success), Toast.LENGTH_SHORT).show()

    restartApp(activity)
}

private fun restartApp(activity: Activity) {
    val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
    val mainIntent = Intent.makeRestartActivityTask(intent?.component)
    activity.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}
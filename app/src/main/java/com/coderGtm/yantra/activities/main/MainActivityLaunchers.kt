package com.coderGtm.yantra.activities.main

import android.app.Activity
import android.graphics.Typeface
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.coderGtm.yantra.R
import com.coderGtm.yantra.YantraLauncher
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.applyLauncherBackground
import com.coderGtm.yantra.commands.backup.copyFile
import com.coderGtm.yantra.commands.theme.copyFileToInternalStorage
import com.coderGtm.yantra.setWallpaperFromUri
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs
import java.io.File
import java.io.FileInputStream

internal class MainActivityLaunchers(
    private val activity: MainActivity,
    private val uiRefs: MainActivityUiRefs,
    private val terminalProvider: () -> Terminal,
    private val appProvider: () -> YantraLauncher,
) {
    val cropImage = activity.registerForActivityResult(CropImageContract()) { result ->
        val terminal = terminalProvider()
        val app = appProvider()

        if (result.isSuccessful) {
            val uriContent = result.uriContent
            if (setWallpaperFromUri(uriContent, activity, app.preferenceObject)) {
                applyLauncherBackground(activity, uiRefs, app.preferenceObject, terminal.theme.bgColor)
                terminal.output(activity.getString(R.string.selected_wallpaper_applied), terminal.theme.successTextColor, null)
            } else {
                terminal.output(activity.getString(R.string.an_error_occurred_please_try_again), terminal.theme.errorTextColor, null)
            }
        } else {
            terminal.output(activity.getString(R.string.no_image_selected), terminal.theme.resultTextColor, Typeface.ITALIC)
        }
    }

    val pickMedia = activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val terminal = terminalProvider()

        if (uri != null) {
            val displayMetrics = activity.resources.displayMetrics
            cropImage.launch(
                CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = CropImageOptions(
                        guidelines = CropImageView.Guidelines.ON,
                        fixAspectRatio = true,
                        aspectRatioX = displayMetrics.widthPixels,
                        aspectRatioY = displayMetrics.heightPixels,
                    ),
                )
            )
        } else {
            terminal.output(activity.getString(R.string.no_image_selected), terminal.theme.resultTextColor, Typeface.ITALIC)
        }
    }

    val yantraSettingsLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data?.getBooleanExtra("settingsChanged", false) == true) {
                activity.recreate()
            }
        }
    }

    val sendFileLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                val fileName = MainActivityBehavior.buildBackupFileName()
                copyTempFileToUri(fileName, uri)
            }
        }
    }

    val selectFileLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                copyFile(activity, uri)
            }
        }
    }

    val openResultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == 6) {
            val errorMessage = result.data?.getStringExtra("ERR").orEmpty()
            toast(activity.baseContext, errorMessage)
        }
    }

    val exportThemeLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val fileName = activity.pendingThemeFileName
            if (fileName.isNullOrBlank()) {
                toast(activity.baseContext, activity.getString(R.string.file_not_found))
                return@registerForActivityResult
            }

            result.data?.data?.also { uri ->
                copyTempFileToUri(fileName, uri)
            }
        }
    }

    val getThemeFile = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                copyFileToInternalStorage(activity, uri)
            }
        }
    }

    val externalEditor = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val tempFile = File(activity.filesDir, "script.lua")
        val scriptName = activity.pendingScriptName

        if (scriptName.isNullOrBlank() || !tempFile.exists()) {
            toast(activity, activity.getString(R.string.failed_to_update_script))
            return@registerForActivityResult
        }

        val editedText = tempFile.readText()
        appProvider().preferenceObject.edit { putString("script_$scriptName", editedText) }
        toast(activity, activity.getString(R.string.script_saved_successfully, scriptName))
    }

    private fun copyTempFileToUri(fileName: String, targetUri: android.net.Uri) {
        val sourceFile = File(activity.filesDir, fileName)
        if (!sourceFile.exists()) {
            toast(activity.baseContext, activity.getString(R.string.file_not_found))
            return
        }

        val copied = runCatching {
            activity.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } != null
        }.getOrDefault(false)

        if (copied) {
            sourceFile.delete()
        } else {
            toast(activity.baseContext, activity.getString(R.string.an_error_occurred_please_try_again))
        }
    }
}

package com.coderGtm.yantra.activities

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.KeyEvent
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.edit
import androidx.core.graphics.drawable.DrawableCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.coderGtm.yantra.R
import com.coderGtm.yantra.SHARED_PREFS_FILE_NAME
import com.coderGtm.yantra.YantraLauncher
import com.coderGtm.yantra.commands.backup.copyFile
import com.coderGtm.yantra.commands.termux.handleTermuxResult
import com.coderGtm.yantra.commands.theme.copyFileToInternalStorage
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.getInit
import com.coderGtm.yantra.informOfProVersionIfOldUser
import com.coderGtm.yantra.isPro
import com.coderGtm.yantra.listeners.TermuxCommandResultReceiver
import com.coderGtm.yantra.requestCmdInputFocusAndShowKeyboard
import com.coderGtm.yantra.requestUpdateIfAvailable
import com.coderGtm.yantra.runInitTasks
import com.coderGtm.yantra.services.TermuxCommandService
import com.coderGtm.yantra.setProStatus
import com.coderGtm.yantra.setWallpaperFromUri
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast
import com.coderGtm.yantra.views.TerminalGestureListenerCallback
import java.io.File
import java.io.FileInputStream
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener, TerminalGestureListenerCallback {

    private lateinit var primaryTerminal: Terminal
    private lateinit var app: YantraLauncher
    private lateinit var binding: ActivityMainBinding
    private lateinit var commandResultReceiver: TermuxCommandResultReceiver

    var tts: TextToSpeech? = null
    var ttsTxt = ""
    var pendingScriptName: String? = null
    var pendingThemeFileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as YantraLauncher
        app.preferenceObject = applicationContext.getSharedPreferences(SHARED_PREFS_FILE_NAME,0)
        setProStatus(this, app.preferenceObject)

        primaryTerminal = Terminal(
            activity = this@MainActivity,
            binding = binding,
            preferenceObject = app.preferenceObject
        )
        primaryTerminal.initialize()
        informOfProVersionIfOldUser(this@MainActivity)

        commandResultReceiver = TermuxCommandResultReceiver { result ->
            handleTermuxResult(result, primaryTerminal)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
            commandResultReceiver,
            IntentFilter(TermuxCommandService.ACTION_COMMAND_RESULT)
        )

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (primaryTerminal.initialized && isPro(this@MainActivity)) {
            Thread {
                val initList = getInit(app.preferenceObject)
                runInitTasks(initList, app.preferenceObject, primaryTerminal)
            }.start()
        }
    }
    override fun onRestart() {
        super.onRestart()
        val unwrappedCursorDrawable = AppCompatResources.getDrawable(this,
            R.drawable.cursor_drawable
        )
        val wrappedCursorDrawable = DrawableCompat.wrap(unwrappedCursorDrawable!!)
        DrawableCompat.setTint(wrappedCursorDrawable, primaryTerminal.theme.inputLineTextColor)
        Thread {
            requestUpdateIfAvailable(app.preferenceObject, this@MainActivity)
        }.start()
    }

    override fun onResume() {
        super.onResume()
        Timer().schedule(250) {
            binding.scrollView.scrollToBottom()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(commandResultReceiver)
    }
    override fun onSingleTap() {
        val oneTapKeyboardActivation = app.preferenceObject.getBoolean("oneTapKeyboardActivation",true)
        if (oneTapKeyboardActivation) {
            requestCmdInputFocusAndShowKeyboard(this@MainActivity, binding)
        }
    }
    override fun onDoubleTap() {
        val cmdToExecute = app.preferenceObject.getString("doubleTapCommand", "lock")
        if (cmdToExecute != "") {
            //execute command
            primaryTerminal.handleCommand(cmdToExecute!!)
        }
    }

    override fun onSwipeRight() {
        if (isPro(this@MainActivity)) {
            val cmdToExecute = app.preferenceObject.getString("swipeRightCommand", getString(R.string.default_right_swipe_text))
            if (cmdToExecute != "") {
                //execute command
                primaryTerminal.handleCommand(cmdToExecute!!)
            }
        }
    }

    override fun onSwipeLeft() {
        if (isPro(this@MainActivity)) {
            val cmdToExecute = app.preferenceObject.getString("swipeLeftCommand", getString(R.string.default_left_swipe_text))
            if (cmdToExecute != "") {
                //execute command
                primaryTerminal.handleCommand(cmdToExecute!!)
            }
        }
    }

    override fun onInit(status: Int) {
        //TTS Initialization function
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                primaryTerminal.output(getString(R.string.error_tts_language_not_supported), primaryTerminal.theme.errorTextColor, null)
            } else {
                tts!!.setSpeechRate(.7f)
                tts!!.speak(ttsTxt, TextToSpeech.QUEUE_FLUSH, null,"")
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                primaryTerminal.output(getString(R.string.tts_synthesized_playing_now), primaryTerminal.theme.successTextColor, null)
            }
            override fun onDone(utteranceId: String) {
                primaryTerminal.output(getString(R.string.shutting_down_tts_engine), primaryTerminal.theme.resultTextColor, null)

                if (tts != null) {
                    tts!!.stop()
                    tts!!.shutdown()
                }
                primaryTerminal.output(getString(R.string.tts_engine_shutdown), primaryTerminal.theme.resultTextColor, null)
            }
            override fun onError(utteranceId: String) {
                primaryTerminal.output(getString(R.string.tts_error), primaryTerminal.theme.errorTextColor, null)
                primaryTerminal.output(getString(R.string.shutting_down_tts_engine), primaryTerminal.theme.resultTextColor, null)

                if (tts != null) {
                    tts!!.stop()
                    tts!!.shutdown()
                }
                primaryTerminal.output(getString(R.string.tts_engine_shutdown), primaryTerminal.theme.resultTextColor, null)

            }
        })
    }
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP && event.action == KeyEvent.ACTION_UP) {
            primaryTerminal.cmdUp()
        }
        else if (event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.action == KeyEvent.ACTION_UP) {
            primaryTerminal.cmdDown()
        }
        else if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
            val inputReceived = binding.cmdInput.text.toString().trim()
            primaryTerminal.handleInput(inputReceived)
        }
        return super.dispatchKeyEvent(event)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            primaryTerminal.output(getString(R.string.permission_denied), primaryTerminal.theme.errorTextColor, null)
        } else {
            primaryTerminal.output(getString(R.string.permission_granted), primaryTerminal.theme.successTextColor, null)
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            val uriContent = result.uriContent
            setWallpaperFromUri(uriContent, this, primaryTerminal.theme.bgColor, app.preferenceObject)
            primaryTerminal.output(getString(R.string.selected_wallpaper_applied), primaryTerminal.theme.successTextColor, null)
        } else {
            primaryTerminal.output(getString(R.string.no_image_selected), primaryTerminal.theme.resultTextColor, Typeface.ITALIC)
        }
    }

    // Registers a photo picker activity launcher in single-select mode.
    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            cropImage.launch(
                CropImageContractOptions(uri = uri, CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON
                ))
            )
        } else {
            primaryTerminal.output(getString(R.string.no_image_selected), primaryTerminal.theme.resultTextColor, Typeface.ITALIC)
        }
    }
    var yantraSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val settingsChanged = data.getBooleanExtra("settingsChanged", false)
                if (settingsChanged) {
                    recreate()
                }
            }
        }
    }

    val sendFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val date = java.text.SimpleDateFormat("HHmm_dd_MM_yyyy", Locale.getDefault()).format(java.util.Date())
            val fileName = "backup_$date.yantra"

            result.data?.data?.also { uri ->
                val file = File(filesDir, fileName)
                if (file.exists()) {
                    val inputStream = FileInputStream(file)

                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    inputStream.close()

                    if (file.exists()) {
                        file.delete()
                    }
                } else {
                    toast(baseContext, getString(R.string.file_not_found))
                }
            }
        }
    }

    val selectFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {
                copyFile(this, result.data!!.data!!)
            }
        }
    }

    val openResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == 6) {
            val errorMessage: String = result.data?.getStringExtra("ERR").toString()
            toast(baseContext, errorMessage)
        }
    }

    /**
     * Launcher for exporting themes.
     * It will export the theme to a file and then prompt the user to select a location to save it.
     */
    val exportThemeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val fileName: String = pendingThemeFileName.toString()

            result.data?.data?.also { uri ->
                val file = File(filesDir, fileName)
                if (file.exists()) {
                    val inputStream = FileInputStream(file)

                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    inputStream.close()

                    if (file.exists()) {
                        file.delete()
                    }
                } else {
                    toast( baseContext, getString(R.string.file_not_found))
                }
            }
        }
    }

    val getThemeFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                copyFileToInternalStorage(this, uri)
            }
        }
    }

    fun getPreferenceObject(): SharedPreferences {
        return app.preferenceObject
    }

    val externalEditor = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val tempFile = File(filesDir, "script.lua")
        val editedText = tempFile.readText()
        val scriptName = pendingScriptName
        if (scriptName == null) {
            toast(this, getString(R.string.failed_to_update_script))
            return@registerForActivityResult
        }

        app.preferenceObject.edit { putString("script_$scriptName", editedText) }
        toast(this, getString(R.string.script_saved_successfully, scriptName))
    }
}

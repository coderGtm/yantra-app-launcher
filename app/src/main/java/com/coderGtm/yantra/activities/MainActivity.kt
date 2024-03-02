package com.coderGtm.yantra.activities

import android.app.Activity
import android.content.Intent
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
import androidx.core.graphics.drawable.DrawableCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.coderGtm.yantra.R
import com.coderGtm.yantra.SHARED_PREFS_FILE_NAME
import com.coderGtm.yantra.YantraLauncher
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.getInit
import com.coderGtm.yantra.misc.purchasePrank
import com.coderGtm.yantra.requestCmdInputFocusAndShowKeyboard
import com.coderGtm.yantra.requestUpdateIfAvailable
import com.coderGtm.yantra.runInitTasks
import com.coderGtm.yantra.setWallpaperFromUri
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.views.TerminalGestureListenerCallback
import java.util.Locale


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener, TerminalGestureListenerCallback {

    private lateinit var primaryTerminal: Terminal
    private lateinit var app: YantraLauncher
    private lateinit var binding: ActivityMainBinding

    var tts: TextToSpeech? = null
    var ttsTxt = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as YantraLauncher
        app.preferenceObject = applicationContext.getSharedPreferences(SHARED_PREFS_FILE_NAME,0)

        primaryTerminal = Terminal(
            activity = this@MainActivity,
            binding = binding,
            preferenceObject = app.preferenceObject
        )
        primaryTerminal.initialize()

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (primaryTerminal.initialized) {
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
        DrawableCompat.setTint(wrappedCursorDrawable, primaryTerminal.theme.buttonColor)
        Thread {
            requestUpdateIfAvailable(app.preferenceObject, this@MainActivity)
        }.start()
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
        val cmdToExecute = app.preferenceObject.getString("swipeRightCommand", getString(R.string.default_right_swipe_text))
        if (cmdToExecute != "") {
            //execute command
            primaryTerminal.handleCommand(cmdToExecute!!)
        }
    }

    override fun onSwipeLeft() {
        val cmdToExecute = app.preferenceObject.getString("swipeLeftCommand", getString(R.string.default_left_swipe_text))
        if (cmdToExecute != "") {
            //execute command
            primaryTerminal.handleCommand(cmdToExecute!!)
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

    fun initializeProductPurchase(skuId: String) {
        primaryTerminal.output("Initializing purchase...Please wait.",primaryTerminal.theme.resultTextColor, null)
        purchasePrank(primaryTerminal, skuId)
        return
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
}

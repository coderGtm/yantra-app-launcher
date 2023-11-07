package com.coderGtm.yantra.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.android.billingclient.api.BillingClient
import com.coderGtm.yantra.ActivityRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.SHARED_PREFS_FILE_NAME
import com.coderGtm.yantra.YantraLauncher
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.getAppsList
import com.coderGtm.yantra.requestCmdInputFocusAndShowKeyboard
import com.coderGtm.yantra.requestUpdateIfAvailable
import com.coderGtm.yantra.setWallpaperFromUri
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.views.TerminalGestureListenerCallback
import java.util.Locale


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener, TerminalGestureListenerCallback {

    private var tts: TextToSpeech? = null

    private lateinit var primaryTerminal: Terminal
    private lateinit var app: YantraLauncher
    private lateinit var binding: ActivityMainBinding
    private lateinit var billingClient: BillingClient

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
    }

    override fun onStart() {
        super.onStart()
        Thread {
            primaryTerminal.appList = getAppsList(primaryTerminal)
            val initList = getInit()
            runInitTasks(initList)
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                primaryTerminal.output("Error: TTS language not supported!", primaryTerminal.theme.errorTextColor, null)
            } else {
                tts!!.setSpeechRate(.7f)
                tts!!.speak(ttsTxt, TextToSpeech.QUEUE_FLUSH, null,"")
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                primaryTerminal.output("TTS synthesized! Playing now...", primaryTerminal.theme.successTextColor, null)
            }
            override fun onDone(utteranceId: String) {
                primaryTerminal.output("Shutting down TTS engine...", primaryTerminal.theme.resultTextColor, null)

                if (tts != null) {
                    tts!!.stop()
                    tts!!.shutdown()
                }
                primaryTerminal.output("TTS engine shutdown.", primaryTerminal.theme.resultTextColor, null)
            }
            override fun onError(utteranceId: String) {
                primaryTerminal.output("TTS error!!", primaryTerminal.theme.errorTextColor, null)
                primaryTerminal.output("Shutting down TTS engine...", primaryTerminal.theme.resultTextColor, null)

                if (tts != null) {
                    tts!!.stop()
                    tts!!.shutdown()
                }
                primaryTerminal.output("TTS engine shutdown.", primaryTerminal.theme.resultTextColor, null)

            }
        })
    }

    private fun getInit(): String {
        return try {
            app.preferenceObject.getString("initList", "") ?: ""
        } catch (e: ClassCastException) {
            // prev Set implementation present
            app.preferenceObject.edit().remove("initList").apply()
            ""
        }
    }
    private fun runInitTasks(initList: String?) {
        if (initList?.trim() != "") {
            val initCmdLog = app.preferenceObject.getBoolean("initCmdLog", false)
            runOnUiThread {
                initList?.lines()?.forEach {
                    primaryTerminal.handleCommand(it.trim(), logCmd = initCmdLog)
                }
            }
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
    override fun onResume() {
        super.onResume()
        if (primaryTerminal.uninstallCmdActive) {
            primaryTerminal.uninstallCmdActive = false
            primaryTerminal.appList = getAppsList(primaryTerminal)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        try {
            billingClient.endConnection()
        }
        catch(_: java.lang.Exception) {}
    }
    override fun onBackPressed() {}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ActivityRequestCodes.IMAGE_PICK.code) {
            if (resultCode == RESULT_OK) {
                val uri = data?.data
                setWallpaperFromUri(uri, this, primaryTerminal.theme.bgColor, app.preferenceObject)
                primaryTerminal.output("Selected Wallpaper applied!", primaryTerminal.theme.successTextColor, null)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            primaryTerminal.output("Permission denied!", primaryTerminal.theme.errorTextColor, null)
        } else {
            primaryTerminal.output("Permission Granted", primaryTerminal.theme.successTextColor, null)
        }
    }
}

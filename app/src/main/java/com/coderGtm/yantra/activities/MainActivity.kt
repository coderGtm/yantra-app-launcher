package com.coderGtm.yantra.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.coderGtm.yantra.activities.main.MainActivityCoordinator
import com.coderGtm.yantra.ui.screens.main.MainActivityScreen
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs
import com.coderGtm.yantra.ui.screens.main.createMainActivityUiRefs
import com.coderGtm.yantra.views.TerminalGestureListenerCallback

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener, TerminalGestureListenerCallback {

    lateinit var uiRefs: MainActivityUiRefs
        private set

    private lateinit var coordinator: MainActivityCoordinator

    var tts: TextToSpeech? = null
    var ttsTxt = ""
    var pendingScriptName: String? = null
    var pendingThemeFileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiRefs = createMainActivityUiRefs(this)
        coordinator = MainActivityCoordinator(this, uiRefs)
        coordinator.onCreate()

        setContent {
            MainActivityScreen(uiRefs = uiRefs)
        }
    }

    override fun onStart() {
        super.onStart()
        coordinator.onStart()
    }

    override fun onRestart() {
        super.onRestart()
        coordinator.onRestart()
    }

    override fun onResume() {
        super.onResume()
        coordinator.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        coordinator.onDestroy()
    }

    override fun onSingleTap() = coordinator.onSingleTap()

    override fun onDoubleTap() = coordinator.onDoubleTap()

    override fun onSwipeRight() = coordinator.onSwipeRight()

    override fun onSwipeLeft() = coordinator.onSwipeLeft()

    override fun onInit(status: Int) = coordinator.onInit(status)

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        coordinator.handleKeyEvent(event)
        return super.dispatchKeyEvent(event)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        coordinator.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun getPreferenceObject(): SharedPreferences = coordinator.getPreferenceObject()

    val pickMedia get() = coordinator.utilActivityLaunchers.pickMedia
    val yantraSettingsLauncher get() = coordinator.utilActivityLaunchers.yantraSettings
    val sendFileLauncher get() = coordinator.utilActivityLaunchers.sendFile
    val selectFileLauncher get() = coordinator.utilActivityLaunchers.selectFile
    val openResultLauncher get() = coordinator.utilActivityLaunchers.openCmdResult
    val exportThemeLauncher get() = coordinator.utilActivityLaunchers.exportTheme
    val getThemeFile get() = coordinator.utilActivityLaunchers.getThemeFile
    val externalEditor get() = coordinator.utilActivityLaunchers.externalEditor
}
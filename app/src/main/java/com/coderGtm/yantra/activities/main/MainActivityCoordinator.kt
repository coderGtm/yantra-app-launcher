package com.coderGtm.yantra.activities.main

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.view.KeyEvent
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.WindowCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.coderGtm.yantra.R
import com.coderGtm.yantra.SHARED_PREFS_FILE_NAME
import com.coderGtm.yantra.YantraLauncher
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.commands.termux.handleTermuxResult
import com.coderGtm.yantra.getInit
import com.coderGtm.yantra.informOfProVersionIfOldUser
import com.coderGtm.yantra.isPro
import com.coderGtm.yantra.listeners.TermuxCommandResultReceiver
import com.coderGtm.yantra.requestCmdInputFocusAndShowKeyboard
import com.coderGtm.yantra.requestUpdateIfAvailable
import com.coderGtm.yantra.runInitTasks
import com.coderGtm.yantra.services.TermuxCommandService
import com.coderGtm.yantra.setProStatus
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs
import java.util.Timer
import kotlin.concurrent.schedule

internal class MainActivityCoordinator(
    private val activity: MainActivity,
    private val uiRefs: MainActivityUiRefs,
) {
    private val app: YantraLauncher
        get() = activity.application as YantraLauncher

    lateinit var terminal: Terminal
        private set

    val launchers = MainActivityLaunchers(
        activity = activity,
        uiRefs = uiRefs,
        terminalProvider = { terminal },
        appProvider = { app },
    )

    private val commandResultReceiver = TermuxCommandResultReceiver { result ->
        handleTermuxResult(result, terminal)
    }

    private val speechCoordinator = MainActivitySpeechCoordinator(activity) { terminal }

    fun onCreate() {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)

        app.preferenceObject = activity.applicationContext.getSharedPreferences(SHARED_PREFS_FILE_NAME, 0)
        setProStatus(activity, app.preferenceObject)

        terminal = Terminal(
            activity = activity,
            binding = uiRefs,
            preferenceObject = app.preferenceObject,
        )
        terminal.initialize()
        informOfProVersionIfOldUser(activity)

        LocalBroadcastManager.getInstance(activity).registerReceiver(
            commandResultReceiver,
            IntentFilter(TermuxCommandService.ACTION_COMMAND_RESULT),
        )

        activity.onBackPressedDispatcher.addCallback(activity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = Unit
        })
    }

    fun onStart() {
        if (terminal.initialized && isPro(activity)) {
            Thread {
                val initList = getInit(app.preferenceObject)
                runInitTasks(initList, app.preferenceObject, terminal)
            }.start()
        }
    }

    fun onRestart() {
        val unwrappedCursorDrawable = AppCompatResources.getDrawable(activity, R.drawable.cursor_drawable)
        val wrappedCursorDrawable = DrawableCompat.wrap(unwrappedCursorDrawable!!)
        DrawableCompat.setTint(wrappedCursorDrawable, terminal.theme.inputLineTextColor)

        Thread {
            requestUpdateIfAvailable(app.preferenceObject, activity)
        }.start()
    }

    fun onResume() {
        Timer().schedule(250) {
            uiRefs.scrollView.scrollToBottom()
        }
    }

    fun onDestroy() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(commandResultReceiver)
    }

    fun onSingleTap() {
        val shouldOpenKeyboard = app.preferenceObject.getBoolean("oneTapKeyboardActivation", true)
        if (shouldOpenKeyboard) {
            requestCmdInputFocusAndShowKeyboard(uiRefs)
        }
    }

    fun onDoubleTap() {
        val cmdToExecute = app.preferenceObject.getString("doubleTapCommand", "lock")
        if (MainActivityBehavior.shouldHandleCommand(cmdToExecute)) {
            terminal.handleCommand(cmdToExecute!!)
        }
    }

    fun onSwipeRight() {
        val cmdToExecute = app.preferenceObject.getString("swipeRightCommand", activity.getString(R.string.default_right_swipe_text))
        if (MainActivityBehavior.shouldHandleSwipeCommand(isPro(activity), cmdToExecute)) {
            terminal.handleCommand(cmdToExecute!!)
        }
    }

    fun onSwipeLeft() {
        val cmdToExecute = app.preferenceObject.getString("swipeLeftCommand", activity.getString(R.string.default_left_swipe_text))
        if (MainActivityBehavior.shouldHandleSwipeCommand(isPro(activity), cmdToExecute)) {
            terminal.handleCommand(cmdToExecute!!)
        }
    }

    fun onInit(status: Int) {
        speechCoordinator.onInit(status)
    }

    fun handleKeyEvent(event: KeyEvent) {
        if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP && event.action == KeyEvent.ACTION_UP) {
            terminal.cmdUp()
        } else if (event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.action == KeyEvent.ACTION_UP) {
            terminal.cmdDown()
        } else if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
            val inputReceived = uiRefs.cmdInput.text.toString().trim()
            terminal.handleInput(inputReceived)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            terminal.output(activity.getString(R.string.permission_denied), terminal.theme.errorTextColor, null)
        } else {
            terminal.output(activity.getString(R.string.permission_granted), terminal.theme.successTextColor, null)
        }
    }

    fun getPreferenceObject(): android.content.SharedPreferences = app.preferenceObject
}

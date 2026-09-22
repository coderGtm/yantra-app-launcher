package com.coderGtm.yantra.activities.main

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.coderGtm.yantra.misc.requiresLegacyImeResize
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
import com.coderGtm.yantra.core.runtime.LauncherRuntime
import com.coderGtm.yantra.core.shell.ShellManager
import com.coderGtm.yantra.cli.shell.TerminalShell
import com.coderGtm.yantra.gui.shell.DesktopShell
import com.coderGtm.yantra.gui.shell.WebGuiShell
import com.coderGtm.yantra.gui.package_manager.GuiRegistry
import com.coderGtm.yantra.gui.package_manager.GuiPackageImporter
import com.coderGtm.yantra.gui.package_manager.GuiPackage
import com.coderGtm.yantra.core.shell.InterfaceStateStore
import com.coderGtm.yantra.gui.desktop.repository.HomeRepository
import com.coderGtm.yantra.ui.screens.gui.NativeGuiState
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

    private val shellManager = ShellManager()

    val guiRegistry = GuiRegistry(activity)
    private val guiPackageImporter = GuiPackageImporter(activity, guiRegistry)
    private val interfaceStateStore = InterfaceStateStore(activity)
    private val homeRepository = HomeRepository(activity)
    
    val guiContainer = FrameLayout(activity).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    /**
     * Utility Activity Launchers
     *
     * Holds reference to utility activity launchers
     * like crop image, send file, pick media, settings, etc.
     */
    val utilActivityLaunchers = MainActivityLaunchers(
        activity = activity,
        uiRefs = uiRefs,
        terminalProvider = { terminal },
        appProvider = { app },
    )

    private val termuxCommandResultReceiver = TermuxCommandResultReceiver { result ->
        handleTermuxResult(result, terminal)
    }

    private val speechCoordinator = MainActivitySpeechCoordinator(activity) { terminal }

    fun onCreate() {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        installLegacyImeResize()

        app.preferenceObject = activity.applicationContext.getSharedPreferences(SHARED_PREFS_FILE_NAME, 0)
        setProStatus(activity, app.preferenceObject)

        terminal = Terminal(
            activity = activity,
            binding = uiRefs,
            preferenceObject = app.preferenceObject,
        )
        terminal.initialize()
        
        uiRefs.guiContainer = guiContainer
        uiRefs.nativeGuiState = NativeGuiState(homeRepository.load())

        // Initialize Shell Platform
        LauncherRuntime.shellManager = shellManager
        
        val terminalShell = TerminalShell(terminal)
        LauncherRuntime.registerShell("cli", terminalShell)

        val desktopShell = DesktopShell(uiRefs)
        LauncherRuntime.registerShell("gui", desktopShell)
        
        // Register already imported shells
        guiRegistry.all().forEach { pkg ->
            registerImportedGuiShell(pkg)
        }

        shellManager.onShellChanged = { name, _ ->
            uiRefs.activeShellName = name
            interfaceStateStore.save(name)
        }

        val savedShell = interfaceStateStore.load()
        if (savedShell != null && LauncherRuntime.hasShell(savedShell)) {
            LauncherRuntime.switchShell(savedShell)
        } else {
            LauncherRuntime.switchShell("cli")
        }

        informOfProVersionIfOldUser(activity)

        LocalBroadcastManager.getInstance(activity).registerReceiver(
            termuxCommandResultReceiver,
            IntentFilter(TermuxCommandService.ACTION_COMMAND_RESULT),
        )

        activity.onBackPressedDispatcher.addCallback(activity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentShell = LauncherRuntime.currentShell
                if (currentShell?.onBackPressed() == true) return
                
                // If not handled by shell, let activity handle it
                // Note: We might need to disable this callback temporarily to call super
                isEnabled = false
                activity.onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        })
    }

    /**
     * On API < 30 the `ime()` window inset type does not exist, so Compose's
     * [androidx.compose.foundation.layout.imePadding] cannot lift content above the keyboard,
     * and under `adjustNothing` the window's visible frame does not change either (so the
     * keyboard cannot be detected by measuring it). Override the manifest's `adjustNothing`
     * with `adjustResize` so the window physically resizes and bottom-anchored content (the
     * suggestion row) rises above the keyboard. API 30+ keeps the insets-based path.
     */
    private fun installLegacyImeResize() {
        if (!requiresLegacyImeResize(Build.VERSION.SDK_INT)) {
            return
        }
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    fun onStart() {
        if (MainActivityBehavior.shouldRunInit(terminal.initialized, isPro(activity), terminal.initTasksQueued)) {
            terminal.initTasksQueued = true
            Thread {
                val initList = getInit(app.preferenceObject)
                runInitTasks(initList, app.preferenceObject, terminal)
            }.start()
        }
    }

    fun onStop() {
        terminal.initTasksQueued = false
    }

    fun onRestart() {
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
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(termuxCommandResultReceiver)
        terminal.cancelSuggestionScope()
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

    fun importGuiPackage(callback: (GuiPackageImporter.Result) -> Unit) {
        utilActivityLaunchers.importGui { uri ->
            if (uri == null) {
                callback(GuiPackageImporter.Result(false, errors = listOf("No folder selected.")))
                return@importGui
            }
            val result = guiPackageImporter.import(uri)
            callback(result)
        }
    }

    fun registerImportedGuiShell(pkg: GuiPackage) {
        val webShell = WebGuiShell(
            guiContainer = guiContainer,
            guiPackage = pkg,
            terminalProvider = { terminal }
        )
        LauncherRuntime.registerShell(pkg.id, webShell)
    }

    fun unregisterImportedGuiShell(name: String) {
        LauncherRuntime.unregisterShell(name)
    }
}

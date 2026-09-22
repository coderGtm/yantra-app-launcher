package com.coderGtm.yantra.gui.web

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.coderGtm.yantra.core.runtime.LauncherRuntime
import com.coderGtm.yantra.gui.package_manager.GuiPackage
import com.coderGtm.yantra.terminal.Terminal
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Android bridge exposed to imported Yantra GUI JavaScript as window.YantraAndroid.
 *
 * Read-only/package queries may run on the WebView bridge thread. UI-affecting work is
 * posted back to the WebView/UI thread.
 */
class YantraWebBridge(
    private val context: Context,
    private val guiPackage: GuiPackage,
    private val terminalProvider: () -> Terminal,
    private val webViewProvider: () -> WebView?
) {

    private val appResolver = YantraAppResolver(context)
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun appsList(): String {
        val array = JSONArray()
        appResolver.list().forEach { app ->
            array.put(JSONObject().apply {
                put("package", app.packageName)
                put("name", app.label)
            })
        }
        return array.toString()
    }

    @JavascriptInterface
    fun appExists(packageName: String): Boolean =
        appResolver.exists(packageName)

    @JavascriptInterface
    fun launchApp(packageName: String): Boolean =
        runUi { appResolver.launch(packageName) }

    @JavascriptInterface
    fun appInfo(packageName: String): Boolean =
        runUi { appResolver.info(packageName) }

    @JavascriptInterface
    fun uninstallApp(packageName: String): Boolean =
        runUi { appResolver.uninstall(packageName) }

    @JavascriptInterface
    fun shellCli() {
        postUi {
            LauncherRuntime.switchShell("cli")
        }
    }

    @JavascriptInterface
    fun shellGui() {
        postUi {
            LauncherRuntime.switchShell("gui")
        }
    }

    @JavascriptInterface
    fun drawerOpen() {
        dispatch("yantra-drawer-open")
    }

    @JavascriptInterface
    fun drawerClose() {
        dispatch("yantra-drawer-close")
    }

    @JavascriptInterface
    fun deviceLock() {
        postUi {
            terminalProvider().handleCommand("lock")
        }
    }

    @JavascriptInterface
    fun wallpaperGet(): String = guiPackage.wallpaper

    private fun dispatch(eventName: String) {
        postUi {
            webViewProvider()?.evaluateJavascript(
                "document.dispatchEvent(new CustomEvent(${JSONObject.quote(eventName)}));",
                null
            )
        }
    }

    private fun postUi(action: () -> Unit) {
        val webView = webViewProvider()
        if (webView != null) {
            webView.post(action)
        }
    }

    private fun runUi(action: () -> Boolean): Boolean {
        val webView = webViewProvider() ?: return false

        if (Looper.myLooper() == Looper.getMainLooper()) {
            return runCatching { action() }.getOrDefault(false)
        }

        val latch = CountDownLatch(1)
        var result = false

        mainHandler.post {
            result = runCatching { action() }.getOrDefault(false)
            latch.countDown()
        }

        // Keep the JavaScript API synchronous while ensuring Android UI work runs on
        // the main thread. A short timeout prevents a stalled UI from blocking the bridge.
        latch.await(2, TimeUnit.SECONDS)
        return result
    }
}

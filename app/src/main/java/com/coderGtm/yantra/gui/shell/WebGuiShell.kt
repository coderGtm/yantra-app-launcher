package com.coderGtm.yantra.gui.shell

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.coderGtm.yantra.core.shell.Shell
import com.coderGtm.yantra.gui.package_manager.GuiPackage
import com.coderGtm.yantra.gui.web.YantraDomProcessor
import com.coderGtm.yantra.gui.web.YantraWebBridge
import com.coderGtm.yantra.terminal.Terminal
import java.io.File

/** Runtime for a user-imported HTML/CSS/JS Yantra GUI package. */
class WebGuiShell(
    private val guiContainer: ViewGroup,
    private val guiPackage: GuiPackage,
    private val terminalProvider: () -> Terminal
) : Shell {

    private var webView: WebView? = null
    private var initialized = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun initialize() {
        if (initialized) return

        lateinit var view: WebView
        view = WebView(guiContainer.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    val url = request.url.toString()
                    return if (url.startsWith("file:") || url.startsWith("about:")) {
                        false
                    } else {
                        runCatching {
                            guiContainer.context.startActivity(
                                Intent(Intent.ACTION_VIEW, request.url)
                            )
                        }
                        true
                    }
                }
            }
        }

        val bridge = YantraWebBridge(
            context = guiContainer.context,
            guiPackage = guiPackage,
            terminalProvider = terminalProvider,
            webViewProvider = { webView }
        )

        view.addJavascriptInterface(bridge, "YantraAndroid")
        guiContainer.addView(view)
        webView = view
        initialized = true
        loadPackage(view)
    }

    override fun resume() {
        initialize()
        guiContainer.visibility = View.VISIBLE
        webView?.visibility = View.VISIBLE
        webView?.onResume()
    }

    override fun pause() {
        webView?.visibility = View.GONE
        webView?.onPause()
        guiContainer.visibility = View.GONE
    }

    override fun destroy() {
        webView?.apply {
            loadUrl("about:blank")
            removeJavascriptInterface("YantraAndroid")
            stopLoading()
            onPause()
            destroy()
        }
        webView = null
        initialized = false
    }

    override fun onBackPressed(): Boolean {
        val view = webView ?: return false
        if (view.canGoBack()) {
            view.goBack()
            return true
        }
        return true
    }

    fun reload() {
        webView?.let { loadPackage(it) }
    }

    fun getPackage(): GuiPackage = guiPackage

    private fun loadPackage(view: WebView) {
        val entry = File(guiPackage.rootDir, guiPackage.entry)
        if (!entry.isFile) return

        val html = entry.readText(Charsets.UTF_8)
        val bootstrappedHtml = YantraDomProcessor.inject(html)

        view.loadDataWithBaseURL(
            guiPackage.rootDir.toURI().toString(),
            bootstrappedHtml,
            "text/html",
            "UTF-8",
            null
        )
    }
}

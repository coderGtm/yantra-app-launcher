package com.coderGtm.yantra.activities

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.coderGtm.yantra.R
import com.coderGtm.yantra.SHARED_PREFS_FILE_NAME
import com.google.android.material.button.MaterialButton


class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: IncognitoWebView
    private lateinit var titleBar: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        var urlPassed = intent.getStringExtra("url")
        if (urlPassed == null) {
            urlPassed = "https://www.google.com"
        }

        if (intent.data != null) {
            urlPassed = intent.data.toString()
        }

        val preference = applicationContext.getSharedPreferences(SHARED_PREFS_FILE_NAME,0)

        webView = findViewById(R.id.webView)
        val closeBtn: MaterialButton = findViewById(R.id.closeBtn)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        titleBar = findViewById(R.id.title)

        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        // no ask-of-cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(false)
        cookieManager.removeAllCookies(null)
        cookieManager.flush()

        webView.clearHistory()
        webView.clearFormData()
        webView.clearCache(true)
        progressBar.visibility = ProgressBar.INVISIBLE
        titleBar.movementMethod = ScrollingMovementMethod()
        titleBar.setHorizontallyScrolling(true)

        webView.webViewClient = object : WebViewClient() {
            //simple ad block
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val url = request.url.toString()

                if (url.contains("ads") || url.contains("banner")) {
                    if (preference.getBoolean("disableAds", false)) {
                        return WebResourceResponse("text/plain", "utf-8", null)
                    }
                }

                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                progressBar.visibility = ProgressBar.VISIBLE
                progressBar.progress = 0
                view.loadUrl(request.url.toString())
                titleBar.text = request.url.host.toString()
                return true
            }

            // also simple ad block
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (!preference.getBoolean("disableAds", false)) {
                    return
                }

                val jsCode = """
                    (function() {
                        var elements = document.querySelectorAll('*');
                        for (var i = 0; i < elements.length; i++) {
                            if (elements[i].className.includes('ad')) {
                                elements[i].style.display = 'none';
                            }
                        }
                    })();
                """
                webView.evaluateJavascript(jsCode, null)
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                progressBar.progress = progress
                if (progress == 100) {
                    progressBar.visibility = View.INVISIBLE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
        webView.loadUrl(urlPassed)
        titleBar.text = Uri.parse(urlPassed).host.toString()

        closeBtn.setOnClickListener {
            with(webView) {
                loadUrl("about:blank")
                clearHistory()
                clearFormData()
                clearCache(true)
                deleteDatabase("webview.db")
                deleteDatabase("webviewCache.db")
                WebStorage.getInstance().deleteAllData()
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            }
            finish()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            val bfList = webView.copyBackForwardList()
            val bfItem = bfList.getItemAtIndex(bfList.currentIndex - 1)
            titleBar.text = Uri.parse(bfItem.url).host.toString()
            webView.goBack()
        } else {
            with(webView) {
                loadUrl("about:blank")
                clearHistory()
                clearFormData()
                clearCache(true)
                deleteDatabase("webview.db")
                deleteDatabase("webviewCache.db")
                WebStorage.getInstance().deleteAllData()
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            }
            super.onBackPressed()
            finish()
        }
    }
}

class IncognitoWebView : WebView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val inputConnection = super.onCreateInputConnection(outAttrs) ?: return null
        outAttrs.imeOptions = outAttrs.imeOptions or 0x1000000
        return inputConnection
    }
}
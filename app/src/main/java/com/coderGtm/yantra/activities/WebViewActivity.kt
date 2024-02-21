package com.coderGtm.yantra.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.coderGtm.yantra.R
import com.google.android.material.button.MaterialButton


class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var titleBar: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        var urlPassed = intent.getStringExtra("url")
        if (urlPassed == null) {
            urlPassed = "https://www.google.com"
        }

        webView = findViewById(R.id.webView)
        val closeBtn: MaterialButton = findViewById(R.id.closeBtn)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        titleBar = findViewById(R.id.title)

        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.clearHistory()
        webView.clearFormData()
        webView.clearCache(true)
        progressBar.visibility = ProgressBar.INVISIBLE
        titleBar.movementMethod = ScrollingMovementMethod()
        titleBar.setHorizontallyScrolling(true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                progressBar.visibility = ProgressBar.VISIBLE
                progressBar.progress = 0
                view.loadUrl(request.url.toString())
                titleBar.text = request.url.host.toString()
                return true
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
        super.onBackPressed()
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
            finish()
        }
    }
}
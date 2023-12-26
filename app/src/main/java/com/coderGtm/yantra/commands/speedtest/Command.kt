package com.coderGtm.yantra.commands.speedtest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.activities.SettingsActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "speedtest",
        helpTitle = "speedtest",
        description = "Opens a small GUI speedtest utility to check your internet speed, powered by openspeedtest.com."
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'speedtest' command does not take any parameters", terminal.theme.errorTextColor)
            return
        }
        output("Loading Speedtest utility...", terminal.theme.successTextColor)
        val view = LayoutInflater.from(terminal.activity).inflate(R.layout.dialog_speedtest , null)
        val dialog = MaterialAlertDialogBuilder(terminal.activity)
            .setView(view)
            .setCancelable(false)
            .show()
        val webView = dialog.findViewById<WebView>(R.id.webView)
        webView?.settings?.javaScriptEnabled = true
        webView?.webViewClient = object : android.webkit.WebViewClient() {
            // on error
            override fun onReceivedError(view: WebView?, webResourceRequest: android.webkit.WebResourceRequest?, webResourceError: android.webkit.WebResourceError?) {
                super.onReceivedError(view, webResourceRequest, webResourceError)
                output("Error loading speedtest utility", terminal.theme.errorTextColor)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    output(webResourceError?.description.toString(), terminal.theme.errorTextColor)
                }
                dialog.dismiss()
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                dialog.findViewById<TextView>(R.id.loadingText)?.visibility = android.view.View.GONE
            }
            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }
        }
        dialog.findViewById<Button>(R.id.closeBtn)?.setOnClickListener {
            dialog.dismiss()
        }
        webView?.loadUrl("file:///android_asset/speedtest.html")
    }
}
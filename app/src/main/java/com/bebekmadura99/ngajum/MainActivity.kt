package com.bebekmadura99.ngajum

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

class MainActivity : AppCompatActivity() {

    companion object {
        private const val WEB_APP_URL =
            "https://script.google.com/macros/s/AKfycbw3UDpv71BADTt3ox5kKhyhSn-fhYnX3xaSPPh6IwKKs3v7RjGkJVHQSD9yOjTAYj71/exec"
        private const val USER_AGENT =
            "BebekMadura99Ngajum AndroidApp"
    }

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        configureWebView()
        configureBackButton()

        if (savedInstanceState == null) {
            webView.loadUrl(WEB_APP_URL)
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.loadsImagesAutomatically = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportMultipleWindows(false)
        settings.userAgentString = USER_AGENT
        settings.allowFileAccess = false
        settings.allowContentAccess = true

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(
                settings,
                WebSettingsCompat.FORCE_DARK_OFF
            )
        }

        webView.setBackgroundColor(Color.WHITE)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return handleUrl(request?.url?.toString() ?: "")
            }

            @Deprecated("Deprecated in Android API")
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                return handleUrl(url ?: "")
            }

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: android.graphics.Bitmap?
            ) {
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {
                progressBar.visibility = View.GONE
            }
        }

        webView.webChromeClient =
            object : android.webkit.WebChromeClient() {
                override fun onProgressChanged(
                    view: WebView?,
                    newProgress: Int
                ) {
                    progressBar.progress = newProgress
                    progressBar.visibility =
                        if (newProgress >= 100) View.GONE
                        else View.VISIBLE
                }
            }
    }

    private fun handleUrl(url: String): Boolean {
        if (url.isBlank()) return false

        val uri = Uri.parse(url)
        val host = uri.host ?: ""

        val isOwnedUrl =
            host == "script.google.com" ||
            host.endsWith(".googleusercontent.com") ||
            host == "accounts.google.com"

        if (isOwnedUrl) {
            return false
        }

        return try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Tidak dapat membuka tautan.",
                Toast.LENGTH_SHORT
            ).show()
            true
        }
    }

    private fun configureBackButton() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        finish()
                    }
                }
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }
}

package com.divyanshu.dailysphere

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val webView: WebView = findViewById(R.id.webView)
        val url = intent.getStringExtra("URL")

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        // Configure WebView settings for security
        configureWebViewSettings(webView)

        if (url != null) {
            webView.loadUrl(url)
        } else {
            Toast.makeText(this, "No link available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureWebViewSettings(webView: WebView) {
        // Disable JavaScript by default
        webView.settings.javaScriptEnabled = false

        // Allow JavaScript only if the URL is trusted
        webView.settings.javaScriptEnabled = true

        // Prevent file access for better security
        webView.settings.allowFileAccess = false

        // Block popups
        webView.settings.javaScriptCanOpenWindowsAutomatically = false

        // Enable HTTPS-only URLs
        webView.settings.domStorageEnabled = true
    }

    override fun onBackPressed() {
        val webView: WebView = findViewById(R.id.webView)
        if (webView.canGoBack()) {
            webView.goBack()  // Go back to the previous page in the WebView
        } else {
            super.onBackPressed()  // Exit the activity if no more pages to go back to
        }
    }
}

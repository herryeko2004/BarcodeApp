package com.bmt_jatim.barcodeapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class WebViewActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val webView = findViewById<WebView>(R.id.webView)
        val progressBar = findViewById<ProgressBar>(R.id.webProgressBar)

        // Tombol back di toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val url = "http://code91.bmtnujatim.id:8887/stock/"

// Atur WebView
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.builtInZoomControls = false
        webSettings.displayZoomControls = false
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE

        // Tambahan agar skala halaman menyesuaikan layar
        webView.setInitialScale(1)
        webView.isHorizontalScrollBarEnabled = false
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

        // Tambahkan User-Agent yang friendly
        webSettings.userAgentString =
            webSettings.userAgentString + " AndroidAppWebView"


        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE

                // ✅ Inject meta viewport agar layout web menyesuaikan layar
                webView.evaluateJavascript(
                    """
            if (!document.querySelector('meta[name="viewport"]')) {
                let meta = document.createElement('meta');
                meta.name = 'viewport';
                meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
                document.head.appendChild(meta);
            }
            """.trimIndent(), null
                )
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = newProgress
                if (newProgress == 100) progressBar.visibility = View.GONE
            }
        }

        webView.loadUrl(url)

        // ✅ handle tombol back HP (cara modern)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }
}

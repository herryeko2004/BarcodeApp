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

        // 🔧 Pengaturan WebView
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        settings.defaultTextEncodingName = "utf-8"
        settings.setSupportZoom(false)

        // 🔧 Pastikan halaman bisa menyesuaikan layar penuh
        webView.isHorizontalScrollBarEnabled = false
        webView.isVerticalScrollBarEnabled = true
        webView.scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY

        // Jika halaman mengandung JS redirect, tetap di WebView
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE

                // ✅ Inject viewport & auto zoom supaya 6 kolom muat layar HP
                webView.evaluateJavascript(
                    """
            (function() {
                // Pastikan ada meta viewport
                var meta = document.querySelector('meta[name=viewport]');
                if (!meta) {
                    meta = document.createElement('meta');
                    meta.name = 'viewport';
                    meta.content = 'width=device-width, initial-scale=0.9, maximum-scale=0.9, user-scalable=no';
                    document.getElementsByTagName('head')[0].appendChild(meta);
                } else {
                    meta.setAttribute('content', 'width=device-width, initial-scale=0.9, maximum-scale=0.9, user-scalable=no');
                }
                // Skala body agar tabel muat
                document.body.style.transform = 'scale(0.99)';
                document.body.style.transformOrigin = '0 0';
                document.body.style.width = '111%';
            })();
            """.trimIndent(), null
                )
            }
        }


        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                progressBar.progress = newProgress
            }
        }

        webView.loadUrl(url)

        // 🔙 Tombol back HP
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack()
                else finish()
            }
        })
    }
}

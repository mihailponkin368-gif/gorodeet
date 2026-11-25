package com.gorodeet.app

import android.app.Activity
import android.os.Bundle
import android.webkit.*
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                if (!url.contains("transport.orgp.spb.ru")) return null
                return try {
                    val conn = URL(url).openConnection() as HttpURLConnection
                    conn.connectTimeout = 10000
                    conn.readTimeout = 10000
                    val code = conn.responseCode
                    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                    WebResourceResponse(
                        conn.contentType ?: "application/octet-stream",
                        null,
                        ByteArrayInputStream(stream?.readBytes() ?: ByteArray(0))
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }

        webView.loadUrl("file:///android_asset/index.html")
        setContentView(webView)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}

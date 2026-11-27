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

            // Блокируем любые переходы кроме нашего файла и API транспорта
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return true
                return !(url.startsWith("file:///android_asset/")
                        || url.startsWith("https://transport.orgp.spb.ru"))
            }

            // Прокси: пропускаем только точный хост API по https
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val uri = request?.url ?: return null
                val host = uri.host ?: return null
                if (uri.scheme != "https") return null
                if (host != "transport.orgp.spb.ru" &&
                    !host.endsWith(".transport.orgp.spb.ru")) return null

                return try {
                    val conn = URL(uri.toString()).openConnection() as HttpURLConnection
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
                    // При ошибке не пускаем запрос наружу, а отдаём пустой ответ
                    WebResourceResponse(
                        "text/plain",
                        "utf-8",
                        ByteArrayInputStream(ByteArray(0))
                    )
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

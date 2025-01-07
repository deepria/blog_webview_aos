package com.blog

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import android.webkit.WebChromeClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.webview)

        webView.webChromeClient = WebChromeClient()
        // WebView 설정
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true // JavaScript 활성화
        webSettings.domStorageEnabled = true // 로컬 저장소 활성화

        // WebViewClient 설정 (내부에서 페이지 열기)
        webView.webViewClient = WebViewClient()

        // URL 로드
        webView.loadUrl("https://main.d39hqh4ds9p1ue.amplifyapp.com")
    }
}


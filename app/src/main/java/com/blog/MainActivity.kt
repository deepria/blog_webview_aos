package com.blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import android.webkit.WebChromeClient
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //액션바 숨김처리
        actionBar?.hide()


        // 웹뷰
        setContentView(R.layout.activity_main)
        val webView: WebView = findViewById(R.id.webview)
        webView.webChromeClient = WebChromeClient()
        // WebView 설정
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true // JavaScript 활성화
        webSettings.domStorageEnabled = true // 로컬 저장소 활성화
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // WebViewClient 설정 (내부에서 페이지 열기)
        webView.webViewClient = WebViewClient()

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                openFilePicker()
                return true
            }
        }

        // URL 로드
        webView.loadUrl("https://main.d39hqh4ds9p1ue.amplifyapp.com")
//        webView.loadUrl("http://127.0.0.1:5173")
    }
    // 파일 선택기 열기
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }

    // 파일 선택 결과 처리
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                filePathCallback?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(result.resultCode, data)
                )
            } else {
                filePathCallback?.onReceiveValue(null)
            }
            filePathCallback = null
        }
}


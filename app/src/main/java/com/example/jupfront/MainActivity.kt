package com.example.jupfront

import android.os.Bundle
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var webViewLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // WebView 초기화
        webView = findViewById(R.id.webview)


        webView.settings.run {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            setLoadWithOverviewMode(true) //메타태그 허용
            setDomStorageEnabled(true) // 로컬저장소 허용
        }

        // 웹 페이지 로드
        webView.loadUrl("https://lets-jupjup.com")


    }
}

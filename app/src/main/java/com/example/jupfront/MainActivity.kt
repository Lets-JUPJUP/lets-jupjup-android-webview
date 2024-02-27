package com.example.jupfront

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import java.net.URISyntaxException


class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var webViewLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 저장된 토큰을 불러와서 확인
        val sharedPreferences = applicationContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val savedToken = sharedPreferences.getString("firebase_token", "")
        Log.d("MainActivity", "Saved token: $savedToken")

        // WebView 초기화
        webView = findViewById(R.id.webview)


        webView.settings.run {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            setLoadWithOverviewMode(true) //메타태그 허용
            setDomStorageEnabled(true) // 로컬저장소 허용
        }

        webView.webChromeClient = object: WebChromeClient() {

            /// ---------- 팝업 열기 ----------
            /// - 카카오 JavaScript SDK의 로그인 기능은 popup을 이용합니다.
            /// - window.open() 호출 시 별도 팝업 webview가 생성되어야 합니다.
            ///
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean {

                // 웹뷰 만들기
                var childWebView = WebView(view.context)
                Log.d("TAG", "웹뷰 만들기")
                // 부모 웹뷰와 동일하게 웹뷰 설정
                childWebView.run {
                    settings.run {
                        javaScriptEnabled = true
                        javaScriptCanOpenWindowsAutomatically = true
                        setSupportMultipleWindows(true)
                        setLoadWithOverviewMode(true) //메타태그 허용
                        setDomStorageEnabled(true) // 로컬저장소 허용
                    }
                    layoutParams = view.layoutParams
                    webViewClient = view.webViewClient
                    webChromeClient = view.webChromeClient
                }

                // 화면에 추가하기
                webViewLayout.addView(childWebView)
                // TODO: 화면 추가 이외에 onBackPressed() 와 같이
                //       사용자의 내비게이션 액션 처리를 위해
                //       별도 웹뷰 관리를 권장함
                //   ex) childWebViewList.add(childWebView)

                // 웹뷰 간 연동
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = childWebView
                resultMsg.sendToTarget()

                return true
            }

            /// ---------- 팝업 닫기 ----------
            /// - window.close()가 호출되면 앞에서 생성한 팝업 webview를 닫아야 합니다.
            ///
            override fun onCloseWindow(window: WebView) {
                super.onCloseWindow(window)

                // 화면에서 제거하기
                webViewLayout.removeView(window)
                // TODO: 화면 제거 이외에 onBackPressed() 와 같이
                //       사용자의 내비게이션 액션 처리를 위해
                //       별도 웹뷰 array 관리를 권장함
                //   ex) childWebViewList.remove(childWebView)
            }
        }

        webView.webViewClient = object: WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                Log.d("TAG url", request.url.toString())
                Log.d("TAG scheme", request.url.scheme.toString())
                if (request.url.scheme == "https") {
                    //webView.loadUrl(request.url.toString())
                }

                if (request.url.scheme == "intent") {
                    try {
                        Log.d("TAG scheme", intent.getPackage().toString())
                        val intent = Intent.parseUri(request.url.toString(), Intent.URI_INTENT_SCHEME)
                        // 실행 가능한 앱이 있으면 앱 실행
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                            Log.d("TAG", "ACTIVITY: ${intent.`package`}")
                            return true
                        }

                        // Fallback URL이 있으면 현재 웹뷰에 로딩
                        val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                        if (fallbackUrl != null) {
                            view.loadUrl(fallbackUrl)
                            Log.d("TAG FALLBACK", "FALLBACK: $fallbackUrl")
                            return true
                        }

                        Log.e("TAG", "Could not parse anythings")

                    } catch (e: URISyntaxException) {
                        Log.e("TAG", "Invalid intent request", e)
                    }
                }

                // 나머지 서비스 로직 구현


                Log.d("TAG", "return false")
                return false
            }
        }

        webView.webViewClient = object: WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView,request: WebResourceRequest): Boolean {
                Log.d("TAG", request.url.toString())

                if (request.url.scheme == "intent") {
                    try {
                        // Intent 생성
                        val intent = Intent.parseUri(request.url.toString(), Intent.URI_INTENT_SCHEME)

                        // 실행 가능한 앱이 있으면 앱 실행
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                            Log.d("TAG", "ACTIVITY: ${intent.`package`}")
                            Log.d("TAG", "카카오톡 실행")
                            return true
                        }

                        Log.d("TAG", "카카오톡 공유하기 실행 못함")

                        // 실행 못하면 웹뷰는 카카오톡 공유하기 화면으로 이동
                        webView.loadUrl("http://kakao-share.s3-website.ap-northeast-2.amazonaws.com/")

                        // 구글 플레이 카카오톡 마켓으로 이동
                        val intentStore = Intent(Intent.ACTION_VIEW)
                        intentStore.addCategory(Intent.CATEGORY_DEFAULT)
                        intentStore.data = Uri.parse("market://details?id=com.kakao.talk")
                        Log.d("TAG", "구글 플레이 카카오톡 마켓으로 이동")
                        startActivity(intentStore)

                    } catch (e: URISyntaxException) {
                        Log.e("TAG", "!!! 에러 Invalid intent request", e)
                    }
                }
                // 나머지 서비스 로직 구현
                return false
            }
        }

        // 웹 페이지 로드
        webView.loadUrl("https://lets-jupjup.com")


    }
}

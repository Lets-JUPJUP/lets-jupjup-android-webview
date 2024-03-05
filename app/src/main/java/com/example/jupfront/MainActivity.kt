package com.example.jupfront

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*;
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.net.URISyntaxException
import android.webkit.CookieManager
import com.example.jupfront.firebase.MyFirebaseMessagingService
import com.example.jupfront.firebase.TokenSender


class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var webViewLayout: FrameLayout
    private val client = OkHttpClient()
    var mFilePathCallback: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(this, MyFirebaseMessagingService::class.java)
        startService(intent)

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


                Log.d("TAG", "return false")
                return false
            }
        }

        webView.webViewClient = object: WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView,request: WebResourceRequest): Boolean {
                Log.d("TAG", request.url.toString())
                var accessToken: String? = null
                var flag = false;
                if (request.url.toString().startsWith("https://lets-jupjup.com/kakao-login")) {
                    flag = true
                }

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
                // 카카오 로그인이 성공한 경우
                if (flag) {
                    // firebase 토큰과 access Token 전송
                    accessToken = Uri.parse(request.url.toString()).getQueryParameter("accessToken")
                    val shredPref = applicationContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    with(shredPref.edit()) {
                        putString("access_token", accessToken)
                        apply()
                    }
                    val sharedPreferences = applicationContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    val savedToken = sharedPreferences.getString("firebase_token", "")
                    Log.d("SavedFCMToken", savedToken.toString());
                    TokenSender().sendTokenToServer(applicationContext, savedToken, accessToken)
                }

                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            // For Android 5.0+
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                // Ensure that there's no existing callback
                mFilePathCallback = filePathCallback

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.setType("image/*")
                // 파일 n개 선택 가능하도록 처리
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

                resultLauncher.launch(intent)
                return true
            }

        }

        // 웹 페이지 로드
        webView.loadUrl("https://lets-jupjup.com")


    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val intent = result.data

            if (resultCode == Activity.RESULT_OK) {
                val clipData = intent?.clipData
                val results = mutableListOf<Uri>()

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        results.add(item.uri)
                    }
                } else {
                    intent?.data?.let { results.add(it) }
                }

                mFilePathCallback?.onReceiveValue(results.toTypedArray())
            } else {
                mFilePathCallback?.onReceiveValue(null)
            }

            mFilePathCallback = null
        }
}

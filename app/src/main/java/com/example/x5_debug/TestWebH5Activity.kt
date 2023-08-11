package com.example.x5_debug

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient

class TestWebH5Activity : AppCompatActivity() {

    private lateinit var mWebView: WebView
    private lateinit var webUrl: String
    private var mAudioManager: AudioManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_web_h5)
        webUrl = intent.getStringExtra("url") ?: ""

        mWebView = WebView(applicationContext)
        mWebView.layoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mWebView.settings.apply {
            javaScriptEnabled = true
            defaultTextEncodingName = "GBK"
            cacheMode = WebSettings.LOAD_DEFAULT
            textZoom = 100
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            loadsImagesAutomatically = true
            allowContentAccess = true
//            allowFileAccessFromFileURLs = true
//            allowUniversalAccessFromFileURLs = true
            loadWithOverviewMode = true
            useWideViewPort = true
            javaScriptCanOpenWindowsAutomatically = true
        }
        mWebView.webViewClient = WebViewClient()
//        mWebView.webChromeClient = object : WebChromeClient() {
//            override fun onPermissionRequest(request: PermissionRequest?) {
//                request?.grant(request.resources)
//            }
//        }
        val testH5 = findViewById<RelativeLayout>(R.id.test_h5)
        testH5.addView(mWebView)
        mWebView.loadUrl(webUrl)

        //获取系统的Audio管理者
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

    }

    override fun onDestroy() {
        try{
            mWebView.run {
                clearHistory()
                (parent as ViewGroup).removeView(mWebView)
                destroy()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                addOrReduceVolume(false)
                true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                addOrReduceVolume(true)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun addOrReduceVolume(isAdd: Boolean) {
        mAudioManager?.adjustStreamVolume(
            AudioManager.STREAM_VOICE_CALL,if (isAdd) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI)
    }
}
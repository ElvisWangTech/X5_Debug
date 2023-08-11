package com.example.x5_debug

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.x5_debug.tookit.UrlFormatTool
import com.example.x5_debug.tookit.X5Tool

class MainActivity : ComponentActivity() {

    private var logView: TextView?=null;
    private var webViewUrl: EditText?=null;
    private val tag: String = MainActivity::class.java.simpleName
    private val defaultUrl = "https://webresource.123kanfang.com/test/5i5j/index.html?type=1&cid=1&hid=501160900&m=WoAiWoJia-8361079_b1a67185-7cb9-4a99-b8e6-212de40af207&bid=8210307&title=%E9%80%9A%E5%B7%9E%E5%8D%8E%E4%B8%9A%E4%B8%9C%E6%96%B9%E7%8E%AB%E7%91%B0D%E5%8C%BA%EF%BC%8C%E4%B8%89%E5%B1%85%E5%AE%A4%EF%BC%8C%E6%96%B0%E7%A4%BE%E5%8C%BA%EF%BC%8C%E5%8D%95%E4%BB%B744800%E4%B8%80%E5%B9%B3.%27&equipment=pc";
    private var toActivity: ActivityResultLauncher<Intent>?=null;
    private var logHandler: Handler?=null;
    private var escaped: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        val looper = Looper.myLooper();
        if (looper != null) {
            logHandler = Handler(looper) {
                log(it.obj.toString(), it.what)
            }
        }
        X5Tool.init(applicationContext, logHandler)

        logView = findViewById(R.id.logView);
        webViewUrl = findViewById(R.id.webViewUrl)
        webViewUrl?.setText(defaultUrl);

        toActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val contents = it.data?.getStringExtra("SCAN_RESULT")
            val format = it.data?.getStringExtra("SCAN_RESULT_FORMAT")
            log("扫描结果：$contents - $format")
            if (!contents.isNullOrEmpty()) {
                webViewUrl?.setText(contents)
            }
        }
        initSpinner();
        bindEvents()
    }
    /**
     * 初始化tbs内核最小版本下拉选择框
     */
    private fun initSpinner() {
        val adapterView = ArrayAdapter(applicationContext, R.layout.item_select, X5Tool.coreMinVersions)
        adapterView.setDropDownViewResource(R.layout.item_dropdown)
        val sp = findViewById<Spinner>(R.id.spinner)
        sp.setPromptId(R.string.spinner_prompt)
        sp.adapter = adapterView;
        sp.setSelection(X5Tool.selectionIndex)
        sp.onItemSelectedListener = SpinnerListener()
    }

    class SpinnerListener: AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
            X5Tool.setSelectionPos(pos)
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            // Another interface callback
        }
    }

    /**
     * 绑定事件
     */
    private fun bindEvents() {
        // 打开
        val openBtn: Button = findViewById(R.id.openWebView);
        openBtn.setOnClickListener {
            log("准备打开x5 webview");
            val webViewUrl = findViewById<EditText>(R.id.webViewUrl);
            val formatUrl = UrlFormatTool.ensureProtocol(webViewUrl.text.toString(), "https", "http");
            openWebView(formatUrl)
        }
        // x5调试
        val inspectBtn: Button = findViewById(R.id.inspect)
        inspectBtn.setOnClickListener {
            openWebView("http://debugx5.qq.com")
        }
        // 扫一扫
        val scanBtn: Button = findViewById(R.id.scanQrcode)
        scanBtn.setOnClickListener {
            val intent = Intent("com.google.zxing.client.android.SCAN")
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
            toActivity?.launch(intent);
        }
        // 转义 - url转义
        val escapeBtn: Button = findViewById(R.id.escape)
        escapeBtn.setOnClickListener {
            val webViewUrl = findViewById<EditText>(R.id.webViewUrl);
            val url = webViewUrl.text.toString()
            val newUrl = if (escaped) UrlFormatTool.encodeUri(url) else UrlFormatTool.decodeUri(url)
            webViewUrl.setText(newUrl);
            escaped = !escaped
        }
        // 选择tbs最小内核版本
        val selectTbsMinVer: Button = findViewById(R.id.downloadX5);
        selectTbsMinVer.setOnClickListener {
            X5Tool.startDownload(applicationContext, X5Tool.isX5Ready)
        }

    }

    private fun openWebView(url: String) {
        log(url, Log.DEBUG)
        val intent = Intent(this, TestWebH5Activity::class.java).apply {
            putExtra("url", url)
        }
        startActivity(intent)
    }

    /**
     * 记录日志
     */
    private fun log(msg: String, level: Int = Log.INFO): Boolean {
        var iColor = R.color.info;
        var size = 40;
        when (level) {
            Log.DEBUG -> { Log.d(tag, msg); iColor = R.color.debug; size = 38; }
            Log.INFO -> { Log.i(tag, msg); iColor = R.color.info; size = 40; }
            Log.WARN -> { Log.w(tag, msg); iColor = R.color.warn; size = 42; }
            Log.ERROR -> { Log.e(tag, msg); iColor = R.color.error; size = 44; }
        }
        val formatMsg = "[$level]$tag->$msg\n"
        var spString = SpannableString(formatMsg);
        spString.setSpan(
            TextAppearanceSpan(null, Typeface.NORMAL, size,
                ColorStateList.valueOf(resources.getColor(iColor, resources.newTheme())),
                null
            ),
            0, formatMsg.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        logView?.append(spString)
        return true;
    }
}

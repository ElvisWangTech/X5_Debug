package com.example.x5_debug

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.x5_debug.tookit.UrlFormatTool
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.QbSdk.PreInitCallback
import com.tencent.smtt.sdk.TbsDownloader
import com.tencent.smtt.sdk.TbsListener

class MainActivity : ComponentActivity() {

    private var logView: TextView?=null;
    private var webViewUrl: EditText?=null;
    private val tag: String = MainActivity::class.java.simpleName
    private val defaultUrl = "https://webresource.123kanfang.com/test/5i5j/index.html?type=1&cid=1&hid=501160900&m=WoAiWoJia-8361079_b1a67185-7cb9-4a99-b8e6-212de40af207&bid=8210307&title=%E9%80%9A%E5%B7%9E%E5%8D%8E%E4%B8%9A%E4%B8%9C%E6%96%B9%E7%8E%AB%E7%91%B0D%E5%8C%BA%EF%BC%8C%E4%B8%89%E5%B1%85%E5%AE%A4%EF%BC%8C%E6%96%B0%E7%A4%BE%E5%8C%BA%EF%BC%8C%E5%8D%95%E4%BB%B744800%E4%B8%80%E5%B9%B3.%27&equipment=pc";
    private var toActivity: ActivityResultLauncher<Intent>?=null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        checkX5()

        logView = findViewById(R.id.logView);
        webViewUrl = findViewById(R.id.webViewUrl)
        webViewUrl?.setText(defaultUrl);

        toActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val contents = it.data?.getStringExtra("SCAN_RESULT")
            val format = it.data?.getStringExtra("SCAN_RESULT_FORMAT")
            log("扫描结果：$contents - $format")
            webViewUrl?.setText(contents)
        }
        bindEvents()
    }

    private fun checkX5() {
        QbSdk.initX5Environment(applicationContext, object : PreInitCallback {
            override fun onCoreInitFinished() {
                // 内核初始化完成，可能为系统内核，也可能为系统内核
                log( "x5内核初始化完成")
            }

            /**
             * 预初始化结束
             * 由于X5内核体积较大，需要依赖网络动态下发，所以当内核不存在的时候，默认会回调false，此时将会使用系统内核代替
             * @param isX5 是否使用X5内核
             */
            override fun onViewInitFinished(isX5: Boolean) {
                log( "x5预初始化完成，是否启用x5：$isX5")
                if (!isX5) {
                    log( "开始下载x5内核")
                    QbSdk.reset(applicationContext);
                    TbsDownloader.startDownload(applicationContext)
                } else {
                    log("x5内核版本：${QbSdk.getTbsVersion(applicationContext).toString()}")
                }
            }
        })

        // fixme: runOnUiThread的方案不是太好，用Handler实现消息传递比较好
        QbSdk.setTbsListener(object : TbsListener {
            override fun onDownloadFinish(p0: Int) {
                runOnUiThread(Runnable {
                    run {
                        log("x5下载完成")
                    }
                })
            }

            override fun onInstallFinish(p0: Int) {
                runOnUiThread(Runnable {
                    run {
                        log("x5安装完成, 内核版本：${QbSdk.getTbsVersion(applicationContext).toString()}")
                    }
                })

            }

            override fun onDownloadProgress(p0: Int) {
                Toast.makeText(applicationContext, "x5下载中$p0", Toast.LENGTH_SHORT).show()
            }

        })
        // 在调用TBS初始化、创建WebView之前进行如下配置
        QbSdk.setDownloadWithoutWifi(true);
        val map = HashMap<String, Any>();
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true;
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true;
        QbSdk.initTbsSettings(map);
    }

    /**
     * 绑定事件
     */
    private fun bindEvents() {
        val openBtn: Button = findViewById(R.id.openWebview);
        openBtn.setOnClickListener {
            log("准备打开x5 webview");
            val webViewUrl = findViewById<EditText>(R.id.webViewUrl);
            val formatUrl = UrlFormatTool.ensureProtocol(webViewUrl.text.toString(), "https", "http");
            openWebView(formatUrl)
        }
        val inspectBtn: Button = findViewById(R.id.inspect)
        inspectBtn.setOnClickListener {
            openWebView("http://debugx5.qq.com")
        }
        val scanBtn: Button = findViewById(R.id.scanQrcode)
        scanBtn.setOnClickListener {
            val intent = Intent("com.google.zxing.client.android.SCAN")
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
            toActivity?.launch(intent);
        }
    }

    private fun openWebView(url: String) {
        log(url)
        val intent = Intent(this, TestWebH5Activity::class.java).apply {
            putExtra("url", url)
        }
        startActivity(intent)
    }

    /**
     * 记录日志
     */
    fun log(msg: String, level: Int = Log.INFO) {
        when (level) {
            Log.DEBUG -> Log.d(tag, msg)
            Log.INFO -> Log.i(tag, msg)
            Log.WARN -> Log.w(tag, msg)
            Log.ERROR -> Log.e(tag, msg)
        }
        logView?.append("[$level]$tag->$msg\n")
    }
}

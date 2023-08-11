package com.example.x5_debug.tookit

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.TbsCommonCode
import com.tencent.smtt.sdk.TbsDownloader
import com.tencent.smtt.sdk.TbsListener

class X5Tool {
    companion object {
        private var logHandler: Handler?=null;
        var isX5Ready: Boolean = false;
        val coreMinVersions = arrayOf(-1, 46247, 46248, 46249, 46250); // -1 表示不限制，不限制表示拉取sdk支持的最低版本
        var selectionIndex = 0
        fun setSelectionPos(i: Int) {
            selectionIndex = i;
            logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "选择tbs最小内核版本：${coreMinVersions[i]}"))
        }
        fun formatErrMsg(errCode: Int): String {
            var errMsg = "unKnown error"
            when (errCode) {
                TbsCommonCode.DOWNLOAD_CANCEL_NOT_WIFI -> errMsg = "非Wi-Fi，不发起下载"
                TbsCommonCode.DOWNLOAD_CANCEL_REQUESTING -> errMsg = "下载请求中，不重复发起，取消下载"
                TbsCommonCode.DOWNLOAD_FLOW_CANCEL -> errMsg = "带宽不允许，下载取消"
                TbsCommonCode.DOWNLOAD_CANCEL_PRIVATE_CDN_MODE -> errMsg = "不支持私有CDN模式，下载取消"
                TbsCommonCode.DOWNLOAD_NO_NEED_REQUEST -> errMsg = "不发起下载请求"
                TbsCommonCode.INSTALL_FOR_PREINIT_CALLBACK -> errMsg = "预加载中间态，非异常，可忽略"
                TbsCommonCode.NETWORK_UNAVAILABLE -> errMsg = "网络不可用"
                TbsCommonCode.STARTDOWNLOAD_OUT_OF_MAXTIME -> errMsg = "发起下载次数超过1次"
            }
            return errMsg;
        }

        /**
         * 初始化x5环境：检查x5并绑定监听
         */
        fun init(applicationContext: Context, logHandler:Handler?) {
            QbSdk.initX5Environment(applicationContext, object : QbSdk.PreInitCallback {
                override fun onCoreInitFinished() {
                    // 内核初始化完成，可能为系统内核，也可能为系统内核
                    logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "x5内核初始化完成"))
                }

                /**
                 * 预初始化结束
                 * 由于X5内核体积较大，需要依赖网络动态下发，所以当内核不存在的时候，默认会回调false，此时将会使用系统内核代替
                 * @param isX5 是否使用X5内核
                 */
                override fun onViewInitFinished(isX5: Boolean) {
                    logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "x5预初始化完成，是否启用x5：$isX5"))
                    isX5Ready = isX5;
                    if (isX5) {
                        val tbsVer = QbSdk.getTbsVersion(applicationContext).toString()
                        val tbsSdkVer = QbSdk.getTbsSdkVersion().toString()
                        logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "内核版本：${tbsVer}, sdk版本：${tbsSdkVer}"))
                    }

                }
            })

            QbSdk.setTbsListener(object : TbsListener {
                override fun onDownloadFinish(errCode: Int) {
                    // 虽然下载结束，但有可能失败
                    if (errCode != TbsCommonCode.DOWNLOAD_SUCCESS) {
                        logHandler?.sendMessage(Message.obtain(logHandler, Log.WARN, "x5下载异常-errCode:$errCode errMsg: ${X5Tool.formatErrMsg(errCode)}"))
                    } else {
                        logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "x5下载完成"))
                    }
                }

                override fun onInstallFinish(errCode: Int) {
                    val tbsVer = QbSdk.getTbsVersion(applicationContext).toString()
                    val tbsSdkVer = QbSdk.getTbsSdkVersion().toString()
                    if (errCode != TbsCommonCode.INSTALL_SUCCESS) {
                        logHandler?.sendMessage(Message.obtain(logHandler, Log.WARN, "x5安装异常-errCode:$errCode errMsg: ${X5Tool.formatErrMsg(errCode)}"))
                    } else {
                        logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "x5安装完成, 内核版本：${tbsVer}, sdk版本：${tbsSdkVer}"))
                    }
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
            this.logHandler = logHandler;
        }

        /**
         * 开始下载x5内核
         */
        fun startDownload(applicationContext: Context, reset: Boolean = false) {
            logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "准备下载x5内核， reset: $reset"))
            if (reset) {
                QbSdk.reset(applicationContext);
                isX5Ready = false;
            }
            val selectedCoreMinVersion = coreMinVersions[selectionIndex];
            QbSdk.setCoreMinVersion(selectedCoreMinVersion)
            if (!isX5Ready && !TbsDownloader.isDownloading()) {
                logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "开始下载x5内核"))
                TbsDownloader.startDownload(applicationContext)
            } else {
                logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "已存在，x5内核版本：${QbSdk.getTbsVersion(applicationContext).toString()}"))
            }
        }
    }
}
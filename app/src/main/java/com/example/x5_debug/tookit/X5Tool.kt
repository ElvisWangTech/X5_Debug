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
        fun formatErrMsg(errCode: Int): String {
            var errMsg = "unKnown error"
            when (errCode) {
                TbsCommonCode.DOWNLOAD_CANCEL_NOT_WIFI -> errMsg = "cancel by not wifi"
                TbsCommonCode.DOWNLOAD_CANCEL_REQUESTING -> errMsg = "cancel requesting"
                TbsCommonCode.DOWNLOAD_FLOW_CANCEL -> errMsg = "cancel by flow"
                TbsCommonCode.DOWNLOAD_CANCEL_PRIVATE_CDN_MODE -> errMsg = "cancel by private cdn mode"
                TbsCommonCode.DOWNLOAD_NO_NEED_REQUEST -> errMsg = "no need download"
                TbsCommonCode.INSTALL_FOR_PREINIT_CALLBACK -> errMsg = "preinit callback"
                TbsCommonCode.NETWORK_UNAVAILABLE -> errMsg = "network unavailable"
                TbsCommonCode.STARTDOWNLOAD_OUT_OF_MAXTIME -> errMsg = "out of time"
            }
            return errMsg;
        }

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
                    if (!isX5 && !TbsDownloader.isDownloading()) {
                        logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "开始下载x5内核"))
                        QbSdk.reset(applicationContext);
                        TbsDownloader.startDownload(applicationContext)
                    } else {
                        logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "x5内核版本：${QbSdk.getTbsVersion(applicationContext).toString()}"))
                    }
                }
            })

            QbSdk.setTbsListener(object : TbsListener {
                override fun onDownloadFinish(errCode: Int) {
                    logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "x5下载完成"))
                    // 虽然下载结束，但有可能失败
                    if (errCode != TbsCommonCode.DOWNLOAD_SUCCESS) {
                        logHandler?.sendMessage(Message.obtain(logHandler, Log.WARN, "x5下载异常-errCode:$errCode errMsg: ${X5Tool.formatErrMsg(errCode)}"))
                    }
                }

                override fun onInstallFinish(errCode: Int) {
                    logHandler?.sendMessage(Message.obtain(logHandler, Log.INFO, "x5安装完成, 内核版本：${QbSdk.getTbsVersion(applicationContext).toString()}"))
                    if (errCode != TbsCommonCode.INSTALL_SUCCESS) {
                        logHandler?.sendMessage(Message.obtain(logHandler, Log.WARN, "x5安装异常-errCode:$errCode errMsg: ${X5Tool.formatErrMsg(errCode)}"))
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
        }
    }
}
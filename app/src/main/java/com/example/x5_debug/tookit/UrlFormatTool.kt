package com.example.x5_debug.tookit

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * 格式化url的工具
 */
class UrlFormatTool {
    companion object {
        /**
         * 确保是指定的协议开头
         */
        fun ensureProtocol(url: String, proto: String, vararg args: String): String {
            if ( url.startsWith(proto) ) return url;
            for(it in args){
                if ( url.startsWith(it) ) return url;
            }
            // 双斜杠开头
            if ( url.startsWith("//") ) proto.plus(":").plus(url);
            return proto.plus("://").plus(url);
        }

        fun ensureVconsole(url: String): String {
            if (url.contains("vconsole")) {
                return url;
            }
            return "$url&vconsole=1";
        }

        fun encodeUri(url: String): String {
            return url.replaceAfterLast('?', URLEncoder.encode(url.substringAfterLast('?'), "utf-8"))
        }

        fun decodeUri(url: String): String {
            return url.replaceAfterLast('?', URLDecoder.decode(url.substringAfterLast('?'), "utf-8"))
        }
    }

}
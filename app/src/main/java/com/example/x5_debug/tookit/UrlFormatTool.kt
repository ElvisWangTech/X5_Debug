package com.example.x5_debug.tookit

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
    }

}
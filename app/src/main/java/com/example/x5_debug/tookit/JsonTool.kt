package com.example.x5_debug.tookit

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

object JsonTool {
    private fun readResolve(): Any {
        return JsonTool
    }
    fun parse(jsonString: String): JSONObject? {
        var obj: JSONObject? = null
        try {
            obj = JSONObject(jsonString)
        } catch (e: JSONException) {
            Log.e("elvis debug:", "parse json error ${e.toString()}");
        }
        return obj
    }

    fun stringify(jsonObj: JSONObject): String {
        return jsonObj.toString();
    }
}
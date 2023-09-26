package com.example.regressiontest.util

import android.os.Build
import android.widget.Toast
import com.example.regressiontest.manager.SessionManager
import org.json.JSONObject

object Util {
    fun getClientName(): String {
        return "Android/native/${Build.VERSION.RELEASE}(SDK: ${Build.VERSION.SDK_INT})"
    }

    fun getClientMetadata(): String {
        val metadata = JSONObject()
        metadata.put("type", "client")
        metadata.put("info", getClientName())
        return metadata.toString()
    }

    fun showToast(context: android.content.Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}

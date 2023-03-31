package com.ntt.skyway.core.util

import android.util.Log
import com.ntt.skyway.BuildConfig

object Logger {
    enum class LogLevel {
        NONE, ERROR, WARN, INFO, DEBUG, VERBOSE;
    }

    var logLevel = LogLevel.INFO
    var webRTCLog = false
    private const val tag = "skyway:${BuildConfig.SkyWayVer}"

    fun logE(message: String) = log(LogLevel.ERROR, message)
    fun logW(message: String) = log(LogLevel.WARN, message)
    fun logI(message: String) = log(LogLevel.INFO, message)
    fun logD(message: String) = log(LogLevel.DEBUG, message)
    fun logV(message: String) = log(LogLevel.VERBOSE, message)

    fun log(level: LogLevel, message: String) {
        val frame = Exception().stackTrace[2]
        printLog(level, message, frame.methodName, frame.fileName, frame.lineNumber)
    }

    fun log(level: LogLevel, message: String, tag: String) {
        val frame = Exception().stackTrace[2]
        printLog(level, "$tag: $message", frame.methodName, frame.fileName, frame.lineNumber)
    }

    fun log(level: Int, message: String, fileName: String, methodName: String, lineNumber: Int) {
        printLog(LogLevel.values()[level], message, methodName, fileName, lineNumber)
    }

    private fun printLog(level: LogLevel, message: String, methodName: String, fileName: String, lineNumber: Int) {
        if (level > logLevel) return
        val text = "$message | $methodName($fileName:$lineNumber)"
        when (level) {
            LogLevel.NONE -> return
            LogLevel.ERROR -> Log.e(tag, "\uD83D\uDCD5 $text")
            LogLevel.WARN -> Log.w(tag, "\uD83D\uDCD9 $text")
            LogLevel.INFO -> Log.i(tag, "\uD83D\uDCD8 $text")
            LogLevel.DEBUG -> Log.d(tag, "\uD83D\uDCD3 $text")
            LogLevel.VERBOSE -> Log.v(tag, "\uD83D\uDCD7 $text")
        }
    }
}

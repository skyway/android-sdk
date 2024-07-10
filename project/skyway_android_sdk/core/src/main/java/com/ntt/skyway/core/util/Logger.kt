package com.ntt.skyway.core.util

import android.util.Log
import com.ntt.skyway.BuildConfig

/**
 * ログの出力を行うクラス。ログの出力機能はSkyWay SDKによって呼び出されます。
 */
object Logger {
    /**
     * ログのレベル一覧。
     */
    enum class LogLevel {
        /**
         * ログを出力しません。
         */
        NONE,

        /**
         * デフォルト値。異常に関する情報を出力します。このエラーが発生したメソッドからは無効値が返されます。
         */
        ERROR,

        /**
         * SDK内部で発生した一時的なエラーに関する情報を出力します。
         */
        WARN,

        /**
         * SDK が提供しているメソッドの呼び出しに関する情報を出力します。
         */
        INFO,

        /**
         * イベントの発火やリクエスト・レスポンスに関する情報など、デバッグ時に参考となる情報を出力します。
         */
        DEBUG,

        /**
         * メモリの破棄など、最も詳細なログを出力します。
         */
        VERBOSE;
    }

    /**
     * 出力するログのレベル。アプリケーションの開発時には[LogLevel.VERBOSE]にしておくことをお勧めします。
     */
    var logLevel = LogLevel.INFO

    /**
     * WebRTCに関するログの出力可否。trueにした場合はメディアや通信に関する詳細な情報を出力することができますが、ログの量が多くなることに注意してください。
     */
    var webRTCLog = false

    /**
     * ログが出力される際に発火するハンドラ。
     */
    var onLogHandler: ((level: LogLevel, message: String) -> Unit)? = null

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

    private fun printLog(
        level: LogLevel,
        message: String,
        methodName: String?,
        fileName: String?,
        lineNumber: Int?
    ) {
        if (level > logLevel) return
        val text = "${getIcon(level)} $message | $methodName($fileName:$lineNumber)"

        onLogHandler?.invoke(level, text)

        when (level) {
            LogLevel.NONE -> return
            LogLevel.ERROR -> Log.e(tag, text)
            LogLevel.WARN -> Log.w(tag, text)
            LogLevel.INFO -> Log.i(tag, text)
            LogLevel.DEBUG -> Log.d(tag, text)
            LogLevel.VERBOSE -> Log.v(tag, text)
        }
    }

    private fun getIcon(level: LogLevel): String {
        return when (level) {
            LogLevel.NONE -> ""
            LogLevel.ERROR -> "\uD83D\uDCD5"
            LogLevel.WARN -> "\uD83D\uDCD9"
            LogLevel.INFO -> "\uD83D\uDCD8"
            LogLevel.DEBUG -> "\uD83D\uDCD3"
            LogLevel.VERBOSE -> "\uD83D\uDCD7"
        }
    }
}

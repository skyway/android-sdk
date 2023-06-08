/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core

import android.content.Context
import com.google.gson.Gson
import com.ntt.skyway.BuildConfig
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.network.HttpClient
import com.ntt.skyway.core.network.WebSocketClientFactory
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.plugin.Plugin
import com.ntt.skyway.plugin.remotePerson.RemotePersonPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *  Experimental APIを表すannotation。
 */
@RequiresOptIn(message = "This API is experimental.")
annotation class SkyWayOptIn

/**
 * アプリケーション内でSkyWayの利用に関する設定を行うクラス。
 */
object SkyWayContext {
    /**
     *  SkyWayの利用に関する設定。
     *  @param authToken 認証トークン。
     *  @param logLevel SkyWayのログレベル。
     *  @param webRTCLog SkyWayのログの内、特にWebRTCに関するログを出力するか。
     */
    data class Options(
        val authToken: String,
        val logLevel: Logger.LogLevel = Logger.LogLevel.INFO,
        val webRTCLog: Boolean = false,
        val enableHardwareCodec: Boolean = true,
        val rtcApi: RtcApi? = null,
        val iceParams: IceParams? = null,
        val signaling: Signaling? = null,
        val rtcConfig: RtcConfig? = null,
        val sfu: Sfu? = null,
        val token: Token? = null,
    ) {
        internal fun toJson(): String {
            return Gson().toJson(this)
        }
    }

    data class RtcApi(
        val domain: String? = null,
        val secure: Boolean? = null
    ) {
        internal fun toJson(): String {
            return Gson().toJson(this)
        }
    }

    data class IceParams(
        val domain: String? = null,
        val version: Int? = null,
        val secure: Boolean? = null
    ) {
        internal fun toJson(): String {
            return Gson().toJson(this)
        }
    }

    data class Signaling(
        val domain: String? = null,
        val secure: Boolean? = null
    ) {
        internal fun toJson(): String {
            return Gson().toJson(this)
        }
    }

    /**
     *  TURNの利用に関する設定。TURN_ONLYにすると必ずTURNの利用を試みます。
     */
    enum class TurnPolicy {
        ENABLE, DISABLE, TURN_ONLY
    }

    /**
     *  WebRTC通信に関する設定。
     */
    data class RtcConfig(val timeout: Int? = null, val policy: TurnPolicy? = null) {
        internal fun toJson(): String {
            return Gson().toJson(this)
        }
    }

    data class Token(val tokenReminderTimeSec: Int? = null) {
        internal fun toJson(): String {
            return Gson().toJson(this)
        }
    }

    data class Sfu(
        val domain: String? = null,
        val version: Int? = null,
        val secure: Boolean? = null
    ) {
        internal fun toJson(): String {
            return Gson().toJson(this)
        }
    }


    data class Error(val message: String)

    var isSetup: Boolean = false
        private set

    /**
     *  SkyWayの利用中にネットワークの瞬断などが原因で再接続処理が開始した時に発火するハンドラ。
     */
    var onReconnectStartHandler: (() -> Unit)? = null

    /**
     *  SkyWayの再接続処理が完了した時に発火するハンドラ。
     */
    var onReconnectSuccessHandler: (() -> Unit)? = null

    /**
     *  SkyWayの利用中に致命的なエラーが起きた場合に発火するハンドラ。
     *  初期化時の認証の失敗や、ネットワークが切断され回復不能となった場合などに発火します。
     */
    var onErrorHandler: ((error: Error) -> Unit)? = null

    /**
     *  SkyWayのトークンの更新が必要な時に発火するハンドラ。
     */
    var onTokenRefreshingNeededHandler: (() -> Unit)? = null

    /**
     *  SkyWayのトークンの有効期限が切れた時に発火するハンドラ。
     */
    var onTokenExpiredHandler: (() -> Unit)? = null

    /**
     *  登録されている[Plugin]の一覧。
     */
    val plugins: MutableList<Plugin> = mutableListOf()

    const val version:String = BuildConfig.SkyWayVer

    init {
        System.loadLibrary("skyway_android")
        registerPlugin(RemotePersonPlugin())
    }

    /**
     *  SkyWayの利用を開始します。
     *  既に開始している場合はログを出力し、何も行いません。
     *  Optionを変更したい場合は一度[dispose]を行ってから再度実行してください。
     *
     *  @param context コンテキスト。
     *  @param option SkyWayの認証や通信の設定、ログレベルの設定。
     *  @param onErrorHandler 非推奨:[SkyWayContext.onErrorHandler]にセットするハンドラ。
     */
    @JvmStatic
    suspend fun setup(
        context: Context,
        option: Options,
        onErrorHandler: ((error: Error) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (isSetup) {
            Logger.logI("Already setup SkyWayContext")
            return@withContext true
        }

        if(onErrorHandler != null) {
            SkyWayContext.onErrorHandler = onErrorHandler
        }

        Logger.logLevel = option.logLevel
        Logger.webRTCLog = option.webRTCLog

        WebRTCManager.setup(context, option.enableHardwareCodec)
        isSetup = nativeSetup(
            option.authToken,
            option.toJson(),
            WebRTCManager.nativePCFactory,
            HttpClient,
            WebSocketClientFactory,
            Logger
        )

        if (!isSetup) {
            onFatalError("Setup failed")
        }

        return@withContext isSetup
    }

    /**
     *  AuthTokenを更新します。
     *
     *  @param authToken 今後利用するトークン。
     */
    @JvmStatic
    fun updateAuthToken(authToken: String): Boolean {
        return nativeUpdateAuthToken(authToken)
    }

    /**
     *  RtcConfigを更新します。
     *  このAPIは内部向けのものであり、サポート対象外です
     *
     *  @param rtcConfig 更新後のRtcConfig。
     */
    @JvmStatic
    @SkyWayOptIn
    fun _updateRtcConfig(rtcConfig: RtcConfig) {
        nativeUpdateRtcConfig(rtcConfig.toJson())
    }

    /**
     *  [Plugin]を登録します。Botの利用に対応します。
     *
     *  @param plugin 利用したいプラグインのインスタンス。
     */
    @JvmStatic
    fun registerPlugin(plugin: Plugin) {
        plugins.add(plugin)
    }

    /**
     *  登録されている[Plugin]を探します。
     *
     *  @param name プラグイン名。
     *
     *  @return 見つかった場合は[Plugin]を、見つからなかった場合はnullを返します。
     */
    @JvmStatic
    fun findPlugin(name: String): Plugin? {
        return plugins.find { p -> p.name == name }
    }

    /**
     *  SkyWayの利用を終了します。
     *  以降全ての操作はできなくなり、各インスタンスからイベントが発火しなくなります。
     *  再度SkyWayを利用する場合は[setup]を実行してください。
     */
    @JvmStatic
    fun dispose() {
        if(!isSetup){
            Logger.logI("Already disposed SkyWayContext")
            return
        }
        CameraSource.stopCapturing()
        AudioSource.stop()
        nativeDispose()
        WebRTCManager.dispose()
        isSetup = false
    }

    @JvmStatic
    fun onReconnectStart() {
        onReconnectStartHandler?.invoke()
    }

    @JvmStatic
    fun onReconnectSuccess() {
        onReconnectSuccessHandler?.invoke()
    }

    @JvmStatic
    fun onFatalError(message: String) {
        isSetup = false
        onErrorHandler?.invoke(Error(message))
    }

    @JvmStatic
    fun onTokenRefreshingNeeded() {
        onTokenRefreshingNeededHandler?.invoke()
    }

    @JvmStatic
    fun onTokenExpired() {
        onTokenExpiredHandler?.invoke()
    }

    private external fun nativeSetup(
        authToken: String,
        optionsJson: String,
        pcFactory: Long,
        httpClient: HttpClient,
        webSocketClient: WebSocketClientFactory,
        logger: Logger
    ): Boolean

    private external fun nativeUpdateAuthToken(authToken: String): Boolean
    private external fun nativeUpdateRtcConfig(rtcConfig: String)
    private external fun nativeDispose()
}

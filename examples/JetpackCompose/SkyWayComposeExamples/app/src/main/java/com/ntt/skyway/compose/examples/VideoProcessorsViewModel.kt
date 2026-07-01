package com.ntt.skyway.compose.examples

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.Room
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.videoprocessors.BlurProcessingConfig
import com.ntt.skyway.videoprocessors.BlurProcessor
import com.ntt.skyway.videoprocessors.VirtualBackgroundProcessingConfig
import com.ntt.skyway.videoprocessors.VirtualBackgroundProcessor
import kotlinx.coroutines.launch
import java.util.UUID

enum class ProcessorMode(val displayName: String) {
    NONE("なし"),
    BLUR("背景ぼかし"),
    VIRTUAL_BACKGROUND("バーチャル背景")
}

class VideoProcessorsViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = this.javaClass.simpleName
    private val appContext = getApplication<Application>().applicationContext
    private var isSkyWayContextSetupDone = false
    private val appId = SkyWayCredentials.APP_ID
    private val secretKey = SkyWayCredentials.SECRET_KEY

    // ── UI State ──────────────────────────────────────────────────────────
    private val _joinedRoom = mutableStateOf(false)
    val joinedRoom: Boolean by _joinedRoom

    private val _localMemberName = mutableStateOf("")
    val localMemberName: String by _localMemberName

    private val _localVideoStream = mutableStateOf<LocalVideoStream?>(null)
    val localVideoStream: LocalVideoStream? by _localVideoStream

    private val _isPublishing = mutableStateOf(false)
    val isPublishing: Boolean by _isPublishing

    private val _processorMode = mutableStateOf(ProcessorMode.NONE)
    val processorMode: ProcessorMode by _processorMode

    private val _isProcessorActive = mutableStateOf(true)
    val isProcessorActive: Boolean by _isProcessorActive

    private val _blurStrength = mutableIntStateOf(60)
    val blurStrength: Int by _blurStrength

    private val _foregroundThreshold = mutableFloatStateOf(0.55f)
    val foregroundThreshold: Float by _foregroundThreshold

    private val _edgeSoftness = mutableFloatStateOf(0.08f)
    val edgeSoftness: Float by _edgeSoftness

    private val _mediaPipeSyncInputSize = mutableIntStateOf(256)
    val mediaPipeSyncInputSize: Int by _mediaPipeSyncInputSize

    private val _segmentationIntervalFrames = mutableIntStateOf(1)
    val segmentationIntervalFrames: Int by _segmentationIntervalFrames

    // ── Internal ──────────────────────────────────────────────────────────
    private var room: Room? = null
    private var localMember: LocalRoomMember? = null
    private var videoPublication: RoomPublication? = null

    private var blurProcessor: BlurProcessor? = null
    private var vbProcessor: VirtualBackgroundProcessor? = null

    // ── Room ──────────────────────────────────────────────────────────────

    /** SkyWay セットアップ → カメラ起動 → ルーム入室 */
    fun joinRoom(roomName: String, memberName: String) {
        viewModelScope.launch {
            setupSkyWayContextIfNecessary()
            if (!isSkyWayContextSetupDone) {
                Toast.makeText(appContext, "SkyWay setup failed", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val r = Room.findOrCreate(roomName) ?: run {
                Toast.makeText(appContext, "Room findOrCreate failed", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val init = RoomMember.Init(memberName.ifBlank { UUID.randomUUID().toString() })
            val member = r.join(init) ?: run {
                Toast.makeText(appContext, "Join failed", Toast.LENGTH_SHORT).show()
                r.dispose()
                return@launch
            }

            room = r
            localMember = member

            startCamera()

            _localMemberName.value = member.name ?: member.id
            _joinedRoom.value = true

            setupRoomHandlers(r)
            Log.d(tag, "Joined room: ${r.id}")
        }
    }

    /** カメラ映像（プロセッサ適用済み）を Publish します。 */
    fun publishVideo() {
        val stream = _localVideoStream.value ?: return
        viewModelScope.launch {
            videoPublication = localMember?.publish(stream)
            _isPublishing.value = (videoPublication != null)
            Log.d(tag, "Publish result: ${videoPublication?.id}")
        }
    }

    /** Publish を停止します。 */
    fun unpublishVideo() {
        val pub = videoPublication ?: return
        viewModelScope.launch {
            localMember?.unpublish(pub)
            videoPublication = null
            _isPublishing.value = false
        }
    }

    /** ルームから退出します。 */
    fun leaveRoom() {
        viewModelScope.launch {
            localMember?.let { room?.leave(it) }
            room?.dispose()
            room = null
            localMember = null
            videoPublication = null
            _joinedRoom.value = false
            _isPublishing.value = false
            disposeProcessors()
            CameraSource.stopCapturing()
            _localVideoStream.value = null
        }
    }

    // ── Processor ─────────────────────────────────────────────────────────

    /** プロセッサモードを切り替えます。 */
    fun setProcessorMode(mode: ProcessorMode) {
        blurProcessor?.let { CameraSource.removeVideoProcessor(it) }
        vbProcessor?.let { CameraSource.removeVideoProcessor(it) }

        _processorMode.value = mode

        when (mode) {
            ProcessorMode.NONE -> {}
            ProcessorMode.BLUR -> {
                if (blurProcessor == null) {
                    blurProcessor = BlurProcessor(
                        config = BlurProcessingConfig(
                            blurStrength = blurStrength,
                            isActive = isProcessorActive,
                            foregroundThreshold = foregroundThreshold,
                            edgeSoftness = edgeSoftness,
                            mediaPipeSyncInputSize = mediaPipeSyncInputSize,
                            segmentationIntervalFrames = segmentationIntervalFrames
                        ),
                        context = appContext
                    )
                }
                CameraSource.addVideoProcessor(blurProcessor!!)
            }
            ProcessorMode.VIRTUAL_BACKGROUND -> {
                if (vbProcessor == null) {
                    vbProcessor = VirtualBackgroundProcessor(
                        config = VirtualBackgroundProcessingConfig(
                            isActive = isProcessorActive,
                            foregroundThreshold = foregroundThreshold,
                            edgeSoftness = edgeSoftness,
                            mediaPipeSyncInputSize = mediaPipeSyncInputSize,
                            segmentationIntervalFrames = segmentationIntervalFrames
                        ),
                        context = appContext
                    )
                }
                CameraSource.addVideoProcessor(vbProcessor!!)
            }
        }
    }

    /** プロセッサの有効/無効を切り替えます。 */
    fun setProcessorActive(active: Boolean) {
        _isProcessorActive.value = active
        blurProcessor?.isActive = active
        vbProcessor?.isActive = active
    }

    /** ぼかし強度を設定します（0〜100）。 */
    fun setBlurStrength(strength: Int) {
        _blurStrength.intValue = strength.coerceIn(0, 100)
        blurProcessor?.blurStrength = blurStrength
    }

    /** バーチャル背景の背景画像を設定します。`null` で置き換えなし。 */
    fun setVirtualBackground(bitmap: Bitmap?) {
        vbProcessor?.backgroundImage = bitmap
    }

    /** 前景判定のしきい値を設定します（0f〜1f）。大きくすると前景をより保持。 */
    fun setForegroundThreshold(value: Float) {
        _foregroundThreshold.floatValue = value.coerceIn(0f, 1f)
        blurProcessor?.foregroundThreshold = _foregroundThreshold.floatValue
        vbProcessor?.foregroundThreshold = _foregroundThreshold.floatValue
    }

    /** 前景と背景境界の滑らかさを設定します（0f〜1f）。 */
    fun setEdgeSoftness(value: Float) {
        _edgeSoftness.floatValue = value.coerceIn(0f, 1f)
        blurProcessor?.edgeSoftness = _edgeSoftness.floatValue
        vbProcessor?.edgeSoftness = _edgeSoftness.floatValue
    }

    /**
     * MediaPipe 推論の入力解像度を設定します（192 / 256 / 320 px）。
     *
     * config のみで変更可能なため、アクティブなプロセッサを再作成します。
     */
    fun setMediaPipeSyncInputSize(size: Int) {
        if (_mediaPipeSyncInputSize.intValue == size) return
        _mediaPipeSyncInputSize.intValue = size
        // 再作成が必要なので既存プロセッサを破棄してから再適用
        val currentMode = processorMode
        if (currentMode == ProcessorMode.BLUR) {
            blurProcessor?.let { CameraSource.removeVideoProcessor(it); it.dispose() }
            blurProcessor = null
        } else if (currentMode == ProcessorMode.VIRTUAL_BACKGROUND) {
            vbProcessor?.let { CameraSource.removeVideoProcessor(it); it.dispose() }
            vbProcessor = null
        }
        setProcessorMode(currentMode)
    }

    /** セグメンテーション実行フレーム間隔を設定します（1〜30）。 */
    fun setSegmentationIntervalFrames(frames: Int) {
        _segmentationIntervalFrames.intValue = frames.coerceIn(1, 30)
        blurProcessor?.segmentationIntervalFrames = _segmentationIntervalFrames.intValue
        vbProcessor?.segmentationIntervalFrames = _segmentationIntervalFrames.intValue
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private fun startCamera() {
        val cameras = CameraSource.getFrontCameras(appContext)
        if (cameras.isEmpty()) {
            Log.w(tag, "フロントカメラが見つかりません")
            return
        }
        CameraSource.startCapturing(
            appContext,
            cameras.first(),
            CameraSource.CapturingOptions(800, 800)
        )
        _localVideoStream.value = CameraSource.createStream()
    }

    private fun setupRoomHandlers(r: Room) {
        r.onStreamPublishedHandler = { publication ->
            Log.d(tag, "onStreamPublished: ${publication.id}")
        }
    }

    private fun disposeProcessors() {
        blurProcessor?.let {
            CameraSource.removeVideoProcessor(it)
            it.dispose()
        }
        vbProcessor?.let {
            CameraSource.removeVideoProcessor(it)
            it.dispose()
        }
        blurProcessor = null
        vbProcessor = null
        _processorMode.value = ProcessorMode.NONE
    }

    private suspend fun setupSkyWayContextIfNecessary() {
        if (isSkyWayContextSetupDone) return
        val result = SkyWayContext.setupForDev(
            context = appContext,
            appId = appId,
            secretKey = secretKey,
            option = SkyWayContext.Options(logLevel = Logger.LogLevel.VERBOSE)
        )
        if (result) {
            isSkyWayContextSetupDone = true
            Log.d(tag, "SkyWay Setup succeed")
        }
    }

    override fun onCleared() {
        // Avoid launching cleanup coroutines in viewModelScope here because the scope is cancelled when the ViewModel is cleared.
        disposeProcessors()
        CameraSource.stopCapturing()
        room?.dispose()
        super.onCleared()
    }
}

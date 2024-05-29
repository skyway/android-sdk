/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.sink

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.WebRTCManager
import com.ntt.skyway.core.util.Logger
import org.webrtc.RendererCommon

/**
 *  SurfaceViewへの映像出力に関する操作を行うクラス。
 */
class SurfaceViewRenderer : FrameLayout, Renderer {
    /**
     *  描画方法の設定。
     */
    enum class ScalingType {
        SCALE_ASPECT_FIT, SCALE_ASPECT_FILL, SCALE_ASPECT_BALANCED;

        companion object {
            internal fun toWebRTC(scalingType: ScalingType): RendererCommon.ScalingType {
                return when (scalingType) {
                    SCALE_ASPECT_FIT -> RendererCommon.ScalingType.SCALE_ASPECT_FIT
                    SCALE_ASPECT_FILL -> RendererCommon.ScalingType.SCALE_ASPECT_FILL
                    SCALE_ASPECT_BALANCED -> RendererCommon.ScalingType.SCALE_ASPECT_BALANCED
                }
            }
        }
    }

    /**
     *  UIの配置に関する設定。
     */
    enum class LayoutParam(val value: Int) {
        FILL_PARENT(-1), MATCH_PARENT(-1), WRAP_CONTENT(-2)
    }

    override var isSetup: Boolean = false
    override var sink: org.webrtc.SurfaceViewRenderer? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    /**
     *  初期化します。
     */
    fun setup(width: LayoutParam = LayoutParam.WRAP_CONTENT, height: LayoutParam = LayoutParam.WRAP_CONTENT) {
        if (isSetup) {
            Logger.logI("Already setup SurfaceViewRenderer")
            return
        }
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        sink = org.webrtc.SurfaceViewRenderer(context)
        sink?.init(WebRTCManager.eglBaseContext, null)
        addRendererToView(width, height)
        isSetup = true
    }

    /**
     *  描画方法を変更します。
     */
    fun setScalingType(scalingType: ScalingType) {
        sink?.setScalingType(ScalingType.toWebRTC(scalingType))
    }

    override fun dispose() {
        sink?.let {
            it.release()
            removeView(it)
        }
    }

    private fun addRendererToView(width: LayoutParam, height: LayoutParam) {
        sink?.let {
            val params = LayoutParams(width.value, height.value)
            params.gravity = Gravity.CENTER
            addView(it, params)
            it.requestLayout()
        }
    }
}

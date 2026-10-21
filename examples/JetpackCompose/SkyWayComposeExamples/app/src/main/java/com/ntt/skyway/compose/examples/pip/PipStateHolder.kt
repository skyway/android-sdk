package com.ntt.skyway.compose.examples.pip

import kotlinx.coroutines.flow.MutableStateFlow


enum class PipVideoTarget { LOCAL, REMOTE }

/**
 * PiP サンプル専用のグローバル状態ホルダー。
 *
 * MainActivity・[PipViewModel]・[PipScreen] の間で PiP 関連の状態を共有するためのブリッジです。
 *
 * - [isCallActive]: 通話中かどうか。[PipViewModel] が更新し、MainActivity が監視して
 *   `BasicPictureInPicture.setEnabled()` を呼び出します。
 * - [isPipMode]: PiP ウィンドウ表示中かどうか。MainActivity が更新し、[PipScreen] が監視して
 *   ミニマル UI に切り替えます。
 * - [pipVideoTarget]: PiP ウィンドウに表示する映像ソース（ローカル or リモート）。
 *   [PipScreen] が更新し、PiP 表示中に正しい映像を描画するために参照します。
 */
object PipStateHolder {
    val isCallActive = MutableStateFlow(false)
    val isPipMode = MutableStateFlow(false)
    val pipVideoTarget = MutableStateFlow(PipVideoTarget.LOCAL)
}

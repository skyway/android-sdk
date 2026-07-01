package com.ntt.skyway.compose.examples

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer

/** プリセット背景カラーの定義 */
private val backgroundPresets = listOf(
    "ブルー" to Color.rgb(30, 100, 200),
    "グリーン" to Color.rgb(30, 160, 80),
    "マゼンタ" to Color.rgb(180, 30, 180),
)

/**
 * 背景ぼかし・バーチャル背景のデモ画面。
 *
 * 入室前は Room 名・メンバー名の入力フォームを表示し、
 * 入室後はカメラプレビュー・映像配信・プロセッサ設定を表示します。
 */
@Composable
fun VideoProcessorsScreen(
    viewModel: VideoProcessorsViewModel,
    navController: NavController,
) {
    if (viewModel.joinedRoom) {
        RoomScreen(viewModel = viewModel, navController = navController)
    } else {
        JoinScreen(viewModel = viewModel, navController = navController)
    }
}

// ── 入室前：Room 名 / メンバー名 入力 ──────────────────────────────────────

@Composable
private fun JoinScreen(
    viewModel: VideoProcessorsViewModel,
    navController: NavController,
) {
    var roomName by remember { mutableStateOf("") }
    var memberName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Video Processors Sample",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            label = { Text("Room Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = memberName,
            onValueChange = { memberName = it },
            label = { Text("Member Name (省略可)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.joinRoom(roomName, memberName) },
            enabled = roomName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("入室")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("戻る")
        }
    }
}

// ── 入室後：カメラプレビュー + 映像配信 + プロセッサ設定 ─────────────────────

@Composable
private fun RoomScreen(
    viewModel: VideoProcessorsViewModel,
    navController: NavController,
) {
    val localVideoStream = viewModel.localVideoStream

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── メンバー情報 ─────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Member: ", style = MaterialTheme.typography.bodyMedium)
            Text(viewModel.localMemberName, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── カメラプレビュー（ローカル・フルワイド） ──────────────────────
        Text("自分", style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            factory = { context ->
                SurfaceViewRenderer(context).apply {
                    setup()
                    localVideoStream?.addRenderer(this)
                }
            },
            update = { renderer ->
                localVideoStream?.removeRenderer(renderer)
                localVideoStream?.addRenderer(renderer)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        // ── 映像配信ボタン ────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.publishVideo() },
                enabled = !viewModel.isPublishing,
                modifier = Modifier.weight(1f)
            ) {
                Text("配信開始")
            }
            OutlinedButton(
                onClick = { viewModel.unpublishVideo() },
                enabled = viewModel.isPublishing,
                modifier = Modifier.weight(1f)
            ) {
                Text("配信停止")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        // ── プロセッサ選択 ─────────────────────────────────────────────────
        Text("プロセッサ", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ProcessorMode.entries.forEach { mode ->
                val isSelected = viewModel.processorMode == mode
                Button(
                    onClick = { viewModel.setProcessorMode(mode) },
                    colors = if (isSelected) ButtonDefaults.buttonColors()
                    else ButtonDefaults.outlinedButtonColors(),
                    modifier = if (isSelected) Modifier
                    else Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.small
                    )
                ) {
                    Text(mode.displayName, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── プロセッサ設定コントロール ─────────────────────────────────────
        when (viewModel.processorMode) {
            ProcessorMode.BLUR -> BlurControls(viewModel)
            ProcessorMode.VIRTUAL_BACKGROUND -> VirtualBackgroundControls(viewModel)
            ProcessorMode.NONE -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 退室ボタン ────────────────────────────────────────────────────
        Button(
            onClick = {
                viewModel.leaveRoom()
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("退室")
        }
    }
}

// ── 背景ぼかしコントロール ─────────────────────────────────────────────────

@Composable
private fun BlurControls(viewModel: VideoProcessorsViewModel) {
    var blurStrength by remember { mutableFloatStateOf(viewModel.blurStrength.toFloat()) }
    var isActive by remember { mutableStateOf(viewModel.isProcessorActive) }

    // 有効/無効
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("有効", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = isActive, onCheckedChange = { isActive = it; viewModel.setProcessorActive(it) })
    }
    Spacer(modifier = Modifier.height(4.dp))

    // ぼかし強度
    Text("ぼかし強度: ${blurStrength.toInt()}", style = MaterialTheme.typography.bodySmall)
    Slider(
        value = blurStrength,
        onValueChange = { blurStrength = it; viewModel.setBlurStrength(it.toInt()) },
        valueRange = 0f..100f,
        modifier = Modifier.fillMaxWidth()
    )

    // 共通セグメンテーション設定
    SegmentationControls(viewModel)
}

// ── バーチャル背景コントロール ─────────────────────────────────────────────

@Composable
private fun VirtualBackgroundControls(viewModel: VideoProcessorsViewModel) {
    var isActive by remember { mutableStateOf(viewModel.isProcessorActive) }

    // 有効/無効
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("有効", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = isActive, onCheckedChange = { isActive = it; viewModel.setProcessorActive(it) })
    }
    Spacer(modifier = Modifier.height(8.dp))

    // 背景カラー選択
    Text("背景カラー", style = MaterialTheme.typography.bodySmall)
    Spacer(modifier = Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ColorSwatch(label = "なし", color = null) { viewModel.setVirtualBackground(null) }
        backgroundPresets.forEach { (label, argb) ->
            ColorSwatch(label = label, color = argb) {
                val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(argb)
                viewModel.setVirtualBackground(bitmap)
            }
        }
    }

    // 共通セグメンテーション設定
    SegmentationControls(viewModel)
}

// ── 共通セグメンテーション設定（両プロセッサ共通） ─────────────────────────

@Composable
private fun SegmentationControls(viewModel: VideoProcessorsViewModel) {
    var foregroundThreshold by remember { mutableFloatStateOf(viewModel.foregroundThreshold) }
    var edgeSoftness by remember { mutableFloatStateOf(viewModel.edgeSoftness) }
    var intervalFrames by remember { mutableIntStateOf(viewModel.segmentationIntervalFrames) }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(8.dp))

    // foregroundThreshold
    Text(
        "前景しきい値: ${"%.2f".format(foregroundThreshold)}",
        style = MaterialTheme.typography.bodySmall
    )
    Slider(
        value = foregroundThreshold,
        onValueChange = { foregroundThreshold = it; viewModel.setForegroundThreshold(it) },
        valueRange = 0f..1f,
        modifier = Modifier.fillMaxWidth()
    )

    // edgeSoftness
    Text(
        "境界の滑らかさ: ${"%.2f".format(edgeSoftness)}",
        style = MaterialTheme.typography.bodySmall
    )
    Slider(
        value = edgeSoftness,
        onValueChange = { edgeSoftness = it; viewModel.setEdgeSoftness(it) },
        valueRange = 0f..1f,
        modifier = Modifier.fillMaxWidth()
    )

    // segmentationIntervalFrames
    Text(
        "セグメンテーション間隔: ${intervalFrames} フレームごと",
        style = MaterialTheme.typography.bodySmall
    )
    Slider(
        value = intervalFrames.toFloat(),
        onValueChange = {
            intervalFrames = it.toInt()
            viewModel.setSegmentationIntervalFrames(intervalFrames)
        },
        valueRange = 1f..30f,
        steps = 28,
        modifier = Modifier.fillMaxWidth()
    )

    // mediaPipeSyncInputSize
    Spacer(modifier = Modifier.height(4.dp))
    Text("推論入力サイズ (px)", style = MaterialTheme.typography.bodySmall)
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(192, 256, 320).forEach { size ->
            val isSelected = viewModel.mediaPipeSyncInputSize == size
            Button(
                onClick = { viewModel.setMediaPipeSyncInputSize(size) },
                colors = if (isSelected) ButtonDefaults.buttonColors()
                else ButtonDefaults.outlinedButtonColors(),
                modifier = if (isSelected) Modifier
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
            ) {
                Text("$size", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}



// ── 背景カラー選択スウォッチ ───────────────────────────────────────────────

@Composable
private fun ColorSwatch(label: String, color: Int?, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (color != null) {
                        androidx.compose.ui.graphics.Color(color)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(6.dp)
                )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

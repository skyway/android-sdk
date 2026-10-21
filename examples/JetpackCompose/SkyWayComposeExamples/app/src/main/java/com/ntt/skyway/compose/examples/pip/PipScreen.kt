package com.ntt.skyway.compose.examples.pip

import android.app.PictureInPictureParams
import android.content.Context
import android.os.Build
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.ntt.skyway.core.content.VideoStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer

/**
 * PiP（Picture-in-Picture）デモ画面。
 *
 * 入室前は Room 名と Member 名の入力フォームを表示します。
 * 入室後は自分のカメラ映像と相手映像、手動での PiP 切り替えボタンを表示します。
 * PiP ウィンドウの表示中は [PipFullScreenVideo] のみを描画します。
 */
@Composable
fun PipScreen(
    viewModel: PipViewModel,
    navController: NavController,
) {
    val isPipMode by PipStateHolder.isPipMode.collectAsState()
    val pipTarget by PipStateHolder.pipVideoTarget.collectAsState()

    if (isPipMode) {
        PipFullScreenVideo(
            target = pipTarget,
            localVideoStream = viewModel.localVideoStream,
            remoteVideoStream = viewModel.remoteVideoStream,
        )
        return
    }

    if (viewModel.joinedRoom) {
        PipRoomScreen(viewModel = viewModel)
    } else {
        PipJoinScreen(viewModel = viewModel, navController = navController)
    }
}

// 入室前：Room 名 / Member 名の入力

@Composable
private fun PipJoinScreen(
    viewModel: PipViewModel,
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
        Text("PiP Sample", style = MaterialTheme.typography.headlineSmall)
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

// 入室後：自分のカメラ映像 / 相手映像 / 手動での PiP 切り替えボタン

@Composable
private fun PipRoomScreen(viewModel: PipViewModel) {
    val context = LocalContext.current
    val localVideoStream = viewModel.localVideoStream
    val remoteVideoStream = viewModel.remoteVideoStream

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Member: ${viewModel.localMemberName}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(12.dp))

        Text("自分", style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            factory = { ctx ->
                SurfaceViewRenderer(ctx).apply {
                    setup()
                    localVideoStream?.addRenderer(this)
                }
            },
            update = { renderer ->
                localVideoStream?.removeRenderer(renderer)
                localVideoStream?.addRenderer(renderer)
            },
            onRelease = { renderer ->
                localVideoStream?.removeRenderer(renderer)
                renderer.dispose()
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                PipStateHolder.pipVideoTarget.value = PipVideoTarget.LOCAL
                enterPip(context)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("PiP (自分)")
        }

        if (remoteVideoStream != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("相手", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        setup()
                        remoteVideoStream.addRenderer(this)
                    }
                },
                update = { renderer ->
                    remoteVideoStream.removeRenderer(renderer)
                    remoteVideoStream.addRenderer(renderer)
                },
                onRelease = { renderer ->
                    remoteVideoStream.removeRenderer(renderer)
                    renderer.dispose()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    PipStateHolder.pipVideoTarget.value = PipVideoTarget.REMOTE
                    enterPip(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("PiP (相手)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.leaveRoom() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("退室")
        }
    }
}

// PiP ウィンドウ表示中

@Composable
private fun PipFullScreenVideo(
    target: PipVideoTarget,
    localVideoStream: LocalVideoStream?,
    remoteVideoStream: RemoteVideoStream?,
) {
    // LocalVideoStream/RemoteVideoStream はどちらも VideoStream（addRenderer/removeRenderer を持つ）を実装している
    val stream: VideoStream? = when (target) {
        PipVideoTarget.LOCAL -> localVideoStream
        PipVideoTarget.REMOTE -> remoteVideoStream
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                SurfaceViewRenderer(ctx).apply {
                    setup()
                    stream?.addRenderer(this)
                }
            },
            update = { renderer ->
                stream?.removeRenderer(renderer)
                stream?.addRenderer(renderer)
            },
            onRelease = { renderer ->
                stream?.removeRenderer(renderer)
                renderer.dispose()
            }
        )
    }
}

/**
 * PiP モードに手動で入ります。
 *
 * ホームボタンを押した時の自動 PiP 開始（`BasicPictureInPicture.setEnabled()`）とは別に、
 * ボタンのタップによる明示的な PiP 開始として直接 Activity API を呼びます。
 *
 * アスペクト比は横向き映像を想定して `16:9` を指定しています。
 */
private fun enterPip(context: Context) {
    // enterPictureInPictureMode() は Android 8（API 26）以降のみ利用可能
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Activity 以外の Context（Application など）の場合は PiP に入れないため安全にキャスト
        val activity = context as? ComponentActivity ?: return
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()
        activity.enterPictureInPictureMode(params)
    }
}

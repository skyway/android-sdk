package com.ntt.skyway.example.quickstart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer
import java.util.UUID

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    modifier: Modifier
) {
    var roomName by remember { mutableStateOf(UUID.randomUUID().toString()) }

    val localVideoStream = mainViewModel.localVideoStream
    val remoteVideoStream = mainViewModel.remoteVideoStream

    var localRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    var remoteRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
            if (localVideoStream != null) {
                AndroidView(
                    modifier = Modifier
                        .width(150.dp)
                        .height(150.dp),
                    factory = { context ->
                        localRenderView = SurfaceViewRenderer(context)
                        localRenderView!!.apply {
                            setup()
                            localVideoStream.addRenderer(this)
                        }
                    },
                    update = {
                        localVideoStream.removeRenderer(localRenderView!!)
                        localVideoStream.addRenderer(localRenderView!!)
                    }
                )
            }
            if (remoteVideoStream != null) {
                AndroidView(
                    modifier = Modifier
                        .width(150.dp)
                        .height(150.dp),
                    factory = { context ->
                        remoteRenderView = SurfaceViewRenderer(context)
                        remoteRenderView!!.apply {
                            setup()
                            remoteVideoStream.addRenderer(this)
                        }
                    },
                    update = {
                        remoteVideoStream.removeRenderer(remoteRenderView!!)
                        remoteVideoStream.addRenderer(remoteRenderView!!)
                    }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("room:")
            TextField(
                value = roomName,
                onValueChange = { newText ->
                    roomName = newText
                },
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    mainViewModel.joinAndPublish(roomName)
                }
            ) {
                Text("Join")
            }
        }
    }
}

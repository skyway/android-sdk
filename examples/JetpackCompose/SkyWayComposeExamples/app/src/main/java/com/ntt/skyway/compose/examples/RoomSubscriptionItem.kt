package com.ntt.skyway.compose.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer
import com.ntt.skyway.room.RoomSubscription

@Composable
fun RoomSubscriptionItem(
    roomSubscription: RoomSubscription
) {
    var remoteRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    val remoteVideoStream by remember { mutableStateOf(roomSubscription.stream as RemoteVideoStream?) }

    Surface(
        modifier = Modifier
            .wrapContentSize(),
        shadowElevation = 4.dp // elevation効果を追加
    ) {
        Column(
            modifier = Modifier.width(30.dp)
        ) {
            AndroidView(
                modifier = Modifier.size(30.dp),
                factory = { context ->
                    remoteRenderView = SurfaceViewRenderer(context)
                    remoteRenderView!!.apply {
                        setup()
                        remoteVideoStream?.addRenderer(this)
                    }
                },
                update = {
                    remoteVideoStream?.removeRenderer(remoteRenderView!!)
                    remoteVideoStream?.addRenderer(remoteRenderView!!)
                }
            )
            Text(
                text = roomSubscription.publication.publisher?.name ?: "Unknown",
                fontSize = 6.sp,
                maxLines = 1,
                lineHeight = 6.sp,
                overflow = TextOverflow.Ellipsis
            )
        }
    }



}
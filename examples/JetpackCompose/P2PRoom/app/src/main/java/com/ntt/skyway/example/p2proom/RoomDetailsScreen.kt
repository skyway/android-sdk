package com.ntt.skyway.example.p2proom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer

@Composable
fun RoomDetailsScreen(
    viewModel: MainViewModel,
    navController: NavController,
) {
    val localVideoStream = viewModel.localVideoStream
    val remoteVideoStream = viewModel.remoteVideoStream
    val memberList = viewModel.roomMembers
    val publicationList = viewModel.roomPublications
    var localRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    var remoteRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    LaunchedEffect(viewModel.joinedRoom) {
        if (!viewModel.joinedRoom) {
            navController.navigate("main")
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Member Name: ")
            Text(text = viewModel.localMemberName)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            Button(onClick = {
                viewModel.publishCameraVideoStream()
            }, modifier = Modifier.width(107.dp)) {
                Text(text = "Publish Video")
            }
            Button(onClick = {
                viewModel.publishAudioStream()
            }, modifier = Modifier.width(107.dp)) {
                Text(text = "Publish Audio")
            }
            Button(onClick = {
                viewModel.publishDataStream()
            }, modifier = Modifier.width(107.dp)) {
                Text(text = "Publish Data")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            var textData by remember { mutableStateOf("") }
            TextField(
                value = textData,
                onValueChange = { textData = it },
                placeholder = { Text("data pub message") },
                modifier = Modifier
                    .weight(1f)
                    .height(IntrinsicSize.Min)
            )
            Button(onClick = {
                viewModel.sendData(textData)
            }) {
                Text(text = "Send")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.leaveRoom()
                }
            )
            {
                Text(text = "Leave")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            AndroidView(
                modifier = Modifier.size(150.dp),
                factory = { context ->
                    localRenderView = SurfaceViewRenderer(context)
                    localRenderView!!.apply {
                        setup()
                        localVideoStream?.addRenderer(this)
                    }
                },
                update = {
                    localVideoStream?.removeRenderer(localRenderView!!)
                    localVideoStream?.addRenderer(localRenderView!!)
                }
            )
            Spacer(modifier = Modifier.width(20.dp))
            AndroidView(
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp),
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
        }

        // Members and Publications lists with RecyclerViews
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = "Members",
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                LazyColumn {
                    items(memberList) { member ->
                        Column {
                            Text(
                                text = member.name ?: member.id,
                                fontSize = 8.sp,
                                style = TextStyle(lineHeight = 10.sp)
                            )
                            HorizontalDivider(
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Publications",
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                LazyColumn {
                    items(publicationList) { publication ->
                        if (publication.publisher?.id == viewModel.localPerson?.id) {
                            RoomPublicationItem(
                                showUnPublishButton = true,
                                showSubscribeButton = false,
                                showUnsubscribeButton = false,
                                roomPublication = publication,
                                viewModel = viewModel
                            )
                        } else {
                            RoomPublicationItem(
                                showSubscribeButton = true,
                                showUnsubscribeButton = false,
                                showUnPublishButton = false,
                                roomPublication = publication,
                                viewModel = viewModel
                            )
                        }

                    }
                }
            }
        }
    }
}

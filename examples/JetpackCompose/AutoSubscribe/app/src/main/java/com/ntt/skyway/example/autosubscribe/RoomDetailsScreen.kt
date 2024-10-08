package com.ntt.skyway.example.sfuroom

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.ntt.skyway.example.autosubscribe.RoomSubscriptionItem

@Composable
fun RoomDetailsScreen(
    viewModel: MainViewModel,
    navController: NavController,
) {
    val localVideoStream = viewModel.localVideoStream
    val remoteVideoStream = viewModel.remoteVideoStream
    val memberList = viewModel.roomMembers
    val publicationList = viewModel.roomPublications
    var subscriptions = viewModel.roomSubscriptions
    var localRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }


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
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AndroidView(
                modifier = Modifier.size(40.dp),
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
                .height(220.dp)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(subscriptions) { subscription ->
                    RoomSubscriptionItem(
                        roomSubscription = subscription
                    )
                }
            }
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

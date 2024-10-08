package com.ntt.skyway.example.sfuroom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ntt.skyway.room.RoomPublication


@Composable
fun RoomPublicationItem(
    showUnPublishButton: Boolean = false,
    showSubscribeButton: Boolean = true,
    showUnsubscribeButton: Boolean = false,
    roomPublication: RoomPublication,
    viewModel: MainViewModel,

    ) {
    val showUnPublishBtn by remember { mutableStateOf(showUnPublishButton) }
    var showSubscribeBtn by remember { mutableStateOf(showSubscribeButton) }
    var showUnsubscribeBtn by remember { mutableStateOf(showUnsubscribeButton) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(5.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "publisher: ",
                    fontSize = 8.sp
                )
                Text(
                    text = roomPublication.publisher?.name ?: "Unknown",
                    fontSize = 8.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "type: ",
                    fontSize = 8.sp
                )
                Text(
                    text = roomPublication.contentType.toString(),
                    fontSize = 8.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showSubscribeBtn) {
                    Button(
                        onClick = {
                            viewModel.subscribe(roomPublication)
                            showSubscribeBtn = false
                            showUnsubscribeBtn = true
                        },
                        modifier = Modifier.padding(start = 5.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(text = "Sub", fontSize = 8.sp)
                    }
                }
                if (showUnsubscribeBtn) {
                    Button(
                        onClick = {
                            viewModel.unsubscribe()
                            showSubscribeBtn = true
                            showUnsubscribeBtn = false
                        },
                        modifier = Modifier.padding(start = 5.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(text = "UnSub", fontSize = 8.sp)
                    }
                }
                if (showUnPublishBtn) {
                    Button(
                        onClick = {
                            viewModel.unPublish(roomPublication)
                        },
                        modifier = Modifier.padding(start = 5.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(text = "UnPub", fontSize = 8.sp)
                    }
                }

            }
        }
    }
}
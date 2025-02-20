package com.ntt.skyway.compose.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ntt.skyway.room.Room

@Composable
fun SelectRoomScreen(
    navController: NavController,
    mainViewModel: MainViewModel
) {

    var roomName by remember { mutableStateOf("") }
    var memberName by remember { mutableStateOf("") }

    LaunchedEffect(mainViewModel.joinedRoom ) {
        if (mainViewModel.joinedRoom) {
            if (mainViewModel.sampleType == SampleType.AUTO_SUBSCRIBE) {
                navController.navigate("auto_sub_room_details/${roomName}/${memberName}")
            } else {
                navController.navigate("room_details/${roomName}/${memberName}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            value = roomName,
            onValueChange = { roomName = it },
            label = { Text("Room Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = memberName,
            onValueChange = { memberName = it },
            label = { Text("Member Name") },
            modifier = Modifier.fillMaxWidth()
        )
        if (mainViewModel.sampleType != SampleType.SFU_ROOM) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    mainViewModel.joinRoom(roomName, memberName, Room.Type.P2P)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join P2P Room")
            }
        }
        if (mainViewModel.sampleType != SampleType.P2P_ROOM) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    mainViewModel.joinRoom(roomName, memberName, Room.Type.SFU)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join SFU Room")
            }
        }
    }
}
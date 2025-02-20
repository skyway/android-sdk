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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MainScreen(
    navController: NavController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Button(
            onClick = {
                navController.navigate("p2p_room_sample")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Launch P2P Room Sample")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                navController.navigate("sfu_room_sample")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Launch SFU Room Sample")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                navController.navigate("auto_subscribe_sample")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Launch Auto Subscribe Sample")
        }
    }
}

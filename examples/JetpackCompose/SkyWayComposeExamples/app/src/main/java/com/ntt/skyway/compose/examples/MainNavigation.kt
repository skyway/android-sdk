package com.ntt.skyway.compose.examples

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainNavigation(
    mainViewModel: MainViewModel,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            mainViewModel.sampleType = SampleType.NULL
            MainScreen(
                navController = navController
            )
        }
        composable("p2p_room_sample") {
            SelectRoomScreen(
                navController = navController,
                mainViewModel = mainViewModel.apply {
                    sampleType = SampleType.P2P_ROOM
                }
            )
        }
        composable("sfu_room_sample") {
            SelectRoomScreen(
                navController = navController,
                mainViewModel = mainViewModel.apply {
                    sampleType = SampleType.SFU_ROOM
                }
            )
        }
        composable("auto_subscribe_sample") {
            SelectRoomScreen(
                navController = navController,
                mainViewModel = mainViewModel.apply {
                    sampleType = SampleType.AUTO_SUBSCRIBE
                }
            )
        }
        composable("room_details/{roomName}/{memberName}") { backStackEntry ->
            val roomName = backStackEntry.arguments?.getString("roomName") ?: ""
            val memberName = backStackEntry.arguments?.getString("memberName") ?: ""
            CommonRoomDetailsScreen(
                viewModel = mainViewModel,
                navController = navController,
            )
        }
        composable("auto_sub_room_details/{roomName}/{memberName}") { backStackEntry ->
            val roomName = backStackEntry.arguments?.getString("roomName") ?: ""
            val memberName = backStackEntry.arguments?.getString("memberName") ?: ""
            AutoSubRoomDetailsScreen(
                viewModel = mainViewModel,
                navController = navController,
            )
        }
    }
}
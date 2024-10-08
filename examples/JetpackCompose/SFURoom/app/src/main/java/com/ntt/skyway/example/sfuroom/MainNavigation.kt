package com.ntt.skyway.example.sfuroom

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
            MainScreen(
                navController = navController,
                mainViewModel = mainViewModel
            )
        }
        composable("roomDetails/{roomName}/{memberName}") { backStackEntry ->
            val roomName = backStackEntry.arguments?.getString("roomName") ?: ""
            val memberName = backStackEntry.arguments?.getString("memberName") ?: ""
            SFURoomDetailsScreen(
                viewModel = mainViewModel,
                navController = navController,
            )
        }
    }
}
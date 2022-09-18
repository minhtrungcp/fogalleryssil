package com.example.fogalleryssil.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fogalleryssil.presentation.screen.GalleryScreen


@Composable
fun NavHostController(
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ScreenName.GalleryScreen.route
    ) {
        composable(
            route = ScreenName.GalleryScreen.route
        ) {
            GalleryScreen(navController = navController)
        }
        composable(
            route = ScreenName.ViewImageScreen.route
        ) {

        }
        composable(
            route = ScreenName.PlayVideoScreen.route
        ) {

        }
    }
}
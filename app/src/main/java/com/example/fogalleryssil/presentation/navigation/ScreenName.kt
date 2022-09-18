package com.example.fogalleryssil.presentation.navigation

sealed class ScreenName(val route: String){
    object GalleryScreen: ScreenName("gallery_screen")
    object ViewImageScreen: ScreenName("view_image_screen")
    object PlayVideoScreen: ScreenName("play_video_screen")
}
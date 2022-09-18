package com.example.fogalleryssil.presentation.screen.model

import android.net.Uri

data class GalleryModel(
    var isVideo: Boolean = false,
    var path: Uri,
    var isFavorite: Boolean = false
)

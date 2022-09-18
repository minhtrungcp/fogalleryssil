package com.example.fogalleryssil.presentation.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.ComponentRegistry
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import coil.size.Size
import com.example.fogalleryssil.R
import com.example.fogalleryssil.presentation.screen.model.GalleryModel
import com.example.fogalleryssil.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GalleryScreen(
    navController: NavController,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val loading = viewModel.isLoading.collectAsState()

    val allGalleryFiles = viewModel.allFilesFromGallery.collectAsState()
    val allFavoriteFiles = viewModel.favoriteGalleryList.collectAsState()

    val permissionsState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val eventObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    permissionsState.launchPermissionRequest()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(eventObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(eventObserver)
        }
    })

    val isClickAll = remember {
        mutableStateOf(true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (loading.value)
            LoadingView()

        when {
            permissionsState.hasPermission -> {
                if (allGalleryFiles.value.isEmpty())
                    viewModel.getGalleryInfo()
                else
                    GalleryList(
                        viewModel,
                        if (isClickAll.value) allGalleryFiles.value else allFavoriteFiles.value,
                        isClickAll
                    )
            }
            permissionsState.shouldShowRationale -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Reading external permission is required by this app")
                    Button(
                        onClick = {
                            permissionsState.launchPermissionRequest()
                        }, modifier = Modifier
                            .padding(horizontal = padding_10, vertical = padding_10)
                            .width(size_100)
                    ) {
                        Text(text = "Grant")
                    }
                }
            }
            !permissionsState.hasPermission && !permissionsState.shouldShowRationale -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Permission fully denied. Go to settings to enable")
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", context.packageName, null)
                            intent.data = uri
                            context.startActivity(intent)
                        }, modifier = Modifier
                            .padding(horizontal = padding_10, vertical = padding_10)
                            .width(size_180)
                    ) {
                        Text(text = "Go setting")
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun GalleryList(
    viewModel: GalleryViewModel,
    allGalleryFiles: List<GalleryModel>,
    isClickAll: MutableState<Boolean>
) {
    val context = LocalContext.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = padding_10),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    isClickAll.value = true
                }, modifier = Modifier
                    .padding(horizontal = padding_10)
                    .width(size_100)
            ) {
                Text(text = "All")
            }
            Button(
                onClick = {
                    isClickAll.value = false
                }, modifier = Modifier
                    .padding(horizontal = padding_10)
                    .width(size_100)
            ) {
                Text(text = "Favorite")
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(horizontal = padding_10)
        ) {
            items(allGalleryFiles.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(
                            start = padding_10,
                            end = padding_10,
                            top = padding_5,
                            bottom = padding_5
                        )
                        .size(size_180)
                ) {
                    val file = allGalleryFiles[index]
                    val isFavorite = remember {
                        mutableStateOf(file.isFavorite)
                    }
                    if (file.isVideo) {
                        VideoView(file.path)
                    } else {
                        val painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(context)
                                .data(file.path)
                                .crossfade(true)
                                .size(Size.ORIGINAL)
                                .build(),
                        )
                        Image(
                            painter = painter,
                            contentScale = ContentScale.Crop,
                            contentDescription = "gallery",
                            modifier = Modifier
                                .clip(RoundedCornerShape(corner_10))
                        )
                    }
                    if (isClickAll.value) {
                        Box(
                            modifier = Modifier
                                .size(size_40)
                                .padding(padding_5)
                                .align(Alignment.TopEnd)
                                .clickable {
                                    if (!isFavorite.value) {
                                        viewModel.setGalleryFavorite(file.path)
                                        isFavorite.value = true
                                    }
                                }
                        ) {
                            Image(
                                painter = painterResource(id = if (isFavorite.value) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(size_16)
                                    .align(Alignment.TopEnd),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoView(uri: Uri) {
    val context = LocalContext.current
    var playWhenReady by remember { mutableStateOf(true) }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            playWhenReady = playWhenReady
            prepare()
            play()
        }
    }
    DisposableEffect(
        AndroidView(
            modifier = Modifier
                .clip(RoundedCornerShape(corner_10)),
            factory = {
                StyledPlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}
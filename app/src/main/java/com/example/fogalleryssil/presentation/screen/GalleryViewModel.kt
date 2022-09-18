package com.example.fogalleryssil.presentation.screen

import android.app.Application
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.example.fogalleryssil.domain.model.FavoriteGallery
import com.example.fogalleryssil.domain.use_cases.GetFavoriteUseCase
import com.example.fogalleryssil.domain.use_cases.SetFavoriteGalleryUseCase
import com.example.fogalleryssil.presentation.core.BaseViewModel
import com.example.fogalleryssil.presentation.screen.model.GalleryModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.net.URI
import java.net.URLConnection
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    application: Application,
    private val getFavoriteUseCase: GetFavoriteUseCase,
    private val setFavoriteGalleryUseCase: SetFavoriteGalleryUseCase
) : BaseViewModel(application) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _allFilesFromGallery: MutableStateFlow<List<GalleryModel>> =
        MutableStateFlow(listOf())
    val allFilesFromGallery = _allFilesFromGallery.asStateFlow()

    private val _favoriteGalleryList: MutableStateFlow<List<GalleryModel>> =
        MutableStateFlow(listOf())
    val favoriteGalleryList = _favoriteGalleryList.asStateFlow()

    init {
        getGalleryInfo()
    }

    fun getGalleryInfo() {
        getGalleryFavorite()
        getGalleryAll()
    }

    private fun isVideoFile(path: Uri): Boolean {
        return try {
            val mimeType = context.contentResolver.getType(path)
            mimeType != "image/jpeg"
        } catch (e: Exception) {
            false
        }
    }

    fun setGalleryFavorite(uri: Uri) {
        uri.path?.let { path ->
            viewModelScope.launch {
                val newPath = uri.scheme + "://media" + path
                setFavoriteGalleryUseCase.invoke(SetFavoriteGalleryUseCase.Params(newPath))
                updateItemInAllGallery(uri)
                updateItemInFavorite(uri)
            }
        }
    }

    private fun updateItemInAllGallery(uri: Uri) {
        _allFilesFromGallery.value.first { it.path == uri }.isFavorite = true
    }

    private fun updateItemInFavorite(uri: Uri) {
        val list = _favoriteGalleryList.value.toMutableList()
        list.add(
            GalleryModel(
                isFavorite = true,
                path = uri,
                isVideo = isVideoFile(uri)
            )
        )
        _favoriteGalleryList.value = list
    }

    private fun getGalleryFavorite() {
        viewModelScope.launch {
            val allFavoriteList = mutableListOf<GalleryModel>()
            val favoriteResult = async { getFavoriteList() }
            val favoriteList = favoriteResult.await()

            favoriteList.distinctBy { it.path }.forEach { favorite ->
                val uri = favorite.path.toUri()
                allFavoriteList.add(
                    GalleryModel(
                        isFavorite = true,
                        path = uri,
                        isVideo = isVideoFile(uri)
                    )
                )
            }
            _favoriteGalleryList.value = allFavoriteList
        }
    }

    private fun getGalleryAll() {
        viewModelScope.launch {
            _isLoading.value = true
            val allGalleryFiles = mutableListOf<GalleryModel>()

            val favoriteResult = async { getFavoriteList() }
            val galleryResult = async { getAllFiles() }
            val favoriteList = favoriteResult.await()
            val galleryList = galleryResult.await()
            galleryList.forEach { uri ->
                val isFavorite = isFavorite(uri, favoriteList)
                allGalleryFiles.add(
                    GalleryModel(
                        isFavorite = isFavorite,
                        path = uri,
                        isVideo = isVideoFile(uri)
                    )
                )
            }
            _allFilesFromGallery.value = allGalleryFiles
            _isLoading.value = false
        }
    }

    private fun isFavorite(uri: Uri, list: List<FavoriteGallery>): Boolean {
        val newPath = uri.scheme + "://media" + uri.path
        return list.any { it.path == newPath }
    }

    private suspend fun getFavoriteList(): List<FavoriteGallery> {
        return getFavoriteUseCase.invoke().last()
    }

    private fun getAllFiles(): MutableList<Uri> {
        val allGalleryFiles = mutableListOf<Uri>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE
        )
        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

        val queryUri = MediaStore.Files.getContentUri("external")
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        val cursor = context.contentResolver.query(
            queryUri,
            projection,
            selection,
            null,
            sortOrder
        )
        cursor.use {
            it?.let {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    allGalleryFiles.add(contentUri)
                }
            } ?: kotlin.run {
                Log.e("TAG", "Cursor is null!")
            }
        }
        return allGalleryFiles
    }

    fun getCapturedImage(selectedPhotoUri: Uri): Bitmap {
        return when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                selectedPhotoUri
            )
            else -> {
                val source = ImageDecoder.createSource(context.contentResolver, selectedPhotoUri)
                ImageDecoder.decodeBitmap(source)
            }
        }
    }
}

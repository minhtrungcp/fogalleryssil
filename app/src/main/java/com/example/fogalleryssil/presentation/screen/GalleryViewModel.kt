package com.example.fogalleryssil.presentation.screen

import android.Manifest
import android.app.Application
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.example.fogalleryssil.domain.model.FavoriteGallery
import com.example.fogalleryssil.domain.use_cases.GetFavoriteUseCase
import com.example.fogalleryssil.domain.use_cases.SetFavoriteGalleryUseCase
import com.example.fogalleryssil.presentation.core.BaseViewModel
import com.example.fogalleryssil.presentation.screen.model.GalleryModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GalleryViewModel @Inject constructor(
    application: Application,
    private val getFavoriteUseCase: GetFavoriteUseCase,
    private val setFavoriteGalleryUseCase: SetFavoriteGalleryUseCase
) : BaseViewModel(application) {

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
        }
    }

    private fun isFavorite(uri: Uri, list: List<FavoriteGallery>): Boolean {
        val newPath = uri.scheme + "://media" + uri.path
        return list.any { it.path == newPath }
    }

    private suspend fun getFavoriteList(): List<FavoriteGallery> {
        return getFavoriteUseCase.invoke().last()
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getAllFiles(): MutableList<Uri> {
        val allGalleryFiles = mutableListOf<Uri>()
        if (checkPermission()) {
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
                    val mimeTypeColumn: Int =
                        it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                    while (it.moveToNext()) {
                        val id = it.getLong(idColumn)
                        val mimeType = it.getString(mimeTypeColumn)
                        val contentUri =
                            ContentUris.withAppendedId(
                                if (mimeType.contains("video")) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id
                            )
                        allGalleryFiles.add(contentUri)
                    }
                } ?: kotlin.run {
                    Log.e("TAG", "Cursor is null!")
                }
            }
        }
        return allGalleryFiles
    }
}

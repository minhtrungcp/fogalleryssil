package com.example.fogalleryssil.data.repository

import android.net.Uri
import com.example.fogalleryssil.data.db.GalleryDAO
import com.example.fogalleryssil.data.mapper.Mapper
import com.example.fogalleryssil.data.model.FavoriteGalleryEntity
import com.example.fogalleryssil.data.model.toModel
import com.example.fogalleryssil.domain.model.FavoriteGallery
import com.example.fogalleryssil.domain.repository.LocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalRepositoryImpl @Inject constructor(
    private val galleryDAO: GalleryDAO
) :
    LocalRepository {
    override suspend fun insertFavorite(path: String): Long = withContext(Dispatchers.IO) {
        galleryDAO.insertFavorite(FavoriteGalleryEntity(path = path))
    }

    override suspend fun deleteFavorite(path: String) = withContext(Dispatchers.IO) {
        galleryDAO.deleteFavorite(path)
    }

    override suspend fun deleteAllFavorite() = withContext(Dispatchers.IO) {
        galleryDAO.deleteAllFavorite()
    }

    override fun getFavoriteList(): Flow<List<FavoriteGallery>> = flow {
        try {
            val response = galleryDAO.getFavoriteList()
            emit(response.map { it.toModel() })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}
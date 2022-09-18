package com.example.fogalleryssil.domain.repository

import com.example.fogalleryssil.domain.model.FavoriteGallery
import kotlinx.coroutines.flow.Flow

interface LocalRepository {
    suspend fun insertFavorite(path: String): Long
    suspend fun deleteFavorite(path: String)
    suspend fun deleteAllFavorite()
    fun getFavoriteList(): Flow<List<FavoriteGallery>>
}
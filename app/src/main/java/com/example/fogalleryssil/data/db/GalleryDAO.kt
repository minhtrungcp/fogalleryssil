package com.example.fogalleryssil.data.db

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fogalleryssil.data.model.FavoriteGalleryEntity

@Dao
interface GalleryDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(entity: FavoriteGalleryEntity): Long

    @Query("delete from gallery_favorite where path =:path")
    suspend fun deleteFavorite(path: String)

    @Query("delete from gallery_favorite")
    suspend fun deleteAllFavorite()

    @Query("select * from gallery_favorite ")
    suspend fun getFavoriteList(): List<FavoriteGalleryEntity>
}
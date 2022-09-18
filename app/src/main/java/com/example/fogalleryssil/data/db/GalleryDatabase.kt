package com.example.fogalleryssil.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fogalleryssil.data.model.FavoriteGalleryEntity

@Database(entities = [FavoriteGalleryEntity::class], version = 1, exportSchema = false)
abstract class GalleryDatabase : RoomDatabase() {
    abstract fun galleryDAO(): GalleryDAO
}
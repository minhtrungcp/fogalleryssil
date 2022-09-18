package com.example.fogalleryssil.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fogalleryssil.domain.model.FavoriteGallery

@Entity(tableName = "gallery_favorite")
data class FavoriteGalleryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "path")
    var path: String
)


fun FavoriteGalleryEntity.toModel() = FavoriteGallery(path = path)

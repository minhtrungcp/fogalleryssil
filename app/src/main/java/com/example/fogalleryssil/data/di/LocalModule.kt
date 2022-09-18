package com.example.fogalleryssil.data.di

import android.content.Context
import androidx.room.Room
import com.example.fogalleryssil.data.db.GalleryDAO
import com.example.fogalleryssil.data.db.GalleryDatabase
import com.example.fogalleryssil.data.mapper.Mapper
import com.example.fogalleryssil.data.model.FavoriteGalleryEntity
import com.example.fogalleryssil.data.repository.LocalRepositoryImpl
import com.example.fogalleryssil.domain.model.FavoriteGallery
import com.example.fogalleryssil.domain.repository.LocalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): GalleryDatabase {
        return Room.databaseBuilder(context, GalleryDatabase::class.java, "fogalleryssil.database")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    @Singleton
    @Provides
    fun cityDao(database: GalleryDatabase): GalleryDAO {
        return database.galleryDAO()
    }

    @Provides
    @Singleton
    fun provideLocalRepository(
        galleryDAO: GalleryDAO
    ): LocalRepository =
        LocalRepositoryImpl(galleryDAO)

}
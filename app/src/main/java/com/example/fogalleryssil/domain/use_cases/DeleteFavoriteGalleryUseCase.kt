package com.example.fogalleryssil.domain.use_cases

import android.net.Uri
import com.example.fogalleryssil.domain.repository.LocalRepository
import javax.inject.Inject

class DeleteFavoriteGalleryUseCase @Inject constructor(private val localRepository: LocalRepository) {
    @JvmInline
    value class Params(
        val path: String
    )

    suspend operator fun invoke(params: Params) = localRepository.deleteFavorite(params.path)
}
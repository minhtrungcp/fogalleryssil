package com.example.fogalleryssil.domain.use_cases

import com.example.fogalleryssil.domain.repository.LocalRepository
import javax.inject.Inject

class SetFavoriteGalleryUseCase @Inject constructor(private val localRepository: LocalRepository) {
    @JvmInline
    value class Params(
        val path: String
    )

    suspend operator fun invoke(params: Params) = localRepository.insertFavorite(params.path)
}
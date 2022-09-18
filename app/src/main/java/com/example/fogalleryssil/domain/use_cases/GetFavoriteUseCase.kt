package com.example.fogalleryssil.domain.use_cases

import com.example.fogalleryssil.domain.repository.LocalRepository
import javax.inject.Inject

class GetFavoriteUseCase @Inject constructor(private val localRepository: LocalRepository) {
    operator fun invoke() = localRepository.getFavoriteList()
}
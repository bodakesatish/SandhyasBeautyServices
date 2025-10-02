package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCategoriesWithServicesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke() = categoryRepository.getCategoriesWithServices()
}
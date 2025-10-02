package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCategoryListUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke() = categoryRepository.getCategoryList()
}
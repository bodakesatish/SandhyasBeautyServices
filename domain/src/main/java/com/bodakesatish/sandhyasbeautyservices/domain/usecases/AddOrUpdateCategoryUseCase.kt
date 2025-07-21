package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.repository.CategoryRepository
import com.bodakesatish.sandhyasbeautyservices.domain.utils.NetworkResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddOrUpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
)  {
    suspend operator fun invoke(category: Category): NetworkResult<String> {
       return categoryRepository.insertOrUpdateCategory(category)
    }
}
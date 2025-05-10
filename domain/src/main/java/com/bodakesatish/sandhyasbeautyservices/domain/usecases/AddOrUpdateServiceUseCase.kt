package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddOrUpdateServiceUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
)  {
    suspend operator fun invoke(service: Service): Long {
       return categoryRepository.insertOrUpdateService(service)
    }
}
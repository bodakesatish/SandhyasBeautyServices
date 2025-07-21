package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetServicesListUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(categoryId: String) = categoryRepository.getServiceList(categoryId)
}
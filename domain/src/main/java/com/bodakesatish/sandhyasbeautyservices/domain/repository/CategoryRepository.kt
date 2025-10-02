package com.bodakesatish.sandhyasbeautyservices.domain.repository

import com.bodakesatish.sandhyasbeautyservices.domain.model.CategoriesWithServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun insertOrUpdateCategory(category: Category): Long
    suspend fun deleteCategory(categoryId: Int)
    fun getCategoryList(): Flow<List<Category>>
    suspend fun getCategoryById(categoryId: Int): Category?

    suspend fun insertOrUpdateService(service: Service): Long
    suspend fun deleteService(serviceId: Int)
    fun getServiceList(categoryId: Int): Flow<List<Service>>
    suspend fun getServiceById(serviceId: Long): Service?

    fun getCategoriesWithServices(): Flow<List<CategoriesWithServices>>
}
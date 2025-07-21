package com.bodakesatish.sandhyasbeautyservices.domain.repository

import com.bodakesatish.sandhyasbeautyservices.domain.model.CategoriesWithServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.utils.NetworkResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun insertOrUpdateCategory(category: Category): NetworkResult<String>
    suspend fun deleteCategory(categoryId: String) : NetworkResult<Boolean>
    fun getCategoryList(forceToRefresh: Boolean): Flow<NetworkResult<List<Category>>>
    suspend fun getCategoryById(categoryId: String): Category?

    suspend fun insertOrUpdateService(service: Service): Long
    suspend fun deleteService(serviceId: Int)
    fun getServiceList(categoryId: String): Flow<List<Service>>
    suspend fun getServiceById(serviceId: Long): Service?

    fun getCategoriesWithServices(): Flow<List<CategoriesWithServices>>
}
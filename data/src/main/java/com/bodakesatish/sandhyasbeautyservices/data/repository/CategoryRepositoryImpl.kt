package com.bodakesatish.sandhyasbeautyservices.data.repository

import com.bodakesatish.sandhyasbeautyservices.data.mapper.CategoryMapper.mapFromDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.CategoryMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.ServiceMapper.mapFromDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.ServiceMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.CategoryDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.ServicesDao
import com.bodakesatish.sandhyasbeautyservices.domain.model.CategoriesWithServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val serviceDao: ServicesDao
) : CategoryRepository {

    override suspend fun insertOrUpdateCategory(customer: Category): Long {
        return categoryDao.insertOrUpdate(customer.mapFromDomainModel())
    }

    override suspend fun deleteCategory(customerId: Int) {
        return categoryDao.delete(customerId)
    }

    override fun getCategoryList(): Flow<List<Category>> {
        return categoryDao.getCategoryList().map { customers ->
            customers.map { customer ->
                customer.mapToDomainModel()
            }
        }
    }

    override suspend fun getCategoryById(customerId: Long): Category? {
        TODO("Not yet implemented")
    }

    override suspend fun insertOrUpdateService(service: Service): Long {
        val result = serviceDao.insertOrUpdateService(service.mapFromDomainModel())
        return 1
    }

    override suspend fun deleteService(serviceId: Int) {
        return serviceDao.deleteService(serviceId)
    }

    override fun getServiceList(categoryId: Int): Flow<List<Service>> {
        return serviceDao.getServiceListByCategoryId(categoryId).map { services ->
            services.map { service ->
                service.mapToDomainModel()
            }
        }
    }

    override suspend fun getServiceById(serviceId: Long): Service? {
        TODO("Not yet implemented")
    }

    override fun getCategoriesWithServices(): Flow<List<CategoriesWithServices>> {
        return categoryDao.getCategoriesWithServices().map { categoryWithServices ->
            categoryWithServices.map {
                CategoriesWithServices(
                    category = it.category.mapToDomainModel(),
                    services = it.services.map { serviceEntity ->
                        serviceEntity.mapToDomainModel()
                    }
                )
            }
        }
    }

}
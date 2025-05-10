package com.bodakesatish.sandhyasbeautyservices.data.repository

import com.bodakesatish.sandhyasbeautyservices.data.mapper.CustomerMapper.mapFromDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.CustomerMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.CustomerDao
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override suspend fun insertOrUpdate(customer: Customer): Long {
        return customerDao.insertOrUpdate(customer.mapFromDomainModel())
    }

    override suspend fun update(customer: Customer): Long {
        return customerDao.insertOrUpdate(customer.mapFromDomainModel())
    }

    override suspend fun delete(customerId: Int) {
        return customerDao.delete(customerId)
    }

    override fun getCustomerList(): Flow<List<Customer>> {
        return customerDao.getCustomerList().map { customers ->
            customers.map { customer ->
                customer.mapToDomainModel()
            }
        }
    }

    override suspend fun getCustomerById(customerId: Long): Customer? {
        TODO("Not yet implemented")
    }

}
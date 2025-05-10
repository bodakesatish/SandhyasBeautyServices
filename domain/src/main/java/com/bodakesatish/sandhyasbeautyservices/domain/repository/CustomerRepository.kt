package com.bodakesatish.sandhyasbeautyservices.domain.repository

import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    suspend fun insertOrUpdate(customer: Customer): Long
    suspend fun update(customer: Customer): Long
    suspend fun delete(customerId: Int)
    fun getCustomerList(): Flow<List<Customer>>
    suspend fun getCustomerById(customerId: Long): Customer?
}
package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.repository.CustomerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddOrUpdateCustomerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
)  {
    suspend operator fun invoke(customer: Customer): Long {
       return customerRepository.insertOrUpdate(customer)
    }
}
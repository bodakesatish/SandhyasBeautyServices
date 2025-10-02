package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.repository.CustomerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetSelectedCustomerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(customerId: Int) = customerRepository.getCustomerById(customerId)
}
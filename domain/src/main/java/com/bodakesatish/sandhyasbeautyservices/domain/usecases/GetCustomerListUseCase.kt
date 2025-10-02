package com.bodakesatish.sandhyasbeautyservices.domain.usecases

import com.bodakesatish.sandhyasbeautyservices.domain.repository.CustomerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCustomerListUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    operator fun invoke() = customerRepository.getCustomerList()
}
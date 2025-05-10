package com.bodakesatish.sandhyasbeautyservices.data.mapper

import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CustomerEntity
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer

object CustomerMapper : Mapper<CustomerEntity, Customer> {
    override fun CustomerEntity.mapToDomainModel(): Customer {
        return Customer(
            id = id,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            address = address,
            age = age,
        )
    }

    override fun Customer.mapFromDomainModel(): CustomerEntity {
        return CustomerEntity(
            id = id,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            address = address,
            age = age,
        )
    }

}
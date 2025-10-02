package com.bodakesatish.sandhyasbeautyservices.data.mapper

import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceEntity
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service

object ServiceMapper : Mapper<ServiceEntity, Service> {
    override fun ServiceEntity.mapToDomainModel(): Service {
        return Service(
            id = id,
            categoryId = categoryId,
            serviceName = serviceName,
            servicePrice = servicePrice
        )
    }

    override fun Service.mapFromDomainModel(): ServiceEntity {
        return ServiceEntity(
            id = id,
            categoryId = categoryId,
            serviceName = serviceName,
            servicePrice = servicePrice
        )
    }

}
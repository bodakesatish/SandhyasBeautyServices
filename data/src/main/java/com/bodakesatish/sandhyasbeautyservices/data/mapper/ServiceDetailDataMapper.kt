package com.bodakesatish.sandhyasbeautyservices.data.mapper

import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceDetailEntity
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetail
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService

object ServiceDetailDataMapper : Mapper<ServiceDetailEntity, ServiceDetailWithService> {
    override fun ServiceDetailEntity.mapToDomainModel(): ServiceDetailWithService {
        return ServiceDetailWithService(
            id = id,
            customerId = customerId,
            appointmentId = appointmentId,
            serviceId = serviceId,
            originalPrice = originalAmount,
            discountAmount = discount,
            priceAfterDiscount = priceAfterDiscount,
            serviceSummary = serviceSummary,
            serviceName = ""
        )
    }

    override fun ServiceDetailWithService.mapFromDomainModel(): ServiceDetailEntity {
        return ServiceDetailEntity(
            id = id,
            customerId = customerId,
            appointmentId = appointmentId,
            serviceId = serviceId,
            originalAmount = originalPrice,
            discount = discountAmount,
            discountPercentage = 0.0,
            priceAfterDiscount = finalPrice,
            serviceSummary = serviceSummary
        )
    }

}
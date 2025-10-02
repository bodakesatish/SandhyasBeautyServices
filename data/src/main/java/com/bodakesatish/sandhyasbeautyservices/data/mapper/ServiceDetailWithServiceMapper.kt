package com.bodakesatish.sandhyasbeautyservices.data.mapper

import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceDetailWithServiceData
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService

object ServiceDetailWithServiceMapper : Mapper<ServiceDetailWithServiceData, ServiceDetailWithService> {
    override fun ServiceDetailWithServiceData.mapToDomainModel(): ServiceDetailWithService {
        return ServiceDetailWithService(
            id = id,
            appointmentId = appointmentId,
            serviceId = serviceId,
            customerId = customerId,
            originalPrice = amount,
            discountAmount = discount,
            priceAfterDiscount = priceAfterDiscount,
            serviceSummary = serviceSummary,
            serviceName = serviceName
        )
    }

    override fun ServiceDetailWithService.mapFromDomainModel(): ServiceDetailWithServiceData {
        return ServiceDetailWithServiceData(
            id = id,
            appointmentId = appointmentId,
            serviceId = serviceId,
            customerId = customerId,
            amount = originalPrice,
            discount = discountAmount,
            priceAfterDiscount = priceAfterDiscount,
            serviceSummary = serviceSummary,
            serviceName = serviceName
        )
    }

}
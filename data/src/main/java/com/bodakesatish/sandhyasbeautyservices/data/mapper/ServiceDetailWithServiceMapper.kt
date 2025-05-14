package com.bodakesatish.sandhyasbeautyservices.data.mapper

import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceDetailWithServiceData
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService

object ServiceDetailWithServiceMapper : Mapper<ServiceDetailWithServiceData, ServiceDetailWithService> {
    override fun ServiceDetailWithServiceData.mapToDomainModel(): ServiceDetailWithService {
        return ServiceDetailWithService(
            id = id,
            appointmentId = appointmentId,
            serviceId = serviceId,
            quantity = 0,
            amount = amount,
            serviceName = serviceName,
            servicePrice = normalPrice
        )
    }

    override fun ServiceDetailWithService.mapFromDomainModel(): ServiceDetailWithServiceData {
        return ServiceDetailWithServiceData(
            id = id,
            appointmentId = appointmentId,
            serviceId = serviceId,
            amount = amount,
            serviceName = serviceName,
            normalPrice = servicePrice
        )
    }

}
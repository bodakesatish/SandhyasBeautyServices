package com.bodakesatish.sandhyasbeautyservices.data.mapper

import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceDetailEntity
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetail

object ServiceDetailMapper : Mapper<ServiceDetailEntity, ServiceDetail> {
    override fun ServiceDetailEntity.mapToDomainModel(): ServiceDetail {
        return ServiceDetail(
            id = id,
            customerId = customerId,
            appointmentId = appointmentId,
            serviceId = serviceId,
            amount = amount,
            discount = discount,
            priceAfterDiscount = priceAfterDiscount
        )
    }

    override fun ServiceDetail.mapFromDomainModel(): ServiceDetailEntity {
        return ServiceDetailEntity(
            id = id,
            customerId = customerId,
            appointmentId = appointmentId,
            serviceId = serviceId,
            amount = amount,
            discount = discount,
            priceAfterDiscount = priceAfterDiscount
        )
    }

}
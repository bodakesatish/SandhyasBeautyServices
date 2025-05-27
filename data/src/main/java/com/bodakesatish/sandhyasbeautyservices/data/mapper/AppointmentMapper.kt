package com.bodakesatish.sandhyasbeautyservices.data.mapper

import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.AppointmentDataStatus // Data layer enum
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.AppointmentsEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.PaymentDataStatus // Data layer enum
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.PaymentModeDataStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus // Domain layer enum
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentMode // Domain layer enum
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentStatus // Domain layer enum

object AppointmentMapper : Mapper<AppointmentsEntity, Appointment> {

    override fun AppointmentsEntity.mapToDomainModel(): Appointment {
        return Appointment(
            id = id,
            customerId = customerId,
            appointmentDate = appointmentDate, // Assuming domain also uses Date for now
            appointmentTime = appointmentTime, // Assuming domain also uses Date for now
            // Consider mapping to LocalTime if domain uses LocalTime:
            // appointmentTime = appointmentTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime(),

            // appointmentPlannedDateTime and appointmentCompletedDateTime would need careful mapping
            // if their source in AppointmentsEntity is different (e.g., if they are also just Date types)
            // and the domain model expects OffsetDateTime. This requires more context on how they are stored.
            // For now, let's assume they are not directly in AppointmentsEntity or are handled similarly to appointmentTime.

            totalBillAmount = totalBillAmount, // Consider converting to BigDecimal in domain if entity uses Double
            servicesDiscount = servicesDiscount,
            otherDiscount = otherDiscount,
            totalDiscount = totalDiscount, // Consider converting to BigDecimal in domain if entity uses Double
            netTotal = netTotal, // Consider converting to BigDecimal in domain if entity uses Double
            paymentMode = mapDataPaymentModeStatusToDomain(paymentMode), // Entity uses String
            appointmentStatus = mapDataStatusToDomain(appointmentStatus),
         //   paymentStatus = mapDataPaymentStatusToDomain(paymentStatus),

//          servicesSummary = servicesSummary.takeIf { it.isNotBlank() }, // Map empty string to null for domain
            appointmentNotes = appointmentNotes, // Map empty string to null for domain
            paymentNotes = paymentNotes // Map empty string to null for domain
        )
    }

    override fun Appointment.mapFromDomainModel(): AppointmentsEntity {
        return AppointmentsEntity(
            id = id,
            customerId = customerId,
            appointmentDate = appointmentDate,
            appointmentTime = appointmentTime, // Consider mapping from LocalTime to Date if domain uses LocalTime
            totalBillAmount = totalBillAmount, // Handle null from domain, provide default for entity
            servicesDiscount = servicesDiscount,
            otherDiscount = otherDiscount,
            totalDiscount = totalDiscount,
            netTotal = netTotal,
            paymentMode = mapDomainPaymentModeToData(paymentMode), // Map enum to String for entity
            appointmentNotes = appointmentNotes, // Map null from domain to empty string for entity
            appointmentStatus = mapDomainStatusToData(appointmentStatus),
            //paymentStatus = mapDomainPaymentStatusToData(paymentStatus),
            paymentNotes = paymentNotes // Map null from domain to empty string for entity,

        )
    }

    // --- Helper Mapping Functions for Enums ---

    // Appointment Status Mapping
    private fun mapDataStatusToDomain(dataStatus: AppointmentDataStatus): AppointmentStatus {
        return when (dataStatus) {
            AppointmentDataStatus.PENDING -> AppointmentStatus.PENDING
            AppointmentDataStatus.COMPLETED -> AppointmentStatus.COMPLETED
            AppointmentDataStatus.UNKNOWN -> AppointmentStatus.UNKNOWN
            // Add default or error handling if dataStatus could have values not in domain
            // else -> AppointmentStatus.UNKNOWN // Example
        }
    }

    private fun mapDomainStatusToData(domainStatus: AppointmentStatus): AppointmentDataStatus {
        return when (domainStatus) {
            AppointmentStatus.PENDING -> AppointmentDataStatus.PENDING
            AppointmentStatus.COMPLETED -> AppointmentDataStatus.COMPLETED
            AppointmentStatus.UNKNOWN -> AppointmentDataStatus.PENDING // Fallback to a sensible default
            // It's crucial to define how all domain statuses map to the more limited data statuses
        }
    }

    // Payment Status Mapping
    private fun mapDataPaymentStatusToDomain(dataPaymentStatus: PaymentDataStatus): PaymentStatus {
        return when (dataPaymentStatus) {
            PaymentDataStatus.PAID -> PaymentStatus.PAID
            PaymentDataStatus.UNPAID -> PaymentStatus.UNPAID
            PaymentDataStatus.PARTIALLY_PAID -> PaymentStatus.PARTIALLY_PAID
            else -> PaymentStatus.UNKNOWN
        }
    }

    private fun mapDomainPaymentStatusToData(domainPaymentStatus: PaymentStatus?): PaymentDataStatus {
        return when (domainPaymentStatus) {
            PaymentStatus.PAID -> PaymentDataStatus.PAID
            PaymentStatus.UNPAID -> PaymentDataStatus.UNPAID
            PaymentStatus.PARTIALLY_PAID -> PaymentDataStatus.PARTIALLY_PAID
            PaymentStatus.UNKNOWN -> PaymentDataStatus.UNPAID // Default
            null -> PaymentDataStatus.UNPAID // Default for null domain status
        }
    }

    private fun mapDataPaymentModeStatusToDomain(paymentModeDataStatus: PaymentModeDataStatus): PaymentMode {
        return when (paymentModeDataStatus){
            PaymentModeDataStatus.CASH -> PaymentMode.CASH
            PaymentModeDataStatus.PENDING -> PaymentMode.PENDING
            PaymentModeDataStatus.UNKNOWN -> PaymentMode.UNKNOWN
            PaymentModeDataStatus.UPI -> PaymentMode.UPI
        }
    }

    private fun mapDomainPaymentModeToData(paymentMode: PaymentMode): PaymentModeDataStatus {
        return when (paymentMode){
            PaymentMode.CASH -> PaymentModeDataStatus.CASH
            PaymentMode.UPI -> PaymentModeDataStatus.UPI
            PaymentMode.PENDING -> PaymentModeDataStatus.PENDING
            PaymentMode.UNKNOWN -> PaymentModeDataStatus.UNKNOWN
        }
    }

}
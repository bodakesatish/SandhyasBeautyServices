package com.bodakesatish.sandhyasbeautyservices.data.mapper

import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.AppointmentDataStatus // Data layer enum
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.AppointmentsEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.PaymentDataStatus // Data layer enum
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus // Domain layer enum
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentMode // Domain layer enum
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentStatus // Domain layer enum
import java.util.Date

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
            appointmentPlannedDateTime = Date(), // Placeholder: Map appropriately if source exists
            appointmentCompletedDateTime = Date(), // Placeholder: Map appropriately if source exists

            totalBillAmount = totalBillAmount, // Consider converting to BigDecimal in domain if entity uses Double
            status = mapDataStatusToDomain(this.status),
            paymentMode = mapPaymentModeStringToDomain(this.paymentMode), // Entity uses String
            paymentStatus = mapDataPaymentStatusToDomain(this.paymentStatus),
            servicesSummary = servicesSummary.takeIf { it.isNotBlank() }, // Map empty string to null for domain
            notes = notes.takeIf { it.isNotBlank() } // Map empty string to null for domain
        )
    }

    override fun Appointment.mapFromDomainModel(): AppointmentsEntity {
        return AppointmentsEntity(
            id = id,
            customerId = customerId,
            appointmentDate = appointmentDate,
            appointmentTime = appointmentTime, // Consider mapping from LocalTime to Date if domain uses LocalTime
            totalBillAmount = totalBillAmount ?: 0.0, // Handle null from domain, provide default for entity
            paymentMode = mapPaymentModeDomainToString(this.paymentMode), // Map enum to String for entity
            servicesSummary = servicesSummary ?: "", // Map null from domain to empty string for entity
            status = mapDomainStatusToData(this.status),
            paymentStatus = mapDomainPaymentStatusToData(this.paymentStatus),
            notes = notes ?: "" // Map null from domain to empty string for entity
        )
    }

    // --- Helper Mapping Functions for Enums ---

    // Appointment Status Mapping
    private fun mapDataStatusToDomain(dataStatus: AppointmentDataStatus): AppointmentStatus {
        return when (dataStatus) {
            AppointmentDataStatus.PENDING -> AppointmentStatus.PENDING
            AppointmentDataStatus.CONFIRMED -> AppointmentStatus.CONFIRMED
            AppointmentDataStatus.COMPLETED -> AppointmentStatus.COMPLETED
            AppointmentDataStatus.CANCELLED -> AppointmentStatus.CANCELLED
            // Add default or error handling if dataStatus could have values not in domain
            // else -> AppointmentStatus.UNKNOWN // Example
        }
    }

    private fun mapDomainStatusToData(domainStatus: AppointmentStatus): AppointmentDataStatus {
        return when (domainStatus) {
            AppointmentStatus.PENDING -> AppointmentDataStatus.PENDING
            AppointmentStatus.SCHEDULED -> AppointmentDataStatus.PENDING // Or map to a specific DataStatus if available, e.g., CONFIRMED
            AppointmentStatus.CONFIRMED -> AppointmentDataStatus.CONFIRMED
            AppointmentStatus.IN_PROGRESS -> AppointmentDataStatus.CONFIRMED // Example: InProgress might still be 'CONFIRMED' at data layer
            AppointmentStatus.COMPLETED -> AppointmentDataStatus.COMPLETED
            AppointmentStatus.CANCELLED -> AppointmentDataStatus.CANCELLED
            AppointmentStatus.NO_SHOW -> AppointmentDataStatus.CANCELLED // Example: NoShow might be stored as 'CANCELLED'
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
            // else -> PaymentStatus.UNKNOWN
        }
    }

    private fun mapDomainPaymentStatusToData(domainPaymentStatus: PaymentStatus?): PaymentDataStatus {
        return when (domainPaymentStatus) {
            PaymentStatus.PAID -> PaymentDataStatus.PAID
            PaymentStatus.UNPAID -> PaymentDataStatus.UNPAID
            PaymentStatus.PENDING_CONFIRMATION -> PaymentDataStatus.UNPAID // Example
            PaymentStatus.PARTIALLY_PAID -> PaymentDataStatus.PARTIALLY_PAID
            PaymentStatus.REFUNDED -> PaymentDataStatus.UNPAID // Or handle as a separate data status if available
            PaymentStatus.FAILED -> PaymentDataStatus.UNPAID // Example
            PaymentStatus.UNKNOWN -> PaymentDataStatus.UNPAID // Default
            null -> PaymentDataStatus.UNPAID // Default for null domain status
        }
    }

    // Payment Mode Mapping (String in Entity to Enum in Domain)
    private fun mapPaymentModeStringToDomain(paymentModeString: String): PaymentMode {
        return try {
            PaymentMode.valueOf(paymentModeString.uppercase()) // Assumes string matches enum names (case-insensitive)
        } catch (e: IllegalArgumentException) {
            PaymentMode.UNKNOWN // Fallback for unknown strings
        }
    }

    private fun mapPaymentModeDomainToString(paymentMode: PaymentMode?): String {
        return paymentMode?.name ?: PaymentMode.UNKNOWN.name // Store as uppercase string or a default
    }
}
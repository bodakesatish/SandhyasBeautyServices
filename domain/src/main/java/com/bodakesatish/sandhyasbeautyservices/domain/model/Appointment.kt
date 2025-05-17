package com.bodakesatish.sandhyasbeautyservices.domain.model

import java.util.Date
import java.time.LocalDate // For date without time
import java.time.LocalTime // For time without date
import java.time.OffsetDateTime // Or ZonedDateTime if you need to store full timezone info with completion/planning

// Define Enums for Statuses (preferably in their own files or a shared model file)

enum class AppointmentStatus {
    PENDING,
    SCHEDULED,
    CONFIRMED, // Example, if you have this step
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    UNKNOWN // Good to have a fallback
}

enum class PaymentMode {
    ONLINE,
    CASH,
    CARD,
    UPI, // Unified Payments Interface - common in India
    NOT_APPLICABLE, // e.g., for free consultations
    UNKNOWN,
    PENDING
}

enum class PaymentStatus { // This was used in the adapter, ensure it's defined
    PAID,
    UNPAID,
    PENDING_CONFIRMATION,
    PARTIALLY_PAID,
    REFUNDED,
    FAILED,
    UNKNOWN
}

data class Appointment(
    var id: Int = 0,
    var customerId: Int = 0,

    // Option 1: Keep Date for compatibility, but aim to map to LocalDate in ViewModel/Repository
    var appointmentDate: Date = Date(), // Or consider using LocalDate directly if feasible

    // For appointment time, store as LocalTime (time of day) or String if always pre-formatted
    var appointmentTime: Date = Date(), // Time of the appointment (e.g., 10:30)
    // Alternatively, keep as String if it's already formatted:
    // var appointmentTime: String? = null, e.g., "10:30 AM"

    // For planned/completed times, if they include date and time, OffsetDateTime or ZonedDateTime is better
    // If they are just durations or offsets, different types might be needed.
    // Assuming these are specific points in time:
    var appointmentPlannedDateTime: Date = Date(),//OffsetDateTime? = null, // When the appointment was initially planned/booked
    var appointmentCompletedDateTime: Date = Date(),//OffsetDateTime? = null, // Actual completion timestamp

    var totalBillAmount: Double? = null, // Make nullable if it can be absent (e.g., before billing)
    // Consider BigDecimal for financial calculations to avoid precision issues

    var status: AppointmentStatus = AppointmentStatus.PENDING, // Use the enum

    var paymentMode: PaymentMode? = null, // Use enum, make nullable if not always set
    var paymentStatus: PaymentStatus? = null, // Add if needed, was used in adapter

    var servicesSummary: String? = null, // Make nullable if it can be empty
    var notes: String? = null // Example: additional field for internal notes
)

// Helper to get default Date for appointmentDate (if still using Date)
// Consider moving to a companion object or a DateHelper if used extensively
// fun getDefaultAppointmentDate(): Date = Date()
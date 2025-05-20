package com.bodakesatish.sandhyasbeautyservices.domain.model

import java.util.Date

// Define Enums for Statuses (preferably in their own files or a shared model file)

enum class AppointmentStatus {
    PENDING,
//    SCHEDULED,
//    CONFIRMED, // Example, if you have this step
//    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
//    NO_SHOW,
    UNKNOWN // Good to have a fallback
}

enum class PaymentMode {
    ONLINE,
    CASH,
//    CARD,
//    UPI, // Unified Payments Interface - common in India
//    NOT_APPLICABLE, // e.g., for free consultations
    UNKNOWN,
    PENDING
}

enum class PaymentStatus { // This was used in the adapter, ensure it's defined
    PAID,
    UNPAID,
//    PENDING_CONFIRMATION,
    PARTIALLY_PAID,
//    REFUNDED,
//    FAILED,
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

    var totalBillAmount: Double = 0.0, // Make nullable if it can be absent (e.g., before billing)
    // Consider BigDecimal for financial calculations to avoid precision issues

    var totalDiscount: Double = 0.0, // Make nullable if it can be absent

    var totalDiscountPercentage: Double = 0.0, // Make nullable if it can be absent

    var paymentMode: PaymentMode = PaymentMode.PENDING, // Use enum, make nullable if not always set

    var servicesSummary: String = "", // Make nullable if it can be empty

    var appointmentStatus: AppointmentStatus = AppointmentStatus.PENDING, // Use the enum

    var paymentStatus: PaymentStatus = PaymentStatus.UNPAID, // Add if needed, was used in adapter

    var serviceNotes: String = "" // Example: additional field for internal notes
)

// Helper to get default Date for appointmentDate (if still using Date)
// Consider moving to a companion object or a DateHelper if used extensively
// fun getDefaultAppointmentDate(): Date = Date()
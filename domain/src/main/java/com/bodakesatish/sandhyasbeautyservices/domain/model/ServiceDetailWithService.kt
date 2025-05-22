package com.bodakesatish.sandhyasbeautyservices.domain.model

// Define this data class in your data layer (e.g., within a 'models' or 'entities' package)
data class ServiceDetailWithService(
    // Fields from ServiceDetail (assuming a ServiceDetail entity exists)
    val id: Int, // Assuming ServiceDetail has an ID
    val appointmentId: Int,
    val serviceId: Int,
    val customerId: Int,
    //val quantity: Int, // Example field from ServiceDetail
    val originalPrice: Double, // Example field from ServiceDetail (price specifically for this appointment)
    val discountAmount: Double,
    val priceAfterDiscount: Double,
    val serviceSummary: String,
    // Fields from Service (assuming a Service entity exists)
    val serviceName: String,
    // Add any other fields from Service you select in the query

    // Transient property, calculated
    var calculatedDiscountValue: Double = discountAmount.coerceAtMost(originalPrice), // Discount cannot exceed original price
    var finalPrice: Double = (originalPrice - calculatedDiscountValue).coerceAtLeast(0.0) // Final price cannot be negative

) {

    // Convenience to create a copy with updated discount
    fun withUpdatedDiscount(
        newDiscountAmount: Double
    ): ServiceDetailWithService {
        val newDis = newDiscountAmount
        val calculatedDiscountValue = newDiscountAmount.coerceAtMost(originalPrice)
        val finalPrice =  (originalPrice - calculatedDiscountValue).coerceAtLeast(0.0)
        return this.copy(
            discountAmount = newDis,
            calculatedDiscountValue = calculatedDiscountValue,
            finalPrice = finalPrice
        )
    }
}
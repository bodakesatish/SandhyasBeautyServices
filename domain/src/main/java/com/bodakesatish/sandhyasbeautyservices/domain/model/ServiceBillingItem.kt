package com.bodakesatish.sandhyasbeautyservices.domain.model

data class ServiceBillingItem1(
    val id: String, // Unique ID for the service
    val serviceName: String,
    val originalPrice: Double,
    var discountAmount: Double = 0.0,
    // Transient property, calculated
    var calculatedDiscountValue: Double = discountAmount.coerceAtMost(originalPrice), // Discount cannot exceed original price
    var finalPrice: Double = (originalPrice - calculatedDiscountValue).coerceAtLeast(0.0) // Final price cannot be negative
) {

    // Convenience to create a copy with updated discount
    fun withUpdatedDiscount(
        newDiscountAmount: Double
    ): ServiceBillingItem1 {
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
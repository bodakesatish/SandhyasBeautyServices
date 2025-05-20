package com.bodakesatish.sandhyasbeautyservices.domain.model


// In a suitable package, e.g., com.bodakesatish.sandhyasbeautyservices.ui.billing
data class ServiceBillingItem(
    val id: String, // Unique ID for the service
    val serviceName: String,
    val originalPrice: Double,
    var discountAmount: Double = 0.0,
    var discountPercentage: Double = 0.0, // 0-100
    var isPercentageDiscount: Boolean = false, // True if discountPercentage is active
    // Transient property, calculated
    var calculatedDiscountValue: Double = if (isPercentageDiscount) {
        originalPrice * (discountAmount / 100.0)
    } else {
        discountAmount
    }.coerceAtMost(originalPrice), // Discount cannot exceed original price
    var finalPrice: Double = (originalPrice - calculatedDiscountValue).coerceAtLeast(0.0) // Final price cannot be negative
) {
    fun hasDiscount(): Boolean = calculatedDiscountValue > 0

    // Convenience to create a copy with updated discount
    fun withUpdatedDiscount(
        newDiscountAmount: Double,
        newDiscountPercentage: Double,
        newIsPercentage: Boolean
    ): ServiceBillingItem {
        val newDis = newDiscountAmount
        val newPercentage = newDiscountPercentage
        val newIsPercentage = newIsPercentage
        val calculatedDiscountValue = if (newIsPercentage) {
            originalPrice * (newDiscountAmount / 100.0)
        } else {
            newDiscountAmount
        }.coerceAtMost(originalPrice)
        val finalPrice =  (originalPrice - calculatedDiscountValue).coerceAtLeast(0.0)
        return this.copy(
            discountAmount = newDis,
            discountPercentage = newPercentage,
            isPercentageDiscount = newIsPercentage,
            calculatedDiscountValue = calculatedDiscountValue,
            finalPrice = finalPrice
        )
    }
}
package com.bodakesatish.sandhyasbeautyservices.ui.billing

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceBillingItem
import java.text.NumberFormat
import java.util.Locale

@HiltViewModel
class BillingViewModel @Inject constructor() : ViewModel() {

    private val tag = this.javaClass.simpleName

    // StateFlow to hold the appointment ID (for edit mode)
    private val _appointmentId = MutableStateFlow<Int?>(null)


    private val _serviceItems = MutableLiveData<List<ServiceBillingItem>>(emptyList())
    val serviceItems: LiveData<List<ServiceBillingItem>> = _serviceItems

    // Overall Bill Details
    val subTotal: LiveData<Double> = _serviceItems.map { items ->
        items.sumOf { it.originalPrice }
    }

    val totalDiscount: LiveData<Double> = _serviceItems.map { items ->
        items.sumOf { it.calculatedDiscountValue }
    }

    val netAmount: LiveData<Double> = _serviceItems.map { items ->
        items.sumOf { it.finalPrice }
    }

    // Dummy data loading - replace with your actual data source (e.g., from appointment)
    init {
        loadDummyServices()
    }

    private fun loadDummyServices() {
        // Replace with actual logic to get selected services for the appointment
        _serviceItems.value = listOf(
            ServiceBillingItem(
                id = "1",
                serviceName = "Haircut and Styling",
                originalPrice = 500.0
            ),
            ServiceBillingItem(id = "2", serviceName = "Manicure", originalPrice = 300.0),
            ServiceBillingItem(id = "3", serviceName = "Pedicure", originalPrice = 400.0),
            ServiceBillingItem(id = "4", serviceName = "Facial", originalPrice = 800.0)
        )
    }

    fun updateServiceDiscount(
        serviceId: String,
        newDiscountAmount: Double,
        newDiscountPercentage: Double,
        isPercentage: Boolean
    ) {
        val currentList = _serviceItems.value ?: return
        val updatedList = currentList.map { item ->
            if (item.id == serviceId) {
                item.withUpdatedDiscount(newDiscountAmount, newDiscountPercentage, isPercentage)
            } else {
                item
            }
        }
        _serviceItems.value = updatedList
    }

    // --- Helper for formatting ---
    fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount) // For INR
    }
}
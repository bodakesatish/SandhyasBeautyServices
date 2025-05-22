package com.bodakesatish.sandhyasbeautyservices.ui.billing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.model.PaymentMode
import com.bodakesatish.sandhyasbeautyservices.domain.model.ServiceDetailWithService
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.FetchAppointmentDetailUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetServiceDetailWithServiceByAppointmentIdUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.UpdateAppointmentDetailUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.UpdateSelectedServicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val getServiceDetailUseCase: GetServiceDetailWithServiceByAppointmentIdUseCase,
    private val fetchAppointmentDetailUseCase: FetchAppointmentDetailUseCase,
    private val updateAppointmentDetailUseCase: UpdateAppointmentDetailUseCase,
    private val updateSelectedServicesUseCase: UpdateSelectedServicesUseCase
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    // StateFlow to hold the appointment ID (for edit mode)
    private val _appointmentId = MutableStateFlow<Int>(0)

    private val _serviceDetailList = MutableStateFlow<List<ServiceDetailWithService>>(emptyList())
    val serviceDetailList: StateFlow<List<ServiceDetailWithService>> =
        _serviceDetailList.asStateFlow()

    private val _appointmentDetail = MutableStateFlow<Appointment?>(null)
    val appointmentDetail: StateFlow<Appointment?> = _appointmentDetail.asStateFlow()

    // SharedFlow for one-time events like saving success/failure
    private val _saveResult = MutableSharedFlow<Boolean>()
    val saveResult: SharedFlow<Boolean> = _saveResult.asSharedFlow()


    init {
        Log.d(tag, "$tag->init")
        // Collect the processingFlow to update the main UI state flow
        viewModelScope.launch {
            serviceDetailList.collect { serviceDetailList ->
                val totalBillAmount = serviceDetailList.sumOf { it.originalPrice }
                val totalDiscount = serviceDetailList.sumOf { it.discountAmount }
                val netTotal = totalBillAmount - totalDiscount
                _appointmentDetail.value = _appointmentDetail.value?.copy(
                    totalBillAmount = totalBillAmount,
                    totalDiscount = totalDiscount,
                    netTotal = netTotal
                )
            }
        }
    }


    fun getServiceDetail() {
        Log.d(tag, "$tag->getServiceDetail")
        viewModelScope.launch(Dispatchers.IO) {

            getServiceDetailUseCase.invoke(_appointmentId.value).collect { list ->
                _serviceDetailList.value = list
                //  _serviceItems.value = list
                Log.d(tag, "In $tag $list")
            }
        }
    }

    fun updateServiceDiscount(
        serviceId: Int,
        newDiscountAmount: Double
    ) {
        val currentList = _serviceDetailList.value
        val updatedList = currentList.map { item ->
            if (item.serviceId == serviceId) {
                item.withUpdatedDiscount(newDiscountAmount)
            } else {
                item
            }
        }
        _serviceDetailList.value = updatedList
    }

    // --- Helper for formatting ---
    fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount) // For INR
    }

    // Call this function from your Fragment/Activity when an appointment is being edited
    fun setAppointmentId(id: Int) {
        Log.d(tag, "Setting appointmentId to: $id")
        _appointmentId.value = id
        getServiceDetail()
        fetchAppointmentDetail()
    }

    fun fetchAppointmentDetail() {
        Log.d(tag, "$tag->fetchAppointmentDetail")
        viewModelScope.launch(Dispatchers.IO) {

            fetchAppointmentDetailUseCase.invoke(_appointmentId.value).collect {
                _appointmentDetail.value = it
                Log.d(tag, "In $tag $it")
            }
        }
    }

    fun updateAppointmentDetail(paymentNotes: String) {
        Log.d(tag, "$tag->updateAppointmentDetail")
        viewModelScope.launch(Dispatchers.IO) {
            _appointmentDetail.value = _appointmentDetail.value?.copy(paymentNotes = paymentNotes.toString())
            _appointmentDetail.value?.let {
                updateAppointmentDetailUseCase.invoke(it)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            updateSelectedServicesUseCase.invoke(_appointmentId.value, serviceDetailList.value)
            // Emit failure on the main dispatcher
            withContext(Dispatchers.Main) {
                _saveResult.emit(true)
            }
        }
    }

    fun updatePaymentMode(paymentMethod: PaymentMode) {
        _appointmentDetail.value = _appointmentDetail.value?.copy(paymentMode = paymentMethod)
    }

    fun updateAppointmentStatus(appointmentStatus: AppointmentStatus) {
        _appointmentDetail.value = _appointmentDetail.value?.copy(appointmentStatus = appointmentStatus)
    }

}
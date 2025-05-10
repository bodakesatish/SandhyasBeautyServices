package com.bodakesatish.sandhyasbeautyservices.ui.appointments

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.AddOrUpdateCategoryUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.CreateNewAppointmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelNewAppointment @Inject constructor(
    private val createNewAppointmentUseCase: CreateNewAppointmentUseCase
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    var category = Category()

    val customerResponse = MutableLiveData<Boolean>()

    init {
        Log.d(tag, "$tag->init")
    }

    fun createNewAppointment() {
        Log.d(tag, "In $tag createNewAppointmentUseCase")
        val customer = Customer(
            id = 1,
            firstName = "Satish",
            lastName = "Bodake",
            age = 25,
            address = "Pune",
            phone = "9876543210"
        )
        val appointment = Appointment(
            id = 1,
            customerId = 1,
            appointmentDate = "2025-05-10",
            appointmentTime = "10:00",
            appointmentPlannedTime = "10:00",
            appointmentCompletedTime = "10:00",
            totalBillAmount = 100.0,
            appointmentStatus = "Completed",
            paymentMode = "Cash"
        )

        val selectedServicesWithDetails = listOf(1, 2)

        viewModelScope.launch(Dispatchers.IO) {
            val id = createNewAppointmentUseCase.invoke(customer, appointment, selectedServicesWithDetails)
            Log.d(tag, "In $tag $id")
            viewModelScope.launch(Dispatchers.Main) {
                customerResponse.value = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(tag , "$tag->onCleared")
    }

}
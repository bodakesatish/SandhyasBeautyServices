package com.bodakesatish.sandhyasbeautyservices.ui.customer

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.AddOrUpdateCustomerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddOrUpdateCustomerViewModel @Inject constructor(
    private val addOrUpdateCustomerUseCase: AddOrUpdateCustomerUseCase
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    var customer = Customer()

    val customerResponse = MutableLiveData<Boolean>()

    init {
        Log.d(tag, "$tag->init")
    }

    fun addOrUpdateCustomer() {
        Log.d(tag, "In $tag addOrUpdatePatient")
        viewModelScope.launch(Dispatchers.IO) {
            val id = addOrUpdateCustomerUseCase.invoke(customer)
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
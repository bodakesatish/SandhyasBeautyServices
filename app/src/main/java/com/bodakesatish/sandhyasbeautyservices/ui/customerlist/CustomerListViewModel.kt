package com.bodakesatish.sandhyasbeautyservices.ui.customerlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCustomerListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val getCustomerListUseCase: GetCustomerListUseCase
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text


    private val tag = this.javaClass.simpleName

    private val _customerList = MutableStateFlow<List<Customer>>(emptyList())
    val patientList: StateFlow<List<Customer>> = _customerList.asStateFlow()

    init {
        Log.d(tag, "$tag->init")
    }

    fun getCustomerList() {
        Log.d(tag, "$tag->getCustomerList")
        viewModelScope.launch(Dispatchers.IO) {

            getCustomerListUseCase.invoke().collect { list ->
                _customerList.value = list
                Log.d(tag, "In $tag $list")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(tag , "$tag->onCleared")
    }
}
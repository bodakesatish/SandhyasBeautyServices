package com.bodakesatish.sandhyasbeautyservices.ui.services

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetServicesListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceListViewModel @Inject constructor(
    private val getServicesListUseCase: GetServicesListUseCase
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text


    private val tag = this.javaClass.simpleName

    private val _serviceList = MutableStateFlow<List<Service>>(emptyList())
    val serviceList: StateFlow<List<Service>> = _serviceList.asStateFlow()

    init {
        Log.d(tag, "$tag->init")
    }

    fun getCategoryList() {
        Log.d(tag, "$tag->getCategoryList")
        viewModelScope.launch(Dispatchers.IO) {

            getServicesListUseCase.invoke().collect { list ->
                _serviceList.value = list
                Log.d(tag, "In $tag $list")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(tag , "$tag->onCleared")
    }
}
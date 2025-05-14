package com.bodakesatish.sandhyasbeautyservices.ui.services

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.AddOrUpdateServiceUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCategoryListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddOrUpdateServiceViewModel @Inject constructor(
    private val addOrUpdateServiceUseCase: AddOrUpdateServiceUseCase,
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    var service = Service()

    val customerResponse = MutableLiveData<Boolean>(false)

    init {
        Log.d(tag, "$tag->init")
    }

    fun addOrUpdateService() {
        Log.d(tag, "In $tag addOrUpdateService")
        viewModelScope.launch(Dispatchers.IO) {
            val id = addOrUpdateServiceUseCase.invoke(service)
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
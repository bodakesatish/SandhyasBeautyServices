package com.bodakesatish.sandhyasbeautyservices.ui.appointments


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Appointment
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetAppointmentListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelAppointmentDashboard @Inject constructor(
    private val getAppointmentListUseCase: GetAppointmentListUseCase
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text


    private val tag = this.javaClass.simpleName

    private val _categoryList = MutableStateFlow<List<Appointment>>(emptyList())
    val appointmentList: StateFlow<List<Appointment>> = _categoryList.asStateFlow()

    init {
        Log.d(tag, "$tag->init")
    }

    fun getAppointmentList() {
        Log.d(tag, "$tag->getCategoryList")
        viewModelScope.launch(Dispatchers.IO) {

            getAppointmentListUseCase.invoke().collect { list ->
                _categoryList.value = list
                Log.d(tag, "In $tag $list")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(tag , "$tag->onCleared")
    }
}
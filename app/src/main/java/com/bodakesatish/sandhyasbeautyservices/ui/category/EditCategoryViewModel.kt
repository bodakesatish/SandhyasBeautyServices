package com.bodakesatish.sandhyasbeautyservices.ui.category

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.AddOrUpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCategoryViewModel @Inject constructor(
    private val addOrUpdateCategoryUseCase: AddOrUpdateCategoryUseCase
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    var category = Category()

    val customerResponse = MutableLiveData<Boolean>()

    init {
        Log.d(tag, "$tag->init")
    }

    fun addOrUpdateCategory() {
        Log.d(tag, "In $tag addOrUpdateCategory")
        viewModelScope.launch(Dispatchers.IO) {
            val id = addOrUpdateCategoryUseCase.invoke(category)
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
package com.bodakesatish.sandhyasbeautyservices.ui.category

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCategoryListUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryListUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null // Or a more specific error type
)

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val getCategoryListUseCase: GetCategoryListUseCase
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    // StateFlow to hold the entire UI state
    private val _uiState = MutableStateFlow(CategoryListUiState())
    val uiState: StateFlow<CategoryListUiState> = _uiState.asStateFlow()

    // Keep _categoryList if you specifically need just the list elsewhere,
    // but uiState.categories will be the primary source for the UI.
    // private val _categoryList = MutableStateFlow<List<Category>>(emptyList())
    // val categoryList: StateFlow<List<Category>> = _categoryList.asStateFlow()

    init {
        Log.d(tag, "$tag->init")
        // Optionally, load categories when ViewModel is created
         getCategoryList()
    }

    fun getCategoryList(forceToRefresh: Boolean = false) {
        Log.d(tag, "$tag->getCategoryList, forceToRefresh: $forceToRefresh")

        viewModelScope.launch { // No need for Dispatchers.IO here, use case should handle it
            // The use case itself is responsible for choosing the correct dispatcher
            // for its internal operations (like network calls or DB access).
            // The ViewModel should primarily collect on Dispatchers.Main (default for viewModelScope unless changed).

            getCategoryListUseCase.invoke(forceToRefresh)
                .collect { networkResult ->
                    Log.d(tag, "Received NetworkResult: $networkResult")
                    when (networkResult) {
                        is NetworkResult.Loading -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = true
                                    // Optionally, clear previous error message on new load
                                    // userMessage = null
                                )
                            }
                        }
                        is NetworkResult.Success -> {
                            //  _categoryList.value = networkResult.data ?: emptyList() // If you keep separate _categoryList
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    categories = networkResult.data ?: emptyList(),
                                    errorMessage = null // Clear any previous error
                                )
                            }
                            Log.d(tag, "Success: ${networkResult.data?.size ?: 0} categories")
                        }
                        is NetworkResult.Error -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    // Keep existing categories in UI on error, or clear them:
                                    // categories = if (currentState.categories.isNotEmpty()) currentState.categories else emptyList(),
                                    errorMessage = networkResult.message ?: "An unexpected error occurred."
                                )
                            }
                            Log.e(tag, "Error: ${networkResult.message}, Code: ${networkResult.code}, Exception: ${networkResult.exception}")
                        }
                        is NetworkResult.NoInternet -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    errorMessage = "No internet connection."
                                )
                            }
                        }
                    }
                }
        }
    }

    /**
     * Call this method if the user explicitly dismisses an error message.
     */
    fun userMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(tag, "$tag->onCleared")
    }
}
package com.bodakesatish.sandhyasbeautyservices.ui.category

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.di.DefaultDispatcher
import com.bodakesatish.sandhyasbeautyservices.di.IoDispatcher
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.AddOrUpdateCategoryUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 1. Define a UI State data class
data class EditCategoryUiState(
    val categoryName: String = "",
    val categoryNameError: String? = null,
    val isLoading: Boolean = false,
    val saveResult: CategorySaveResult = CategorySaveResult.Idle, // For success/failure/error message
    val isEditMode: Boolean = false,
    val headerText: String = "Add Category" // Or derive based on isEditMode
)

// 2. Define an enum/sealed class for save results
sealed class CategorySaveResult {
    object Idle : CategorySaveResult()
    data class Success(val categoryId: String) : CategorySaveResult()
    data class Error(val message: String) : CategorySaveResult()
}

@HiltViewModel
class EditCategoryViewModel @Inject constructor(
    private val addOrUpdateCategoryUseCase: AddOrUpdateCategoryUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val mainDispatcher: CoroutineDispatcher // Provide default
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    private val _uiState = MutableStateFlow(EditCategoryUiState())
    val uiState: StateFlow<EditCategoryUiState> = _uiState.asStateFlow()

    private var editingCategoryId: String? = null // To store the ID if in edit mode

    init {
        Log.d(tag, "$tag->init block")
    }

    // 4. Method to initialize ViewModel for editing an existing category
    fun loadCategoryForEdit(category: Category) {
        editingCategoryId = category.firestoreDocId
        _uiState.update {
            it.copy(
                categoryName = category.categoryName,
                isEditMode = true,
                headerText = "Edit Category",
                categoryNameError = null // Clear any previous errors
            )
        }
        Log.d(tag, "Loaded category for edit: $category")
    }

    // 5. Method to update category name from UI input
    fun onCategoryNameChanged(name: String) {
        _uiState.update {
            it.copy(
                categoryName = name,
                categoryNameError = if (name.isNotBlank() && it.categoryNameError != null) null else it.categoryNameError, // Clear error if user starts typing valid input
                saveResult = CategorySaveResult.Idle // Reset save result on input change
            )
        }
    }

    // 6. Refactored addOrUpdateCategory
    fun saveCategory() {
        val currentCategoryName = _uiState.value.categoryName.trim()

        if (currentCategoryName.isBlank()) {
            _uiState.update {
                it.copy(categoryNameError = "Category name cannot be empty.")
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, categoryNameError = null) }
        Log.d(tag, "Attempting to save category: $currentCategoryName, ID: $editingCategoryId")

        viewModelScope.launch(mainDispatcher) { // Use injected main dispatcher
            try {
                // Perform the domain operation on the IO dispatcher
                val resultId = withContext(ioDispatcher) { // Use injected IO dispatcher
                    addOrUpdateCategoryUseCase.invoke(
                        Category(
                            firestoreDocId = editingCategoryId ?: "", // Use 0 or appropriate default for new category
                            categoryName = currentCategoryName
                        )
                    )
                }
                Log.d(tag, "Category saved/updated successfully. ID: $resultId")
                _uiState.update {
                    val response = resultId as NetworkResult.Success
                    it.copy(
                        isLoading = false,
                        saveResult = CategorySaveResult.Success(response.data)
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "Error saving category", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveResult = CategorySaveResult.Error(e.message ?: "An unknown error occurred")
                    )
                }
            }
        }
    }

    // Optional: Method to reset saveResult to Idle, useful for navigation triggers
    fun consumeSaveResult() {
        _uiState.update { it.copy(saveResult = CategorySaveResult.Idle) }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(tag, "$tag->onCleared")
    }

}
package com.bodakesatish.sandhyasbeautyservices.compose.category

import com.bodakesatish.sandhyasbeautyservices.compose.R
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.compose.AppDestinations
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.AddOrUpdateCategoryUseCase
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCategoryByIdUseCase // Assuming you have this
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditCategoryFormFieldState(
    val value: String = "",
    val errorResId: Int? = null
)

data class EditCategoryUiState(
    val screenTitleResId: Int = R.string.screen_title_add_category,
    val isEditMode: Boolean = false,
    val categoryName: EditCategoryFormFieldState = EditCategoryFormFieldState(),
    val description: EditCategoryFormFieldState = EditCategoryFormFieldState(), // If it needs validation
    val imageUrl: EditCategoryFormFieldState = EditCategoryFormFieldState(),   // If it needs validation
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false, // For save button state
)

sealed class EditCategoryEffect {
    data class ShowSnackbar(val messageResId: Int, val isError: Boolean = false) : EditCategoryEffect()
    object NavigateBack : EditCategoryEffect()
}

@HiltViewModel
class EditCategoryViewModel @Inject constructor(
    private val addOrUpdateCategoryUseCase: AddOrUpdateCategoryUseCase,
    private val getCategoryByIdUseCase: GetCategoryByIdUseCase, // For loading data in edit mode
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditCategoryUiState())
    val uiState: StateFlow<EditCategoryUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<EditCategoryEffect>()
    val effect: SharedFlow<EditCategoryEffect> = _effect.asSharedFlow()

    private var currentCategoryId: Int? = null

    init {
        currentCategoryId = savedStateHandle[AppDestinations.CategoryFeature.CATEGORY_ID_ARG] // Get from Nav Args

        if (currentCategoryId != null && currentCategoryId != 0) { // Assuming 0 or no arg means "add"
            _uiState.update { it.copy(isEditMode = true, screenTitleResId = R.string.screen_title_edit_category) }
            loadCategoryDetails(currentCategoryId!!)
        } else {
            _uiState.update { it.copy(isEditMode = false, screenTitleResId = R.string.screen_title_add_category) }
            // For add mode, fields are already default empty
        }
    }

    private fun loadCategoryDetails(categoryId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val category = getCategoryByIdUseCase(categoryId)
                if (category != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            categoryName = it.categoryName.copy(value = category.categoryName),
                            description = it.description.copy(value = category.categoryDescription ?: ""),
                          //  imageUrl = it.imageUrl.copy(value = category.imageUrl ?: "")
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) } // Keep fields empty or show error
                    _effect.emit(EditCategoryEffect.ShowSnackbar(R.string.error_category_not_found, isError = true))
                    _effect.emit(EditCategoryEffect.NavigateBack) // Optionally navigate back if category not found
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _effect.emit(EditCategoryEffect.ShowSnackbar(R.string.error_loading_category_details, isError = true))
            }
        }
    }

    fun onCategoryNameChanged(name: String) {
        _uiState.update {
            it.copy(categoryName = it.categoryName.copy(value = name, errorResId = null))
        }
    }

    fun onDescriptionChanged(desc: String) {
        _uiState.update { it.copy(description = it.description.copy(value = desc)) } // Add validation if needed
    }

    fun onImageUrlChanged(url: String) {
        _uiState.update { it.copy(imageUrl = it.imageUrl.copy(value = url)) } // Add validation if needed
    }

    fun onSaveClicked() {
        val name = _uiState.value.categoryName.value.trim()

        if (name.isBlank()) {
            _uiState.update {
                it.copy(categoryName = it.categoryName.copy(errorResId = R.string.error_category_name_required))
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true) }

        val categoryToSave = Category(
            id = if (_uiState.value.isEditMode) currentCategoryId!! else 0, // Or handle new ID generation in UseCase/Repo
            categoryName = name,
            categoryDescription = _uiState.value.description.value.trim(),
         //   imageUrl = _uiState.value.imageUrl.value.trim()
        )

        viewModelScope.launch {
            try {
                addOrUpdateCategoryUseCase(categoryToSave)
                val successMessage = if (_uiState.value.isEditMode) R.string.category_updated_success else R.string.category_added_success
                _effect.emit(EditCategoryEffect.ShowSnackbar(successMessage))
                _effect.emit(EditCategoryEffect.NavigateBack)
            } catch (e: Exception) {
                _effect.emit(EditCategoryEffect.ShowSnackbar(R.string.error_saving_category, isError = true))
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }
}

//Changes in EditCategoryViewModel.kt:•EditCategoryFormFieldState: Represents state for each input field (value, error).•EditCategoryUiState: Clearer state for loading initial data vs. submitting form.•EditCategoryEffect: For snackbars and navigation.•Initialization Logic: Clearly distinguishes add vs. edit mode based on categoryId from SavedStateHandle.•loadCategoryDetails: Fetches category data for editing.•Input Handling: on...Changed methods update the respective form field states.•onSaveClicked:•Performs validation.•Updates isSubmitting state.•Calls use case and emits success/error effects and navigation effect.
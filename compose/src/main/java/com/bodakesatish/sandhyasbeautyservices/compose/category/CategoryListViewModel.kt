package com.bodakesatish.sandhyasbeautyservices.compose.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.compose.R
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.GetCategoryListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the UI state for the category list screen.
 *
 * @property isLoading Indicates whether the data is currently being loaded.
 * @property categories The list of categories to be displayed.
 * @property errorMessageResId The resource ID of a persistent error message to be shown on the screen, if any.
 */

data class CategoryListUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val errorMessageResId: Int? = null // For persistent errors on screen
)

/**
 * Represents one-time events that can occur on the category list screen.
 * These are typically used for actions like showing a snackbar or navigating to another screen.
 */
sealed class CategoryListEffect { // For one-time events like navigation, snackbars
    data class ShowSnackbar(val messageResId: Int) : CategoryListEffect()
    data class NavigateToEditCategory(val categoryId: Int) : CategoryListEffect()
    object NavigateToAddCategory : CategoryListEffect()
}

/**
 * ViewModel for the Category List screen.
 *
 * This ViewModel is responsible for managing the UI state and business logic
 * related to displaying a list of categories. It interacts with use cases
 * to fetch and manipulate category data.
 *
 * It exposes:
 * - `uiState`: A [StateFlow] of [CategoryListUiState] representing the current state of the UI,
 *   including loading status, the list of categories, and any error messages.
 * - `effect`: A [SharedFlow] of [CategoryListEffect] for emitting one-time events
 *   like navigation requests or snackbar messages.
 *
 * Key functionalities include:
 * - Loading the list of categories, with an option to force a refresh.
 * - Handling user interactions such as clicking on a category to edit it or
 *   clicking the "add category" button.
 *
 * @property getCategoryListUseCase Use case for retrieving the list of categories.
 *                                  // Example: @property deleteCategoryUseCase Use case for deleting a category.
 */
@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val getCategoryListUseCase: GetCategoryListUseCase,
    // private val deleteCategoryUseCase: DeleteCategoryUseCase // Example
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryListUiState())
    val uiState: StateFlow<CategoryListUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CategoryListEffect>()
    val effect: SharedFlow<CategoryListEffect> = _effect.asSharedFlow()

    init {
        loadCategories()
    }

    /**
     * Loads the list of categories.
     *
     * This function initiates the process of fetching categories. It updates the UI state to indicate
     * loading, then calls the `getCategoryListUseCase` to retrieve the data.
     *
     * A delay of 2500ms is introduced at the start of the flow for demonstration or to simulate network latency.
     *
     * On successful retrieval, the UI state is updated with the fetched categories and loading is set to false.
     *
     * If an error occurs during the fetching process, the UI state is updated to reflect the error,
     * setting `isLoading` to false and providing an error message resource ID. Additionally, a transient
     * snackbar effect is emitted to inform the user about the error.
     *
     * @param forceRefresh A boolean indicating whether to force a refresh of the categories.
     *                     Defaults to `false`. This can be triggered by actions like pull-to-refresh.
     *                     Currently, this parameter is not directly used by the `getCategoryListUseCase`
     *                     in this implementation but is available for future enhancements.
     */
    fun loadCategories(forceRefresh: Boolean = false) { // forceRefresh can be triggered by pull-to-refresh
        _uiState.update { it.copy(isLoading = true, errorMessageResId = null) }

        getCategoryListUseCase() // Assuming it takes no params or forceRefresh
            .onStart {
                delay(2500)
            }
            .onEach { categories ->

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        categories = categories
                    )
                }
            }
            .catch { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessageResId = R.string.error_loading_categories // Generic error
                    )
                }
                // Optionally emit a snackbar effect too for transient errors
                _effect.emit(CategoryListEffect.ShowSnackbar(R.string.error_loading_categories_transient))
            }
            .launchIn(viewModelScope)
    }

    fun onAddCategoryClicked() {
        viewModelScope.launch {
            _effect.emit(CategoryListEffect.NavigateToAddCategory)
        }
    }

    fun onCategoryClicked(categoryId: Int) {
        viewModelScope.launch {
            _effect.emit(CategoryListEffect.NavigateToEditCategory(categoryId))
        }
    }

    fun onSnackbarShown() {
        // If your snackbar is purely driven by effect, you might not need to clear anything here
        // unless uiState also holds a snackbar message.
        // For this example, we're assuming errorMessageResId is for persistent on-screen messages
        // and ShowSnackbar effect is for transient snackbars.
    }

    // Example: Delete Category
    // fun onDeleteCategoryClicked(categoryId: Int) {
    //     viewModelScope.launch {
    //         _uiState.update { it.copy(isLoading = true) } // Indicate loading
    //         try {
    //             deleteCategoryUseCase(categoryId)
    //             _effect.emit(CategoryListEffect.ShowSnackbar(R.string.category_deleted_success))
    //             // Reloading the list will happen if your use case updates the underlying data source
    //             // or you can explicitly call loadCategories()
    //         } catch (e: Exception) {
    //             _effect.emit(CategoryListEffect.ShowSnackbar(R.string.error_deleting_category))
    //         } finally {
    //              _uiState.update { it.copy(isLoading = false) }
    //         }
    //     }
    // }
}

//Changes in CategoryListViewModel.kt:•Clear CategoryListUiState: Holds only data necessary for rendering the permanent state of the screen.•CategoryListEffect Sealed Class: For one-time events (navigation, snackbars). This helps decouple ViewModel from direct NavController calls and makes testing easier. The UI observes these effects.•Error Handling: Differentiates between persistent on-screen errors (errorMessageResId in uiState) and transient snackbar messages (via effect).•Event Methods: onAddCategoryClicked, onCategoryClicked emit navigation effects.•loadCategories: More robust, with forceRefresh and clearer loading/error state updates.
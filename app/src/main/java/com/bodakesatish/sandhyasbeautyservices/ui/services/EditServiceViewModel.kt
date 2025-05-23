package com.bodakesatish.sandhyasbeautyservices.ui.services

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.usecases.AddOrUpdateServiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.isBlank

// --- Sealed class for Save Operation Result ---
sealed class SaveResult {
    object Loading : SaveResult()
    object Success : SaveResult()
    data class Error(val message: String) : SaveResult()
    object Idle : SaveResult() // Initial state or after error is handled
}

// --- Sealed class for Navigation Events (Single-shot events) ---
sealed class NavigationCommand {
    object NavigateBack : NavigationCommand()
    // Add other navigation commands if needed
}

// --- UI State Data Class ---
data class EditServiceUiState(
    val serviceName: String = "",
    val servicePrice: String = "", // Keep as String for direct EditText binding/update
    val currentCategory: Category? = null,
    val initialServiceId: Int? = null, // To know if it's an edit or new service
    val isEditMode: Boolean = false,
    val headerText: String = "Add Service", // Dynamically set
    val serviceNameError: String? = null,
    val servicePriceError: String? = null,
    val saveResult: SaveResult = SaveResult.Idle
)

@HiltViewModel
class EditServiceViewModel @Inject constructor(
    private val addOrUpdateServiceUseCase: AddOrUpdateServiceUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO // Provide default
    // Inject other use cases or repositories if needed
    // If you were injecting the DefaultDispatcher too:
    // @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val tag = this.javaClass.simpleName

    private val _uiState = MutableStateFlow(EditServiceUiState())
    val uiState: StateFlow<EditServiceUiState> = _uiState.asStateFlow()

    private val _navigationCommands = Channel<NavigationCommand>()
    val navigationCommands = _navigationCommands.receiveAsFlow() // Expose as Flow

    init {
        Log.d(tag, "$tag->init")
    }

    fun setCategory(category: Category) {
        _uiState.update { currentState ->
            currentState.copy(
                currentCategory = category,
                headerText = if (currentState.isEditMode) "Update service in ${category.categoryName}"
                else "Add your service to ${category.categoryName}"
            )
        }
    }

    fun setService(service: Service) {
        // This is called when editing an existing service
        _uiState.update { currentState ->
            val categoryName = currentState.currentCategory?.categoryName  ?: "" // Fallback category name if needed
            currentState.copy(
                initialServiceId = service.id,
                serviceName = service.serviceName,
                servicePrice = service.servicePrice.toString(),
                isEditMode = true,
                headerText = "Update service in $categoryName"
            )
        }
    }

    fun onServiceNameChanged(name: String) {
        _uiState.update {
            it.copy(
                serviceName = name,
                serviceNameError = if (name.isNotBlank()) null else it.serviceNameError // Clear error if input becomes valid
            )
        }
    }

    fun onServicePriceChanged(price: String) {
        _uiState.update {
            it.copy(
                servicePrice = price,
                servicePriceError = if (price.isNotBlank() && price.toDoubleOrNull() != null) null else it.servicePriceError // Clear error if input becomes valid
            )
        }
    }


    fun addOrUpdateService() {
        Log.d(tag, "In $tag addOrUpdateService")
        val currentState = _uiState.value
        var hasError = false

        if (currentState.serviceName.isBlank()) {
            _uiState.update { it.copy(serviceNameError = "Service name cannot be empty") }
            hasError = true
        } else {
            _uiState.update { it.copy(serviceNameError = null) }
        }

        val priceDouble = currentState.servicePrice.toDoubleOrNull()
        if (currentState.servicePrice.isBlank()) {
            _uiState.update { it.copy(servicePriceError = "Service price cannot be empty") }
            hasError = true
        } else if (priceDouble == null || priceDouble <= 0) {
            _uiState.update { it.copy(servicePriceError = "Please enter a valid positive price") }
            hasError = true
        } else {
            _uiState.update { it.copy(servicePriceError = null) }
        }

        if (currentState.currentCategory == null || currentState.currentCategory.id == 0) {
            // This case should ideally be prevented by UI flow, but good to handle
            _uiState.update { it.copy(saveResult = SaveResult.Error("Category not selected.")) }
            hasError = true
        }


        if (hasError) {
            _uiState.update { it.copy(saveResult = SaveResult.Idle) } // Reset save state if validation fails early
            return
        }

        _uiState.update { it.copy(saveResult = SaveResult.Loading) }

        viewModelScope.launch(ioDispatcher) { // Use IO dispatcher for network/DB
            try {
                val serviceToSave = Service(
                    id = currentState.initialServiceId ?: 0, // 0 or a specific indicator for new
                    serviceName = currentState.serviceName,
                    servicePrice = priceDouble!!, // Already validated not to be null
                    categoryId = currentState.currentCategory!!.id // Already validated not to be null
                    // categoryNameFromArgs can be removed if category object is always present
                )

                val resultCount = addOrUpdateServiceUseCase.invoke(serviceToSave) // Assuming use case returns count or ID

                if (resultCount > 0) { // Or check for specific success condition from use case
                    _uiState.update { it.copy(saveResult = SaveResult.Success) }
                    _navigationCommands.send(NavigationCommand.NavigateBack)
                } else {
                    _uiState.update { it.copy(saveResult = SaveResult.Error("Failed to save service. Please try again.")) }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error saving service: ${e.message}", e)
                _uiState.update { it.copy(saveResult = SaveResult.Error(e.message ?: "An unexpected error occurred.")) }
            }
        }
    }

    /**
     * Call this from the Fragment after an error message from saveResult has been displayed
     * to reset it to Idle, preventing it from being shown again on config change.
     */
    fun errorShown() {
        _uiState.update { it.copy(saveResult = SaveResult.Idle) }
    }

    override fun onCleared() {
        super.onCleared()
        _navigationCommands.close() // Close the channel when ViewModel is cleared
        Log.i(tag, "$tag->onCleared")
    }

}
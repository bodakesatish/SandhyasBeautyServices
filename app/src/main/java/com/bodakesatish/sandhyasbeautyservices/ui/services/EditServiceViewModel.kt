package com.bodakesatish.sandhyasbeautyservices.ui.services

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodakesatish.sandhyasbeautyservices.di.IoDispatcher
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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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
        // Log.d(tag, "$tag->init")
    }

    fun setCategory(category: Category) {
        _uiState.update { currentState ->
            val header = if (currentState.isEditMode) {
                "Update service in ${category.categoryName}"
            } else {
                "Add your service to ${category.categoryName}"
            }
            currentState.copy(
                currentCategory = category,
                headerText = header
            )
        }
    }

    fun setService(service: Service) {
        // This is called when editing an existing service
        _uiState.update { currentState ->
            // Logic to determine header text based on existing category and new service
            val header = if (currentState.currentCategory != null) {
                "Update service in ${currentState.currentCategory.categoryName}"
            } else {
                "Update service" // Fallback, though category should ideally be set first or with service
            }
            currentState.copy(
                initialServiceId = service.id,
                serviceName = service.serviceName,
                servicePrice = service.servicePrice.toString(),
                isEditMode = true,
                // currentCategory = service.category, // If service has category info
                headerText = header // Ensure this is updated
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
//        Log.d(tag, "In $tag addOrUpdateService")
        val currentServiceName = _uiState.value.serviceName
        val currentServicePrice = _uiState.value.servicePrice
        val currentCategory = _uiState.value.currentCategory

        if (currentCategory == null) {
            // Consider setting an error or logging
            _uiState.update {
                it.copy(
                    saveResult = SaveResult.Error("Category not selected.")
                )
            }
            return
        }
        // Initialize nameError by calling the validation function
        val nameError = validateServiceName(currentServiceName)
        val priceError =
            validateServicePrice(currentServicePrice) // Assuming you've implemented validateServicePrice

        // --- CRITICAL STATE UPDATE FOR VALIDATION ERRORS ---
        // This update MUST happen if there's any validation error.
        if (nameError != null || priceError != null) {
            _uiState.update {
                it.copy(
                    serviceNameError = nameError,
                    servicePriceError = priceError,
                    saveResult = SaveResult.Idle // Or Error, ensure it's not Success/Loading
                )
            }
            return // Stop further processing if validation fails
        }


        if (nameError != null || priceError != null) {
            _uiState.update { it.copy(saveResult = SaveResult.Idle) }
            // No need to update saveResult again if done above, but ensure it's Idle.
            // _uiState.update { it.copy(saveResult = SaveResult.Idle) } // Can be removed if handled in the previous update
            return // Validation failed
        }

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

        // If validation passes, proceed to save
        _uiState.update {
            it.copy(
                serviceNameError = null, // Clear previous errors
                servicePriceError = null, // Clear previous errors
                saveResult = SaveResult.Loading
            )
        }
        viewModelScope.launch(ioDispatcher) { // Use IO dispatcher for network/DB
            try {
                val serviceToSave = Service(
                    id = currentState.initialServiceId ?: 0, // 0 or a specific indicator for new
                    serviceName = currentState.serviceName,
                    servicePrice = priceDouble!!, // Already validated not to be null
                    categoryId = currentState.currentCategory!!.id // Already validated not to be null
                    // categoryNameFromArgs can be removed if category object is always present
                )

                val resultCount =
                    addOrUpdateServiceUseCase.invoke(serviceToSave) // Assuming use case returns count or ID

                if (resultCount > 0) { // Or check for specific success condition from use case
                    _uiState.update { it.copy(saveResult = SaveResult.Success) }
                    _navigationCommands.send(NavigationCommand.NavigateBack)
                } else {
                    _uiState.update { it.copy(saveResult = SaveResult.Error("Failed to save service. Please try again.")) }
                }
            } catch (e: Exception) {
                // Log.e(tag, "Error saving service: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        saveResult = SaveResult.Error(
                            e.message ?: "An unexpected error occurred."
                        )
                    )
                }
            }
        }
    }

    private fun validateServiceName(name: String): String? {
        return if (name.isBlank()) "Service name cannot be empty" else null // This should be triggered
    }

    private fun validateServicePrice(price: String): String? {
        if (price.isBlank()) {
            return "Service price cannot be empty"
        }
        val priceDouble = price.toDoubleOrNull()
        if (priceDouble == null) {
            return "Please enter a valid numeric price"
        }
        if (priceDouble <= 0) {
            return "Please enter a positive price"
        }
        return null // Price is valid
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

    fun onSaveComplete() {
        _uiState.update { currentState ->
            currentState.copy(saveResult = SaveResult.Idle)
        }
    }

}
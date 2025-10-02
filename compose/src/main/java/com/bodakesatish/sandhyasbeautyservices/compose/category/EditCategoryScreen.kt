package com.bodakesatish.sandhyasbeautyservices.compose.category
//package com.bodakesatish.sandhyasbeautyservices.ui.editcategory // New package

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bodakesatish.sandhyasbeautyservices.compose.R
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryScreen(
    viewModel: EditCategoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit // This lambda will be called by the back button
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is EditCategoryEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(effect.messageResId),
                        duration = SnackbarDuration.Short
                        // You could customize snackbar based on effect.isError
                    )
                }

                is EditCategoryEffect.NavigateBack -> {
                    onNavigateBack() // ViewModel can also trigger navigation back
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(uiState.screenTitleResId)) },
                // Add navigation icon for back if not handled by system
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Call onNavigateBack when icon is clicked
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back) // Content description for accessibility
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            EditCategoryForm(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                onCategoryNameChanged = viewModel::onCategoryNameChanged,
                onDescriptionChanged = viewModel::onDescriptionChanged,
                onImageUrlChanged = viewModel::onImageUrlChanged,
                onSaveClicked = viewModel::onSaveClicked
            )
        }
    }
}

@Composable
private fun EditCategoryForm(
    modifier: Modifier = Modifier,
    uiState: EditCategoryUiState,
    onCategoryNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onImageUrlChanged: (String) -> Unit,
    onSaveClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Make form scrollable
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ValidatedTextField(
            labelResId = R.string.field_label_category_name,
            fieldState = uiState.categoryName,
            onValueChanged = onCategoryNameChanged
        )

        ValidatedTextField(
            labelResId = R.string.field_label_description,
            fieldState = uiState.description,
            onValueChanged = onDescriptionChanged,
            singleLine = false,
            minLines = 3
        )

        ValidatedTextField(
            labelResId = R.string.field_label_image_url,
            fieldState = uiState.imageUrl,
            onValueChanged = onImageUrlChanged
        )

        Spacer(modifier = Modifier.weight(1f)) // Pushes button to bottom if content is short

        Button(
            onClick = onSaveClicked,
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.button_save_category))
            }
        }
    }
}

@Composable
private fun ValidatedTextField(
    labelResId: Int,
    fieldState: EditCategoryFormFieldState,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = fieldState.value,
            onValueChange = onValueChanged,
            label = { Text(stringResource(labelResId)) },
            isError = fieldState.errorResId != null,
            singleLine = singleLine,
            minLines = minLines,
            modifier = Modifier.fillMaxWidth()
        )
        fieldState.errorResId?.let {
            Text(
                text = stringResource(it),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun EditCategoryScreenPreview_Add() {
    MaterialTheme {
        EditCategoryForm(
            uiState = EditCategoryUiState(screenTitleResId = R.string.screen_title_add_category),
            onCategoryNameChanged = {},
            onDescriptionChanged = {},
            onImageUrlChanged = {},
            onSaveClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditCategoryScreenPreview_Edit() {
    MaterialTheme {
        EditCategoryForm(
            uiState = EditCategoryUiState(
                isEditMode = true,
                screenTitleResId = R.string.screen_title_edit_category,
                categoryName = EditCategoryFormFieldState("Existing Category")
            ),
            onCategoryNameChanged = {},
            onDescriptionChanged = {},
            onImageUrlChanged = {},
            onSaveClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditCategoryScreenPreview_WithError() {
    MaterialTheme {
        EditCategoryForm(
            uiState = EditCategoryUiState(
                categoryName = EditCategoryFormFieldState(
                    "Test",
                    R.string.error_category_name_required
                )
            ),
            onCategoryNameChanged = {},
            onDescriptionChanged = {},
            onImageUrlChanged = {},
            onSaveClicked = {}
        )
    }
}

//Changes in EditCategoryScreen.kt:•Observes EditCategoryEffect: Handles snackbars and navigation.•EditCategoryForm private composable: Extracts form UI.•ValidatedTextField private composable: Reusable component for text fields with validation error display.•Handles initial loading state before showing the form.•Shows progress indicator on the save button when isSubmitting.•Improved Previews.
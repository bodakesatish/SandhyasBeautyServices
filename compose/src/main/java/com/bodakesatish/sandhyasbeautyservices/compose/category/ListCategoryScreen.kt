package com.bodakesatish.sandhyasbeautyservices.compose.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListCategoryScreen(
    viewModel: CategoryListViewModel = hiltViewModel(),
    onNavigateToEditCategory: (categoryId: Int) -> Unit,
    onNavigateToAddCategory: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Observe effects for navigation and snackbars
    LaunchedEffect(key1 = Unit) { // Use key1 = Unit for one-time setup
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CategoryListEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(effect.messageResId),
                        duration = SnackbarDuration.Short
                    )
                    viewModel.onSnackbarShown() // Notify ViewModel if needed
                }
                is CategoryListEffect.NavigateToEditCategory -> {
                    onNavigateToEditCategory(effect.categoryId)
                }
                is CategoryListEffect.NavigateToAddCategory -> {
                    onNavigateToAddCategory()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.screen_title_categories)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onAddCategoryClicked) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.fab_desc_add_category))
            }
        }
    ) { paddingValues ->
        CategoryListContent(
            modifier = Modifier.padding(paddingValues),
            isLoading = uiState.isLoading,
            categories = uiState.categories,
            errorMessageResId = uiState.errorMessageResId,
            onCategoryClicked = viewModel::onCategoryClicked,
            onRefresh = { viewModel.loadCategories(forceRefresh = true) }
        )
    }
}

@Composable
private fun CategoryListContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    categories: List<Category>,
    errorMessageResId: Int?,
    onCategoryClicked: (categoryId: Int) -> Unit,
    onRefresh: () -> Unit // For pull-to-refresh
) {
    // Consider adding PullToRefreshBox here
    // val pullRefreshState = rememberPullToRefreshState()
    // if (pullRefreshState.isRefreshing) {
    //    LaunchedEffect(true) {
    //        onRefresh()
    //        pullRefreshState.endRefresh()
    //    }
    // }
    // PullToRefreshBox(state = pullRefreshState, onRefresh = onRefresh, isRefreshing = isLoading && categories.isEmpty() ) { ... }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading && categories.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (categories.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryItem(
                        category = category,
                        onItemClick = { onCategoryClicked(category.id) }
                    )
                }
            }
        } else if (!isLoading && errorMessageResId == null) {
            Text(
                text = stringResource(R.string.no_categories_available),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }

        errorMessageResId?.let {
            Text(
                text = stringResource(id = it),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp) // Or bottom of screen
            )
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = category.categoryName, style = MaterialTheme.typography.titleMedium)
            // Consider adding a subtle arrow or chevron icon for affordance
        }
    }
}

// Previews (Consider making them more robust with fake ViewModels if needed)
@Preview(showBackground = true)
@Composable
fun ListCategoryScreenPreview_Loading() {
    MaterialTheme {
        CategoryListContent(
            isLoading = true, categories = emptyList(), errorMessageResId = null,
            onCategoryClicked = {}, onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCategoryScreenPreview_WithData() {
    MaterialTheme {
        CategoryListContent(
            isLoading = false,
            categories = listOf(
                Category(1, "Facials",  "Desc"),
                Category(2, "Haircuts",  "Desc")
            ),
            errorMessageResId = null,
            onCategoryClicked = {}, onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCategoryScreenPreview_Empty() {
    MaterialTheme {
        CategoryListContent(
            isLoading = false, categories = emptyList(), errorMessageResId = null,
            onCategoryClicked = {}, onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCategoryScreenPreview_Error() {
    MaterialTheme {
        CategoryListContent(
            isLoading = false, categories = emptyList(), errorMessageResId = R.string.error_loading_categories,
            onCategoryClicked = {}, onRefresh = {}
        )
    }
}

//Changes in ListCategoryScreen.kt:•Observes CategoryListEffect: Handles navigation and snackbars based on effects from ViewModel.•Clearer Callbacks: onNavigateToEditCategory and onNavigateToAddCategory are passed from the NavHost.•CategoryListContent private composable: Extracts the main content logic for better organization and previewability.•Improved Preview States: Added more previews for different UI states.•Pull-to-Refresh: Hinted at where to add PullToRefreshBox.•Passes user actions (clicks, refresh) directly to ViewModel methods.
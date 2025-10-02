package com.bodakesatish.sandhyasbeautyservices.ui.category
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import com.bodakesatish.sandhyasbeautyservices.domain.usecases.AddOrUpdateCategoryUseCase
//import com.bodakesatish.sandhyasbeautyservices.util.MainCoroutineRule // Your TestCoroutineRule
//import dagger.hilt.android.testing.BindValue
//import dagger.hilt.android.testing.HiltAndroidRule
//import dagger.hilt.android.testing.HiltAndroidTest
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.TestDispatcher
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.mockito.Mockito.mock
//import javax.inject.Inject
//
//@ExperimentalCoroutinesApi
//@HiltAndroidTest
//class EditCategoryViewModelTest {
//
//    @get:Rule(order = 0) // Hilt rule should run first
//    var hiltRule = HiltAndroidRule(this)
//
//    @get:Rule(order = 1)
//    var instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    @get:Rule(order = 2)
//    var mainCoroutineRule = MainCoroutineRule() // Still crucial for Dispatchers.Main and test scheduler
//
//    // 2. ViewModel can be @Inject'ed if Hilt can construct it (dependencies are mockable/provided)
//    //    However, UseCases are often mocked, so manual instantiation after Hilt setup is common.
//    //    Let's stick to mocking the UseCase and instantiating ViewModel manually,
//    //    but Hilt *will* provide the dispatchers *inside* the ViewModel.
//   // private lateinit var viewModel: EditCategoryViewModel
//    //private lateinit var mockAddOrUpdateUseCase: AddOrUpdateCategoryUseCase
//
//    // If your TestDispatcherModule provides the dispatchers directly (e.g. @IoDispatcher),
//    // and EditCategoryViewModel injects them like that, this is how Hilt works.
//    // The viewModel instance will get these test dispatchers automatically.
//
//    // Now, in your test, bind the TestDispatcher from your rule:
//    @BindValue // This Hilt annotation replaces any existing binding for TestDispatcher
//    @JvmField // Required for @BindValue on fields in Kotlin
//    val testDispatcher: TestDispatcher = mainCoroutineRule.testDispatcher
//    // NOTE: @BindValue fields are initialized *before* @Before.
//    // So mainCoroutineRule.testDispatcher must be available when this field is initialized.
//    // Rules are initialized before @Before, so this usually works.
//
//    // Let's refine setUp for full Hilt power:
//
//    // Bind the mock use case with Hilt as well
//    @BindValue
//    @JvmField
//    val mockedUseCase: AddOrUpdateCategoryUseCase = mock()
//    // Remove: private lateinit var mockAddOrUpdateUseCase: AddOrUpdateCategoryUseCase
//
//    @Inject // Hilt will now create the ViewModel and inject mocks/test dispatchers
//    lateinit var viewModel: EditCategoryViewModel
//
//
//    @Before
//    fun setUp() {
//        // mainCoroutineRule.testDispatcher is already bound by @BindValue testDispatcher field
//        // mockedUseCase is already bound by @BindValue mockedUseCase field
//        hiltRule.inject() // This injects `viewModelInjectedByHilt` and other @Inject fields
//        // and makes sure all @BindValue fields are correctly bound in Hilt's graph.
//
//        // Use the Hilt-injected ViewModel for tests
//     //   viewModel = viewModelInjectedByHilt
//
//    // Correction: If the ViewModel is defined with @Inject constructor,
//        // Hilt would attempt to create it if you @Inject it.
//        // If you manually create it like above, Hilt doesn't intercept *this specific constructor call*
//        // to inject its internal parameters unless the VM class itself is not managed by Hilt.
//        //
//        // The most robust way with Hilt when you have constructor parameters you provide
//        // manually (like mocks) AND constructor parameters Hilt should provide (like dispatchers)
//        // is to have Hilt construct the ViewModel if possible, and use @BindValue for mocks too.
//        //
//        // However, if AddOrUpdateCategoryUseCase is an INTERFACE, @BindValue for it is clean.
//        // If it's a CLASS, mocking and binding can be more involved.
//
//        // Given the common pattern of mocking use cases and Hilt providing dispatchers,
//        // let's assume AddOrUpdateCategoryUseCase is an interface.
//        // And we'll let Hilt construct the ViewModel.
//
//        // Re-evaluating the setup:
//        // For Hilt to inject dispatchers when it constructs the ViewModel,
//        // the ViewModel itself should be obtainable from Hilt.
//        // This implies `AddOrUpdateCategoryUseCase` should also be provided/bound by Hilt.
//        // If we want to mock `AddOrUpdateCategoryUseCase`, we use `@BindValue` for it too.
//    }
//
//
//    @Test
//    fun `initial state is correct`() = runTest {
//        val initialState = EditCategoryUiState()
//        Assert.assertEquals(initialState, viewModel.uiState.value)
//    }
//
//
//}
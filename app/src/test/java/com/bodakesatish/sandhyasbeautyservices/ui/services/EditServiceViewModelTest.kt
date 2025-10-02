package com.bodakesatish.sandhyasbeautyservices.ui.services
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import app.cash.turbine.test
//import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
//import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
//import com.bodakesatish.sandhyasbeautyservices.domain.usecases.AddOrUpdateServiceUseCase
//import com.bodakesatish.sandhyasbeautyservices.util.MainCoroutineRule
//import com.nhaarman.mockitokotlin2.any
//import com.nhaarman.mockitokotlin2.never
//import com.nhaarman.mockitokotlin2.verify
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.TestCoroutineScheduler
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.mockito.Mockito.mock
//
//@ExperimentalCoroutinesApi
//class EditServiceViewModelTest {
//
//    // Rule for JUnit to execute tasks synchronously
//    @get:Rule
//    var instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    // Rule to swap Main dispatcher for a TestDispatcher (used by viewModelScope by default)
//    @get:Rule
//    val mainCoroutineRule =
//        MainCoroutineRule() // Uses UnconfinedTestDispatcher by default for simplicity
//
//    // TestDispatcher for IO operations, which we will inject into the ViewModel
//    // Using StandardTestDispatcher to have explicit control over coroutine execution
//    private lateinit var testScheduler: TestCoroutineScheduler // Declare the scheduler
//
//    private lateinit var mockAddOrUpdateServiceUseCase: AddOrUpdateServiceUseCase
//    private lateinit var viewModel: EditServiceViewModel
//
//    private val sampleCategory = Category(id = 1, categoryName = "Facial")
//    private val sampleService =
//        Service(id = 10, serviceName = "Gold Facial", servicePrice = 100.0, categoryId = 1)
//    private val newServiceId = 15L // Assuming use case returns Long for new ID or rows affected
//
//    private var testIoDispatcher = StandardTestDispatcher()
//
//    @Before
//    fun setUp() {
//        testScheduler = TestCoroutineScheduler() // Initialize the scheduler
//        // Ensure mainCoroutineRule.testDispatcher uses this scheduler if they need to be coordinated
//        // OR if mainCoroutineRule.testDispatcher is already a StandardTestDispatcher, use its scheduler:
//        // testScheduler = (mainCoroutineRule.testDispatcher as? StandardTestDispatcher)?.scheduler ?: TestCoroutineScheduler()
//
//        // Pass the scheduler to your test dispatchers
//        mainCoroutineRule.setTestDispatcher(StandardTestDispatcher(testScheduler))        // We need to ensure ioDispatcher uses the same one for coordination.
//        // If mainCoroutineRule uses UnconfinedTestDispatcher, it doesn't use the scheduler in the same way.
//
//        // Simplest if mainCoroutineRule also uses StandardTestDispatcher from the same scheduler:
//        // testIoDispatcher = StandardTestDispatcher(mainCoroutineRule.testDispatcher.scheduler)
//        // For clarity and control, let's ensure they share a scheduler:
//        testIoDispatcher = StandardTestDispatcher(testScheduler)
//        // If your MainCoroutineRule's dispatcher is also a StandardTestDispatcher, make sure it also uses this `testScheduler`
//        // or obtain the scheduler from it. The key is that dispatchers you want to control together share a scheduler.
//
//        // If your MainCoroutineRule sets up Dispatchers.Main with an UnconfinedTestDispatcher,
//        // then operations on Dispatchers.Main will run eagerly.
//        // For testIoDispatcher (StandardTestDispatcher), you will control it with testScheduler.scheduler.advanceUntilIdle().
//
//        // Mock the use case
//        mockAddOrUpdateServiceUseCase = mock()
//
//        // Instantiate ViewModel with mocked use case and the test IO dispatcher
//        viewModel = EditServiceViewModel(
//            addOrUpdateServiceUseCase = mockAddOrUpdateServiceUseCase,
//            ioDispatcher = testIoDispatcher // Provide the test dispatcher
//        )
//    }
//
//    @Test
//    fun `initial uiState is correct`() = runTest {
//        val initialState = EditServiceUiState()
//        Assert.assertEquals(initialState, viewModel.uiState.value)
//    }
//
//    @Test
//    fun setCategoryupdatesuiStateandheaderforaddmode() = runTest {
//        viewModel.uiState.test {
//            Assert.assertEquals(EditServiceUiState(), awaitItem()) // Initial state
//
//            viewModel.setCategory(sampleCategory)
//
//            val expectedState = EditServiceUiState(
//                currentCategory = sampleCategory,
//                headerText = "Add your service to ${sampleCategory.categoryName}"
//            )
//            Assert.assertEquals(expectedState, awaitItem())
//            ensureAllEventsConsumed()
//        }
//    }
//
//    @Test
//    fun `setCategory updates uiState and header for edit mode`() = runTest {
//        // Setup edit mode first
//        // viewModel.setService(sampleService)
//        // Consume states from setService
//        viewModel.uiState.test { // Line 111 (start of turbine block)
//            val initialState = awaitItem() // 1. Initial state (before any actions in this test)
//            // How many items does setService emit?
//            // Let's assume it emits one for now, setting isEditMode = true
//            viewModel.setService(sampleService)
//
//            val afterSetService = awaitItem() // 2. State after viewModel.setService(sampleService)
//            Assert.assertTrue(
//                "isEditMode should be true after setService",
//                afterSetService.isEditMode
//            )            // If it emits more (e.g. after loading associated category), add more awaitItem() here.
////             val afterSetServicePart2 = awaitItem()
//
//            val newCategoryToSet = Category(id = 2, categoryName = "Haircut")
//            viewModel.setCategory(newCategoryToSet) // This should emit a new state
//
//            // This is line 113, which is timing out:
//            val stateAfterNewCategory =
//                awaitItem() // 3. State after viewModel.setCategory(newCategory)
//            Assert.assertEquals(newCategoryToSet, stateAfterNewCategory.currentCategory)
//            Assert.assertEquals(
//                "Update service in ${newCategoryToSet.categoryName}", // Header should use the NEW category
//                stateAfterNewCategory.headerText
//            )
//            Assert.assertTrue("isEditMode should still be true", stateAfterNewCategory.isEditMode)
//            ensureAllEventsConsumed()
//
//
//            //  This structure is a common and correct way to use Turbine:1.viewModel.uiState.test { ... }: Start collecting emissions from the StateFlow.2.val initialState = awaitItem(): Consume the current value of the StateFlow as the first item. StateFlow always has an initial value.3.viewModel.setService(sampleService): Perform an action that should cause the StateFlow to emit a new state.4.val afterSetService = awaitItem(): Consume the state emitted as a result of calling setService.5.viewModel.setCategory(newCategoryToSet): Perform another action.6.val stateAfterNewCategory = awaitItem(): Consume the state emitted as a result of calling setCategory.7.Asserts: Verify the consumed states. The key change that likely made this pass (compared to some intermediate failing versions) was ensuring the awaitItem() calls correctly matched the actual number of emissions from the ViewModel's methods (setService emitting once, setCategory emitting once after that). It also implies that your setCategory method in the EditServiceViewModel is now correctly updating the _uiState and producing a distinct new emission even when isEditMode is true, particularly with respect to the headerText and currentCategory changing. This is a good, solid test structure for this scenario.
//        }
//    }
//
//    @Test
//    fun `setService updates uiState for edit mode correctly`() = runTest {
//
//        viewModel.uiState.test {
//            val initialState = awaitItem() // initial
//
//            // 2. Perform the setCategory action and consume its resulting state
//            viewModel.setCategory(sampleCategory)
//            val afterSetCategory = awaitItem() // after setCategory
//
//            Assert.assertEquals(sampleCategory, afterSetCategory.currentCategory)
//            Assert.assertEquals(
//                "Add your service to ${sampleCategory.categoryName}", // Assuming this is the header in add mode
//                afterSetCategory.headerText
//            )
//            Assert.assertFalse(afterSetCategory.isEditMode) // Should still be false
//
//            viewModel.setService(sampleService)
//            val actualStateAfterSetService = awaitItem() // Expecting state update from setService
//
//            // Expected state construction:
//            val expectedState = EditServiceUiState(
//                initialServiceId = sampleService.id,
//                serviceName = sampleService.serviceName,
//                servicePrice = sampleService.servicePrice.toString(),
//                isEditMode = true,
//                currentCategory = sampleCategory, // Category from the previous step
//                headerText = "Update service in ${sampleCategory.categoryName}" // Header reflects edit mode
//            )
//
//            // Compare relevant fields
//            Assert.assertEquals(
//                expectedState.initialServiceId,
//                actualStateAfterSetService.initialServiceId
//            )
//            Assert.assertEquals(expectedState.serviceName, actualStateAfterSetService.serviceName)
//            Assert.assertEquals(expectedState.servicePrice, actualStateAfterSetService.servicePrice)
//            Assert.assertEquals(expectedState.isEditMode, actualStateAfterSetService.isEditMode)
//            Assert.assertEquals(
//                expectedState.currentCategory,
//                actualStateAfterSetService.currentCategory
//            )
//            Assert.assertEquals(expectedState.headerText, actualStateAfterSetService.headerText)
//
//            ensureAllEventsConsumed()
//
//            //Key Changes and Why:1.viewModel.setCategory(sampleCategory) moved inside test: This allows you to correctly awaitItem() for its emission.2.First awaitItem() captures true initial state: initialState will be EditServiceUiState().3.Second awaitItem() captures state after setCategory: afterSetCategory is the result of setting the category.4.Third awaitItem() captures state after setService: This is actualStateAfterSetService, which is what you're testing. Next Steps:5.Apply the revised test structure above.6.If it still fails (times out at the same point):•Thoroughly inspect your EditServiceViewModel.setService(service: Service) method.•Add println or Log.d statements inside the update block of _uiState.update in your setService method to see what the currentState is before the copy, and what the newState is after the copy.•Ensure that setService is actually changing enough fields for the new state to be considered different from afterSetCategoryState. The most important changes for this test are isEditMode becoming true and headerText updating to reflect edit mode. Example debugging in setService:
//            // Excellent! It's great to hear that the setService updates uiState for edit mode correctly test is now passing with the revised structure. The key to fixing this was to ensure that all actions causing state changes (setCategory and setService) are performed within the viewModel.uiState.test { ... } block, and that each state change is followed by an awaitItem() call to consume the emitted state. This corrected the issue where setCategory was called outside the test block, leading to an incorrect assumption about which state was being captured by the first awaitItem(). Summary of the Corrected Test Logic:1.val initialState = awaitItem(): Captures the true initial state of the uiState (the default EditServiceUiState() when the ViewModel is first created and uiState is first collected).2.viewModel.setCategory(sampleCategory): Action 1.3.val afterSetCategory = awaitItem(): Captures the state emitted after the category is set.4.viewModel.setService(sampleService): Action 2 (the primary action being tested in this function).5.val actualStateAfterSetService = awaitItem(): Captures the state emitted after the service is set.6.Assertions: Verify that actualStateAfterSetService matches the expectedState based on the actions performed.7.ensureAllEventsConsumed(): Confirms no unexpected state emissions occurred. This pattern is robust for testing sequences of state changes in ViewModels using StateFlow and Turbine. Well done for working through these Turbine testing nuances!
//        }
//    }
//
//    @Test
//    fun `onServiceNameChanged updates serviceName and clears error`() = runTest {
//        // Initial state setup if onServiceNameChanged also clears an error
//        // For this example, let's assume onServiceNameChanged directly updates state
//        // and potentially clears an error if one was present.
//
//        viewModel.uiState.test {
//            var currentState = awaitItem() // Initial state
//
//            // Optional: Simulate a previous error state if onServiceNameChanged should clear it
//            // This would require a way to set serviceNameError directly or a ViewModel action that does.
//            // For simplicity, let's assume the state starts clean or onServiceNameChanged handles it.
//            // If you need to set an error:
//            // viewModel.setInitialErrorStateForTesting(serviceNameError = "Old Error") // Hypothetical method
//            // currentState = awaitItem() // Consume the error state
//
//            viewModel.onServiceNameChanged("New Valid Name")
//            currentState = awaitItem() // State after onServiceNameChanged
//
//            Assert.assertEquals("New Valid Name", currentState.serviceName)
//            Assert.assertNull(currentState.serviceNameError) // Assuming it clears error
//            ensureAllEventsConsumed()
//        }
//    }
//
//    @Test
//    fun `onServicePriceChanged updates servicePrice and clears error`() = runTest {
//        viewModel.onServicePriceChanged("")
//        viewModel.addOrUpdateService() // Trigger validation
//        testIoDispatcher.scheduler.advanceUntilIdle()
//
//        viewModel.uiState.test {
//            skipItems(1)
//
//            viewModel.onServicePriceChanged("123.45")
//
//            val state = awaitItem()
//            Assert.assertEquals("123.45", state.servicePrice)
//            Assert.assertNull(state.servicePriceError)
//            ensureAllEventsConsumed()
//        }
//    }
//
//    @Test
//    fun `addOrUpdateService with no category set shows category error`() = runTest {
//        viewModel.uiState.test {
//            var currentState = awaitItem() // 1. Initial state
//
//            viewModel.onServiceNameChanged("Service Name")
//            currentState = awaitItem() // 2. After name change
//
//            viewModel.onServicePriceChanged("10.0")
//            currentState = awaitItem() // 3. After price change
//
//            viewModel.addOrUpdateService()
//            // `addOrUpdateService` is now emitting a new state with the category error.
//            val errorState = awaitItem() // 4. State after addOrUpdateService (with category error)
//
//            verify(mockAddOrUpdateServiceUseCase, never()).invoke(any())
//
//            // Assert the specific error state for saveResult
//            Assert.assertEquals(
//                SaveResult.Error("Category not selected."), // EXPECT THE ERROR STATE
//                errorState.saveResult
//            )
//            // You might also want to assert a specific error field for category if you have one
//            // Assert.assertEquals("Category must be selected", errorState.categoryError) // Example
//
//            ensureAllEventsConsumed()
//        }
//
//        // Assert no navigation command was sent
//        viewModel.navigationCommands.test {
//            expectNoEvents()
//        }
//
////        Reasoning for the change:•Emitted State: Your addOrUpdateService method, when a category is not set, updates the _uiState with a SaveResult.Error. This causes a new emission from the StateFlow.•awaitItem(): You need an awaitItem() after viewModel.addOrUpdateService() to consume this newly emitted error state.•Assertion Update: The assertion for saveResult needs to check for SaveResult.Error("Category not selected.") instead of SaveResult.Idle. This change aligns your test with the actual behavior of your ViewModel. It's often the case that during testing, you discover the component behaves slightly differently than initially assumed, and the test needs to be adjusted to reflect the correct, desired behavior. Now, let's continue with the rest of the failing tests based on the previous response and this new understanding. 4. addOrUpdateService with empty service name shows name error (Continuing from previous response)
//    }
//
//}
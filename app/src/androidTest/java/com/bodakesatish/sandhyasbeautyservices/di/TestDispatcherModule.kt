package com.bodakesatish.sandhyasbeautyservices.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher

// Assuming you have a way to provide the actual TestDispatcher instance for tests
// e.g., from your MainCoroutineRule or another Hilt binding.
// Let's assume for now you have a TestDispatcher available to be injected here.
// In a real setup, you might provide the TestDispatcher itself as a Hilt binding.

@OptIn(ExperimentalCoroutinesApi::class)
@Module
@InstallIn(SingletonComponent::class) // This replaces or adds to the production bindings
object TestDispatcherModule {

    // Scenario 1: You have a single TestDispatcher from MainCoroutineRule for all
    // You'd need to provide this TestDispatcher instance itself via Hilt, or access it.
    // This is the tricky part without seeing how MainCoroutineRule.testDispatcher becomes available to Hilt.

    // A common way to make MainCoroutineRule.testDispatcher available to Hilt for tests:
    // You'd typically have a Hilt test rule that manages this.
    // For pure unit tests where Hilt isn't building the Android component graph,
    // it's simpler if TestDispatcher is directly usable.

    // Let's assume you have a Hilt component that can provide the TestDispatcher
    // from your MainCoroutineRule. If not, this is where a small adjustment is needed.

    @Provides
    @IoDispatcher
    fun provideTestIoDispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher = testDispatcher

    @Provides
    @DefaultDispatcher
    fun provideTestDefaultDispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher = testDispatcher

    // If you don't have `TestDispatcher` itself as a Hilt binding yet,
    // and TestDispatcherModule needs to create it, it would look like this
    // (but then all tests share the same TestDispatcher instance unless scoped differently,
    // which is usually what you want for a TestCoroutineScheduler).

    // Example: If TestDispatcher is NOT yet a Hilt binding:
    // This requires MainCoroutineRule to be somehow accessible globally or setup early.
    // This is less ideal. Better is to make the TestDispatcher from MainCoroutineRule a Hilt binding.
    /*
    @Provides
    @Singleton // Or appropriate scope
    fun provideTestDispatcherInstance(scheduler: TestCoroutineScheduler): TestDispatcher {
        return StandardTestDispatcher(scheduler)
    }

    @Provides
    @Singleton
    fun provideTestCoroutineScheduler(): TestCoroutineScheduler = TestCoroutineScheduler()
    */
}
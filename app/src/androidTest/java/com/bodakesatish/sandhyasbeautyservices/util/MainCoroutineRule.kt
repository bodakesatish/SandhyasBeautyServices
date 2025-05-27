package com.bodakesatish.sandhyasbeautyservices.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher // Or StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class MainCoroutineRule(
    // Using UnconfinedTestDispatcher for simplicity in many ViewModel tests
    // as it executes coroutines eagerly.
    // For more control over execution order and time, use StandardTestDispatcher.
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }

    fun setTestDispatcher(dispatcher: TestDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        Dispatchers.setMain(dispatcher)
    }
}
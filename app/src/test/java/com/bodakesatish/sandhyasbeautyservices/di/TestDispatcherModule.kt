package com.bodakesatish.sandhyasbeautyservices.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher

@Module
@InstallIn(SingletonComponent::class) // Match the component of the module you're replacing/supplementing
object TestDispatcherModule {

    // You need a TestCoroutineScheduler and a TestDispatcher instance
    // Often managed by a JUnit rule like your MainCoroutineRule
    // For simplicity, let's assume it's accessible or provided here.
    // In a real Hilt test setup, you might provide the TestDispatcher itself as a bean.

    @Provides
    @IoDispatcher
    fun provideTestIoDispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher = testDispatcher
    // Here, TestDispatcher would be another @Provides fun that returns mainCoroutineRule.testDispatcher

    @Provides
    @DefaultDispatcher
    fun provideTestDefaultDispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher = testDispatcher

    // If you also need a Main dispatcher
    // @Provides
    // @MainDispatcher
    // fun provideTestMainDispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher = testDispatcher
}
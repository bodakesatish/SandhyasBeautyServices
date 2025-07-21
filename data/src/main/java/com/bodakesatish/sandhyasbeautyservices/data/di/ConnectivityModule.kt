package com.bodakesatish.sandhyasbeautyservices.data.di

import android.content.Context
import com.bodakesatish.sandhyasbeautyservices.data.utils.NetworkConnectivityService
import com.bodakesatish.sandhyasbeautyservices.data.utils.NetworkConnectivityServiceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // For application-scoped dependencies
object ConnectivityModule { // Or any appropriate module name

    @Provides
    @Singleton // If NetworkConnectivityService should be a singleton
    fun provideNetworkConnectivityService(
        @ApplicationContext context: Context // <-- How to get Application Context
    ): NetworkConnectivityService {
        // Now you can use the 'context' to instantiate your service
        return NetworkConnectivityServiceImpl(context)
    }

    // You can add other @Provides methods here that might also need Context
    // For example:
    // @Provides
    // @Singleton
    // fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
    //     return context.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE)
    // }
}
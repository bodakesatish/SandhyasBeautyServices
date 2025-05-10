package com.bodakesatish.sandhyasbeautyservices.data.di

import android.content.Context
import androidx.room.Room
import com.bodakesatish.sandhyasbeautyservices.data.source.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val  DATABASE_NAME = "sandhyasapp.db"

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext appContext: Context) : AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun providesMyModelDao(appDatabase: AppDatabase) = appDatabase.myModelDao()

    @Provides
    @Singleton
    fun providesCustomerDao(appDatabase: AppDatabase) = appDatabase.customerDao()

    @Provides
    @Singleton
    fun providesCategoryDao(appDatabase: AppDatabase) = appDatabase.categoryDao()

    @Provides
    @Singleton
    fun providesServiceDao(appDatabase: AppDatabase) = appDatabase.serviceDao()

    @Provides
    @Singleton
    fun providesAppointmentsDao(appDatabase: AppDatabase) = appDatabase.appointmentsDao()

    @Provides
    @Singleton
    fun providesServiceDetailDao(appDatabase: AppDatabase) = appDatabase.serviceDetailDao()

}
package com.bodakesatish.sandhyasbeautyservices.di

import com.bodakesatish.sandhyasbeautyservices.data.repository.AppointmentRepositoryImpl
import com.bodakesatish.sandhyasbeautyservices.data.repository.CategoryRepositoryImpl
import com.bodakesatish.sandhyasbeautyservices.data.repository.CustomerRepositoryImpl
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentRepository
import com.bodakesatish.sandhyasbeautyservices.domain.repository.CategoryRepository
import com.bodakesatish.sandhyasbeautyservices.domain.repository.CustomerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    @Binds
    @Singleton
    fun bindCustomerRepository(customerRepositoryImpl: CustomerRepositoryImpl): CustomerRepository

    @Binds
    @Singleton
    fun bindCategoryRepository(categoryRepository: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    fun bindAppointmentRepository(appointmentRepository: AppointmentRepositoryImpl): AppointmentRepository

}
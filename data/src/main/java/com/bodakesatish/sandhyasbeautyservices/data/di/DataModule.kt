package com.bodakesatish.sandhyasbeautyservices.data.di

import com.bodakesatish.sandhyasbeautyservices.data.repository.DefaultMyModelRepository
import com.bodakesatish.sandhyasbeautyservices.data.repository.MyModelRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindMyModelRepository(
        myModelRepository: DefaultMyModelRepository
    ) : MyModelRepository


}

class FakeMyModelRepository @Inject constructor() : MyModelRepository {

    override val myModels: Flow<List<String>> = flowOf(fakeMyModels)

    override suspend fun add(name: String) {
        throw NotImplementedError()
    }

    override suspend fun delete(name: String) {
        throw NotImplementedError()
    }
}

val fakeMyModels = listOf("One", "Two", "Three")//List(30) { "Item $it" }

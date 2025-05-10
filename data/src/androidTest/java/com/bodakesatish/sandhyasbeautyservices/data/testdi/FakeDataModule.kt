package com.bodakesatish.sandhyasbeautyservices.data.testdi

import com.bodakesatish.sandhyasbeautyservices.data.di.DataModule
import com.bodakesatish.sandhyasbeautyservices.data.di.FakeMyModelRepository
import com.bodakesatish.sandhyasbeautyservices.data.repository.MyModelRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class]
)
interface FakeDataModule {

    @Binds
    abstract fun bindRepository(
        fakeRepository: FakeMyModelRepository
    ) : MyModelRepository
}
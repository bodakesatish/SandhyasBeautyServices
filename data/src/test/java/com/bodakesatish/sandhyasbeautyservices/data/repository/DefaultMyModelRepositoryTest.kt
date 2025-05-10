package com.bodakesatish.sandhyasbeautyservices.data.repository

import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.MyModelDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.MyModelEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals


class DefaultMyModelRepositoryTest {

    @Test
    fun myModels_newItemSaved_itemIsReturned() = runTest {
        val repository = DefaultMyModelRepository(FakeMyModelDao())
        repository.add("Repository")

        assertEquals(repository.myModels.first().size, 1)
    }

    @Test
    fun myModels_deleteItem() = runTest {
        val repository = DefaultMyModelRepository(FakeMyModelDao())
        repository.add("Repository")
        repository.delete("Repository")

        assertEquals(repository.myModels.first().size, 0)
    }

}

private class FakeMyModelDao : MyModelDao {

    private val data = mutableListOf<MyModelEntity>()

    override fun getMyModels(): Flow<List<MyModelEntity>>  = flow {
        emit(data)
    }

    override suspend fun insertMyModel(item: MyModelEntity) {
        data.add(0, item)
    }

    override suspend fun deleteMyModel(name: String) {
        data.removeFirst()
    }
}
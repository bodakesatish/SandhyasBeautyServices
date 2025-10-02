package com.bodakesatish.sandhyasbeautyservices.data.repository

import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.MyModelDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.MyModelEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface MyModelRepository {
    val myModels: Flow<List<String>>
    suspend fun add(name: String)
    suspend fun delete(name: String)
}

class DefaultMyModelRepository @Inject constructor(
    private val myModelDao: MyModelDao
) : MyModelRepository {

    override val myModels: Flow<List<String>>
        get() = myModelDao.getMyModels().map { items -> items.map { it.name } }


    override suspend fun add(name: String) {
        myModelDao.insertMyModel(MyModelEntity(name = name))
    }

    override suspend fun delete(name: String) {
        myModelDao.deleteMyModel(name = name)
    }


}
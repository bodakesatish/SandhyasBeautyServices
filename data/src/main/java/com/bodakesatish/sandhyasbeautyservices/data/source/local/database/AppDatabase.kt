package com.bodakesatish.sandhyasbeautyservices.data.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.AppointmentsDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.CategoryDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CustomerEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.MyModelEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.CustomerDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.MyModelDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.ServiceDetailDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.ServicesDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.AppointmentsEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CategoryEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceDetailEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceEntity

@Database(
    entities = [
        MyModelEntity::class,
        CustomerEntity::class,
        CategoryEntity::class,
        ServiceEntity::class,
        AppointmentsEntity::class,
        ServiceDetailEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun myModelDao(): MyModelDao
    abstract fun customerDao(): CustomerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun serviceDao(): ServicesDao
    abstract fun appointmentsDao(): AppointmentsDao
    abstract fun serviceDetailDao(): ServiceDetailDao
}
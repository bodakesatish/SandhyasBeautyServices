package com.bodakesatish.sandhyasbeautyservices.data.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bodakesatish.sandhyasbeautyservices.data.repository.UserDao
import com.bodakesatish.sandhyasbeautyservices.data.repository.UserEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.convertors.DateConverter
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
import com.bodakesatish.swadhyaycommerceclasses.data.source.local.convertors.BooleanConverter

@Database(
    entities = [
        MyModelEntity::class,
        CustomerEntity::class,
        CategoryEntity::class,
        ServiceEntity::class,
        AppointmentsEntity::class,
        ServiceDetailEntity::class,
        UserEntity::class
    ],
    version = 1
)
@TypeConverters(DateConverter::class, BooleanConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun myModelDao(): MyModelDao
    abstract fun customerDao(): CustomerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun serviceDao(): ServicesDao
    abstract fun appointmentsDao(): AppointmentsDao
    abstract fun serviceDetailDao(): ServiceDetailDao
    abstract fun userDao(): UserDao
}
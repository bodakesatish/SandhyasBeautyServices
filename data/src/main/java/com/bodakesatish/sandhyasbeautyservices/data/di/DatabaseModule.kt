package com.bodakesatish.sandhyasbeautyservices.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bodakesatish.sandhyasbeautyservices.data.source.local.database.AppDatabase
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CategoryEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CustomerEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.ServiceEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Date
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "sandhyasapp.db"

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            DATABASE_NAME
        )//.createFromAsset(DATABASE_NAME)
            .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

//                db.execSQL(
//                    "INSERT INTO ${
//                        CategoryEntity.TABLE_NAME
//                    } (${CategoryEntity.Columns.ID}, ${CategoryEntity.Columns.CATEGORY_NAME}) VALUES " +
//                            "(1,'Skin Care Services'),(2, 'Waxing Services'),(3,'Hair Style')"
//                )
//                db.execSQL(
//                    "INSERT INTO ${
//                        ServiceEntity.TABLE_NAME
//                    } (${ServiceEntity.Columns.ID}, ${ServiceEntity.Columns.CATEGORY_ID}, ${ServiceEntity.Columns.SERVICE_NAME}, ${ServiceEntity.Columns.NORMAL_PRICE}) VALUES " +
//                            "(1,1,'Back Facial',250.0),(2,1,'Spa Facial',350.0),(3,1,'Detox Facial',450.0)," +
//                            "(4,2,'Lip/Chin Waxing',100.0),(5,2,'Eyebrow Waxing',100.0),(6,2,'Full Face Waxing',300.0)," +
//                            "(7,3,'Normal Hair Color',300.0),(8,3,'Haircut',100.0),(9,3,'Premium Hair Color',600.0)"
//                )
//                db.execSQL(
//                    "INSERT INTO ${
//                        CustomerEntity.TABLE_NAME
//                    } (${CustomerEntity.Columns.ID}, ${CustomerEntity.Columns.FIRST_NAME}, ${CustomerEntity.Columns.LAST_NAME}, ${CustomerEntity.Columns.PHONE}, ${CustomerEntity.Columns.ADDRESS}, ${CustomerEntity.Columns.DATE_OF_BIRTH}) VALUES " +
//                            "(1,'Satish','Bodake','1234567890','Warje','${Date()}')," +
//                            "(2,'Sandhya','Bodake','2234567891','Belapur','${Date()}')," +
//                            "(3,'Rajveer','Bodake','3234567892','Kothrud','${Date()}')"
//                )
            }
        }

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

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase) = appDatabase.userDao()

}
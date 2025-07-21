package com.bodakesatish.sandhyasbeautyservices.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
//
//    @Provides
//    @Singleton
//    fun provideFirebaseFirestore(): FirebaseFirestore {
//        val firestore = Firebase.firestore // Get the default Firestore instance
//
//        // Optional: Configure Firestore settings (e.g., offline persistence, cache size)
//        // val settings = firestoreSettings {
//        //    isPersistenceEnabled = true // Enable offline persistence
//        //    cacheSizeBytes = com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED // Example
//        //    // Or a specific size: cacheSizeBytes = 100 * 1024 * 1024 // 100 MB
//        // }
//        // firestore.firestoreSettings = settings
//
//        return firestore
//    }

}
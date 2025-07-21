package com.bodakesatish.sandhyasbeautyservices.data.source.remote

import android.util.Log
import com.bodakesatish.sandhyasbeautyservices.data.utils.NetworkConnectivityService
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.utils.NetworkResult
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


interface CategoryRemoteDataSource {
    suspend fun insertCategory(category: Category): NetworkResult<String> // Returns Firestore Doc ID
    suspend fun updateCategory(category: Category): NetworkResult<String>
    // Fetches from Firestore, does NOT observe Room anymore
    suspend fun fetchCategoriesFromFirestore(): NetworkResult<List<Category>>
    suspend fun deleteCategoryFromFirestore(firestoreDocId: String): NetworkResult<Boolean>
}

class CategoryRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore, // Injected via Hilt
    private val networkConnectivityService: NetworkConnectivityService // Injected via Hilt
): CategoryRemoteDataSource {

    private val tag = "CategoryRemoteDataSource: "

    override suspend fun insertCategory(category: Category): NetworkResult<String> {
        if (!networkConnectivityService.isNetworkAvailable()) {
            return NetworkResult.Error("No internet connection.")
        }
        return try {
            val categoryDataWithoutId = category.copy(firestoreDocId = category.firestoreDocId).toHashMap()
            val documentReference = firestore.collection("categories")
                .add(categoryDataWithoutId)
                .await()
            val documentId = documentReference.id
            Log.d(tag, "Category placeholder inserted with ID: $documentId")

            val finalCategoryData = category.copy(firestoreDocId = documentId).toHashMap()
            firestore.collection("categories")
                .document(documentId)
                .set(finalCategoryData)
                .await()
            Log.d(tag, "Category $documentId finalized with its firestoreDocId.")
            // Room insertion will be handled by the Repository after this success
            NetworkResult.Success(documentId)
        } catch (e: FirebaseNetworkException) {
            Log.e(tag, "Network error inserting category: ${e.message}", e)
            NetworkResult.Error("Network error during insert.", exception = e)
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, "Firestore error inserting category: ${e.code} - ${e.message}", e)
            NetworkResult.Error("Firestore error: ${e.message}", code = e.code.value(), exception = e)
        } catch (e: Exception) {
            Log.e(tag, "Generic error inserting category: ${e.message}", e)
            NetworkResult.Error("An unexpected error occurred during insert.", exception = e)
        }
    }

    override suspend fun fetchCategoriesFromFirestore(): NetworkResult<List<Category>> {
        if (!networkConnectivityService.isNetworkAvailable()) {
            return NetworkResult.Error("No internet connection for sync.", code = -100)
        }
        Log.d(tag, "Attempting Firestore sync...")
        return try {
            val querySnapshot = firestore.collection("categories").get().await()
            if (querySnapshot.isEmpty) {
                val isFromCache = querySnapshot.metadata.isFromCache
                Log.d(tag, "No categories found in Firestore during sync.")
                if(isFromCache) {
                    NetworkResult.Error("Failed to fetch categories from server. No data in cache.", code = -100)
                     // Important: return success with empty list
                } else {
                    NetworkResult.Success(emptyList())

                }
            } else {
                val categoriesFromFirestore = mutableListOf<Category>()
                for (document in querySnapshot.documents) {
                    try {
                        // Directly map to Domain model if possible, or an intermediate DTO
                        val categoryName = document.getString("categoryName")
                        if (categoryName != null) {
                            categoriesFromFirestore.add(
                                Category( // Assuming Category domain model constructor
                                    categoryName = categoryName,
                                    firestoreDocId = document.id
                                    // other fields if your domain model has them
                                )
                            )
                        } else {
                            Log.w(tag, "Doc ${document.id} missing 'categoryName' during sync.")
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Error processing doc ${document.id} during sync", e)
                    }
                }
                if (categoriesFromFirestore.isNotEmpty()) {
                    Log.d(tag, "${categoriesFromFirestore.size} categories fetched from Firestore.")
                    NetworkResult.Success(categoriesFromFirestore)
                } else if (!querySnapshot.isEmpty) {
                    Log.w(tag, "Firestore had documents, but none were parsable into Category domain model.")
                    NetworkResult.Error("Failed to parse category data from server.", code = -101)
                } else {
                    NetworkResult.Success(emptyList()) // Should be covered by querySnapshot.isEmpty
                }
            }
        } catch (e: FirebaseNetworkException) {
            Log.e(tag, "Network error during Firestore category sync: ${e.message}", e)
            NetworkResult.Error("Network error during sync.", exception = e)
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, "Firestore error during category sync: ${e.code} - ${e.message}", e)
            NetworkResult.Error("Firestore error: ${e.message}", code = e.code.value(), exception = e)
        } catch (e: Exception) {
            Log.e(tag, "Generic error during Firestore category sync: ${e.message}", e)
            NetworkResult.Error("Sync failed unexpectedly.", exception = e)
        }
    }

    override suspend fun deleteCategoryFromFirestore(firestoreDocId: String): NetworkResult<Boolean> {
        if (firestoreDocId.isBlank()) {
            Log.e(tag, "Cannot delete category without a valid firestoreDocId.")
            return NetworkResult.Error("Invalid category data: Missing Firestore ID.")
        }
        if (!networkConnectivityService.isNetworkAvailable()) {
            return NetworkResult.Error("No internet connection.")
        }
        return try {
            firestore.collection("categories")
                .document(firestoreDocId)
                .delete()
                .await()
            Log.d(tag, "Successfully deleted category from Firestore: $firestoreDocId")
            NetworkResult.Success(true)
        } catch (e: FirebaseNetworkException) {
            Log.e(tag, "Network error deleting category $firestoreDocId: ${e.message}", e)
            NetworkResult.Error("Network error during delete.", exception = e)
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, "Firestore error deleting category $firestoreDocId: ${e.message}", e)
            NetworkResult.Error("Firestore error: ${e.message}", code = e.code.value(), exception = e)
        } catch (e: Exception) {
            Log.e(tag, "Generic error deleting category $firestoreDocId: ${e.message}", e)
            NetworkResult.Error("An unexpected error occurred during delete.", exception = e)
        }
    }

    override suspend fun updateCategory(category: Category): NetworkResult<String> {
        if (category.firestoreDocId.isBlank()) { // Use isBlank for safety
            Log.e(tag, "Cannot update category without a valid firestoreDocId.")
            return NetworkResult.Error("Invalid category data: Missing Firestore ID.")
        }
        if (!networkConnectivityService.isNetworkAvailable()) {
            return NetworkResult.Error("No internet connection.")
        }
        return try {
            firestore.collection("categories")
                .document(category.firestoreDocId)
                .set(category.toHashMap())
                .await()
            Log.d(tag, "Successfully updated category in Firestore: ${category.firestoreDocId}")
            // Room update will be handled by the Repository
            NetworkResult.Success(category.firestoreDocId)
        } catch (e: FirebaseNetworkException) {
            Log.e(tag, "Network error updating category ${category.firestoreDocId}: ${e.message}", e)
            NetworkResult.Error("Network error during update.", exception = e)
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, "Firestore error updating category ${category.firestoreDocId}: ${e.message}", e)
            NetworkResult.Error("Firestore error: ${e.message}", code = e.code.value(), exception = e)
        } catch (e: Exception) {
            Log.e(tag, "Generic error updating category ${category.firestoreDocId}: ${e.message}", e)
            NetworkResult.Error("An unexpected error occurred during update.", exception = e)
        }
    }
}
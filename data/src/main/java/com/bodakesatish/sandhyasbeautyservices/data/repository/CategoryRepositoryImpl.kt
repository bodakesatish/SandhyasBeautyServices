package com.bodakesatish.sandhyasbeautyservices.data.repository

import android.util.Log
import com.bodakesatish.sandhyasbeautyservices.data.mapper.CategoryMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.CategoryMapper.toEntityList
import com.bodakesatish.sandhyasbeautyservices.data.mapper.ServiceMapper.mapFromDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.ServiceMapper.mapToDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.source.local.CategoryLocalDataSource
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.CategoryDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.ServicesDao
import com.bodakesatish.sandhyasbeautyservices.data.source.remote.CategoryRemoteDataSource
import com.bodakesatish.sandhyasbeautyservices.domain.model.CategoriesWithServices
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Service
import com.bodakesatish.sandhyasbeautyservices.domain.repository.CategoryRepository
import com.bodakesatish.sandhyasbeautyservices.domain.utils.NetworkResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao, // Still needed for getCategoriesWithServices
    private val serviceDao: ServicesDao,
    private val remoteSource: CategoryRemoteDataSource,
    private val localSource: CategoryLocalDataSource
) : CategoryRepository {

    private val tag = "CategoryRepo: "

    override suspend fun insertOrUpdateCategory(category: Category): NetworkResult<String> { // Changed return type
        val remoteResult = if (category.firestoreDocId.isEmpty())
            remoteSource.insertCategory(category = category)
        else remoteSource.updateCategory(category = category)

        if (remoteResult is NetworkResult.Success) {
            // After successful remote insertion, update local source with the ID from remote
            val categoryWithId = category.copy(firestoreDocId = remoteResult.data)
            localSource.insertCategory(categoryWithId) // Assumes insertCategory in localSource also handles updates
            Log.d(
                tag,
                "Category ${remoteResult.data} also inserted/updated in Room via localSource."
            )
        }
        return remoteResult // Return the result from the remote operation
    }

    override suspend fun deleteCategory(categoryId: String): NetworkResult<Boolean> { // Changed categoryId to String (firestoreDocId)
        val remoteResult = remoteSource.deleteCategoryFromFirestore(categoryId)
        if (remoteResult is NetworkResult.Success && remoteResult.data) {
            localSource.deleteCategoryByFirestoreId(categoryId)
            Log.d(tag, "Category $categoryId also deleted from Room via localSource.")
        }
        return remoteResult
    }

    override fun getCategoryList(forceToRefresh: Boolean): Flow<NetworkResult<List<Category>>> {
        return channelFlow {
            Log.d(
                tag,
                "getCategoryList called with forceToRefresh: $forceToRefresh. Subscribing to local changes."
            )

            // 1. Always observe local data and emit it as NetworkResult.Success
            val localDataJob = launch {
                localSource.observeCategories().collectLatest { categories ->
                    Log.d(
                        tag,
                        "Local source emitted ${categories.size} categories. Sending as Success."
                    )
                    if (categories.isNotEmpty())
                        send(NetworkResult.Success(categories))
                }
            }

            // 2. Determine if a remote fetch is needed
            val isLocalCacheEmpty = localSource.getCategoryCount() == 0
            val shouldFetchFromRemote = forceToRefresh || isLocalCacheEmpty
            Log.d(
                tag,
                "isLocalCacheEmpty: $isLocalCacheEmpty, shouldFetchFromRemote: $shouldFetchFromRemote"
            )


            if (shouldFetchFromRemote) {
                // Emit Loading state before fetching from remote if local cache was empty
                // or if we are forcing a refresh.
                if (isLocalCacheEmpty) { // Only send loading if UI has nothing to show yet
                    Log.d(tag, "Local cache is empty, sending Loading state.")
                    send(NetworkResult.Loading)
                }

                Log.d(tag, "Fetching from remote source...")
                val remoteResult = remoteSource.fetchCategoriesFromFirestore()

                when (remoteResult) {
                    is NetworkResult.Success -> {
                        val remoteCategories = remoteResult.data
                        Log.d(
                            tag,
                            "Remote fetch successful with ${remoteCategories.size} categories."
                        )
                        // Update local source with data from remote
                        if (remoteCategories.isEmpty() && !isLocalCacheEmpty) {
                            // If remote is empty, and local wasn't, clear local
                            Log.d(tag, "Remote is empty, clearing local cache.")
                            localSource.clearAllCategories()
                        } else if (remoteCategories.isNotEmpty()) {
                            // Convert domain models from remote to local entities before upserting
                            localSource.upsertAllCategories(remoteCategories.toEntityList()) // You'll need toEntityList() mapper
                        }
                        // The localDataJob will automatically emit the updated list from Room.
                        // No need to send(NetworkResult.Success(remoteCategories)) here again if localSource.observeCategories emits.
                    }

                    is NetworkResult.Error -> {
                        Log.e(tag, "Remote fetch failed: ${remoteResult.message}")
                        // Emit the error. If local data was already sent, UI can decide how to show this error.
                        // If local cache was empty and this is the first emission after Loading, UI will show error.
                        send(remoteResult)
                    }

                    is NetworkResult.Loading -> {
                        // This case should ideally not happen if fetchCategoriesFromFirestore is suspend fun
                        // and directly returns Success/Error. If it could, handle appropriately.
                        Log.d(tag, "Remote source is still loading (unexpected for suspend fun).")
                        // If not already loading, send loading state.
                        if (!isLocalCacheEmpty || !forceToRefresh) { // Avoid double loading if already sent
                            send(NetworkResult.Loading)
                        }
                    }
                    is NetworkResult.NoInternet -> {
                        Log.d(tag, "No internet connection. Sending NoInternet state.")
                        send(NetworkResult.NoInternet)
                    }
                }
            } else {
                Log.d(tag, "Skipping remote fetch. Relying on local data stream.")
                // Local data stream is already active and will provide data.
            }

            awaitClose {
                Log.d(tag, "getCategoryList flow collection stopped. Cancelling localDataJob.")
                localDataJob.cancel()
            }
        }
    }


    override suspend fun getCategoryById(customerId: String): Category? {
        // Primarily from local source. Remote fetch could be an option if not found locally.
        // For now, let's assume local is the source of truth for single item fetch unless specified.
        return localSource.getCategories().find { it.firestoreDocId == customerId }
        // TODO: Implement proper localSource.getCategoryById(firestoreDocId)
    }

    override suspend fun insertOrUpdateService(service: Service): Long {
        // This seems to be local only for now.
        return serviceDao.insertOrUpdateService(service.mapFromDomainModel())
    }

    override suspend fun deleteService(serviceId: Int) {
        // Local only
        return serviceDao.deleteService(serviceId)
    }

    override fun getServiceList(categoryId: String): Flow<List<Service>> {
        // Local only
        return serviceDao.getServiceListByCategoryId(categoryId).map { services ->
            services.map { service ->
                service.mapToDomainModel()
            }
        }
    }

    override suspend fun getServiceById(serviceId: Long): Service? {
        TODO("Not yet implemented")
    }

    override fun getCategoriesWithServices(): Flow<List<CategoriesWithServices>> {
        // This uses categoryDao directly, which is fine if it's purely a local aggregation.
        return categoryDao.getCategoriesWithServices().map { categoryWithServices ->
            categoryWithServices.map {
                CategoriesWithServices(
                    category = it.category.mapToDomainModel(),
                    services = it.services.map { serviceEntity ->
                        serviceEntity.mapToDomainModel()
                    }
                )
            }
        }
    }
}
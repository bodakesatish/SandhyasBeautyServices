package com.bodakesatish.sandhyasbeautyservices.data.source.local

import com.bodakesatish.sandhyasbeautyservices.data.mapper.CategoryMapper.mapFromDomainModel
import com.bodakesatish.sandhyasbeautyservices.data.mapper.CategoryMapper.toDomainModelList
import com.bodakesatish.sandhyasbeautyservices.data.source.local.dao.CategoryDao
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CategoryEntity
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface CategoryLocalDataSource {
    suspend fun insertCategory(category: Category): Long
    suspend fun upsertAllCategories(categories: List<CategoryEntity>) // New: For batch upsert from remote
    suspend fun clearAllCategories() // New: To clear cache when Firestore is empty
    suspend fun getCategories(): List<Category>
    suspend fun getCategoryCount(): Int // New: To check if Room is empty
    fun observeCategories(): Flow<List<Category>> // Changed to non-suspend, as Flow is inherently async
    suspend fun deleteCategory(category: Category)
    suspend fun deleteCategoryByFirestoreId(firestoreDocId: String) // New: For consistency
}

class CategoryLocalDataSourceImpl @Inject constructor(
    private val categoryDao: CategoryDao
): CategoryLocalDataSource {

    override suspend fun insertCategory(category: Category): Long {
        // This is fine if insertCategory is only called for individual, user-initiated inserts
        // that also go to remote. For remote sync, use upsertAllCategories.
        return categoryDao.insertOrUpdate(category.mapFromDomainModel())
    }

    override suspend fun upsertAllCategories(categories: List<CategoryEntity>) {
        categoryDao.upsertAllC(categories) // Assuming your DAO has upsertAll
    }

    override suspend fun clearAllCategories() {
        categoryDao.clearAll() // Assuming your DAO has clearAll
    }

    override suspend fun getCategories(): List<Category> {
        return categoryDao.getCategoryList().toDomainModelList()
    }

    override suspend fun getCategoryCount(): Int {
        return categoryDao.getCategoryListCount() // Assuming your DAO has this
    }

    override fun observeCategories(): Flow<List<Category>> { // Non-suspend
        return categoryDao.observeCategoryList().map { categories ->
            categories.toDomainModelList()
        }
    }

    override suspend fun deleteCategory(category: Category) {
        // Prefer using firestoreDocId if it's the reliable key for deletion
        if (category.firestoreDocId.isNotBlank()) {
            categoryDao.delete(category.firestoreDocId)
        } else {
            // Fallback or error if firestoreDocId is not available for some reason
            // Or ensure category always has a firestoreDocId when this is called.
            // For now, let's assume it has one for remote-originated deletes.
            // If deleting a category that was only local and never synced, this might need adjustment.
            categoryDao.delete(category.firestoreDocId) // This was the original logic
        }
    }

    override suspend fun deleteCategoryByFirestoreId(firestoreDocId: String) {
        categoryDao.delete(firestoreDocId)
    }

}
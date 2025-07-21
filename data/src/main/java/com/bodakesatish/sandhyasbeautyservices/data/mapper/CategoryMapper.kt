package com.bodakesatish.sandhyasbeautyservices.data.mapper

import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CategoryEntity
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category

object CategoryMapper : Mapper<CategoryEntity, Category> {
    override fun CategoryEntity.mapToDomainModel(): Category {
        return Category(
            firestoreDocId = firestoreDocId,
            categoryName = categoryName
        )
    }

    override fun Category.mapFromDomainModel(): CategoryEntity {
        return CategoryEntity(
            firestoreDocId = firestoreDocId,
            categoryName = categoryName
        )
    }

    fun List<CategoryEntity>.toDomainModelList(): List<Category> {
        return this.map { it.mapToDomainModel() } // Calls the extension within the object's scope
    }

    fun List<Category>.toEntityList(): List<CategoryEntity> {
        return this.map { it.mapFromDomainModel() } // Calls the extension within the object's scope
    }



}
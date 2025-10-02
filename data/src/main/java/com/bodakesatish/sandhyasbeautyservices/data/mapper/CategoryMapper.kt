package com.bodakesatish.sandhyasbeautyservices.data.mapper

import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CategoryEntity
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category

object CategoryMapper : Mapper<CategoryEntity, Category> {
    override fun CategoryEntity.mapToDomainModel(): Category {
        return Category(
            id = id,
            categoryName = categoryName,
            categoryDescription = categoryDescription
        )
    }

    override fun Category.mapFromDomainModel(): CategoryEntity {
        return CategoryEntity(
            id = id,
            categoryName = categoryName,
            categoryDescription = categoryDescription
        )
    }

}
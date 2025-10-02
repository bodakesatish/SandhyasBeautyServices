package com.bodakesatish.sandhyasbeautyservices.domain.model

import java.io.Serializable

data class Category(
    val id: Int = 0,
    var categoryName: String = "",
    var categoryDescription: String = ""
) : Serializable
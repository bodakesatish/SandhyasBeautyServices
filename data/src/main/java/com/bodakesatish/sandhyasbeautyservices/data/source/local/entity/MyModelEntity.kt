package com.bodakesatish.sandhyasbeautyservices.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MyModelEntity(val name: String) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}
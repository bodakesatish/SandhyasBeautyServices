package com.bodakesatish.sandhyasbeautyservices.data.mapper

interface Mapper<From, To> {
    fun From.mapToDomainModel(): To
    fun To.mapFromDomainModel(): From
}
package com.flatcode.littlebooks.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "slider")
data class SliderEntity(
    @PrimaryKey var id: String = "",
    var image: String = "",
    var position: Int = 0
)

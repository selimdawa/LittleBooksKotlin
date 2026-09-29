package com.flatcode.littlebooks.model

import androidx.room.Entity

@Entity(tableName = "favorites", primaryKeys = ["userId", "bookId"])
data class FavoriteEntity(
    val userId: String = "",
    val bookId: String = ""
)

package com.flatcode.littlebooks.model

import androidx.room.Entity

@Entity(tableName = "interested", primaryKeys = ["userId", "databaseName", "itemId"])
data class InterestedEntity(
    val userId: String = "",
    val databaseName: String = "",
    val itemId: String = ""
)

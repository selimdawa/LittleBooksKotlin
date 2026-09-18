package com.flatcode.littlebooksadmin.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    var id: String = "",
    var category: String? = null,
    var image: String? = null,
    var publisher: String? = null,
    var timestamp: Long = 0
) : Serializable

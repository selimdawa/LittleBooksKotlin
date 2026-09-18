package com.flatcode.littlebooksadmin.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey
    var id: String = "",
    var bookId: String? = null,
    var timestamp: Long = 0,
    var comment: String? = null,
    var publisher: String? = null
) : Serializable

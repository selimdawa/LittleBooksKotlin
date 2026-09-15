package com.flatcode.littlebooks.Model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey
    var id: String = "",
    var bookId: String? = null,
    var timestamp: Long = 0,
    var comment: String? = null,
    var publisher: String? = null
) : Parcelable
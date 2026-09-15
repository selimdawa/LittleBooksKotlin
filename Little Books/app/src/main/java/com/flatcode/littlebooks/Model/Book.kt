package com.flatcode.littlebooks.Model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "books")
data class Book(
    @PrimaryKey
    var id: String = "",
    var publisher: String? = null,
    var title: String? = null,
    var description: String? = null,
    var categoryId: String? = null,
    var url: String? = null,
    var image: String? = null,
    var timestamp: Long = 0,
    var viewsCount: Int = 0,
    var downloadsCount: Int = 0,
    var lovesCount: Int = 0,
    var editorsChoice: Int = 0
) : Parcelable {
    // No-arg constructor for Firebase/Room if needed, 
    // though data class with defaults handles it.
}
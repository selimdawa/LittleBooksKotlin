package com.flatcode.littlebooksadmin.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "users")
data class User(
    @PrimaryKey var id: String = "",
    var username: String? = null,
    var profileImage: String? = null,
    var email: String? = null,
    var timestamp: Long = 0,
    var version: Int = 0,
    var booksCount: Int = 0,
    var adLoad: Int = 0,
    var adClick: Int = 0
) : Parcelable
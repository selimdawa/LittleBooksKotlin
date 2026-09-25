package com.flatcode.littlebooksadmin.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    var id: String = "",
    var category: String? = null,
    var image: String? = null,
    var publisher: String? = null,
    var timestamp: Long = 0
) : Parcelable

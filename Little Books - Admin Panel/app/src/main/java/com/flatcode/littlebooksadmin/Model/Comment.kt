package com.flatcode.littlebooksadmin.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    var id: String = "",
    var bookId: String? = null,
    var timestamp: Long = 0,
    var comment: String? = null,
    var publisher: String? = null
) : Parcelable

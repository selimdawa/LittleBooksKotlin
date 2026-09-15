package com.flatcode.littlebooksadmin.data.model

data class Comment(
    var id: String? = null,
    var bookId: String? = null,
    var timestamp: Long = 0,
    var comment: String? = null,
    var publisher: String? = null
)

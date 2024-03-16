package com.flatcode.littlebooksadmin.Model

class Comment {
    var id: String? = null
    var bookId: String? = null
    var timestamp: Long = 0
    var comment: String? = null
    var publisher: String? = null

    constructor()

    constructor(
        id: String?, bookId: String?, timestamp: Long, comment: String?, publisher: String?,
    ) {
        this.id = id
        this.bookId = bookId
        this.timestamp = timestamp
        this.comment = comment
        this.publisher = publisher
    }
}
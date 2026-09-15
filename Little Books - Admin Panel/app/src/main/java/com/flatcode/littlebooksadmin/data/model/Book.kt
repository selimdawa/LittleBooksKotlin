package com.flatcode.littlebooksadmin.data.model

data class Book(
    var publisher: String? = null,
    var id: String? = null,
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
)

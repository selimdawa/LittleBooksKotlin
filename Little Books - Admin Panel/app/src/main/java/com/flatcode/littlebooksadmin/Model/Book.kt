package com.flatcode.littlebooksadmin.Modelimport

class Book {
    var publisher: String? = null
    var id: String? = null
    var title: String? = null
    var description: String? = null
    var categoryId: String? = null
    var url: String? = null
    var image: String? = null
    var timestamp: Long = 0
    var viewsCount = 0
    var downloadsCount = 0
    var lovesCount = 0
    var editorsChoice = 0

    constructor()

    constructor(
        publisher: String?, id: String?, title: String?, description: String?, categoryId: String?,
        url: String?, image: String?, timestamp: Long, viewsCount: Int, downloadsCount: Int,
        lovesCount: Int, editorsChoice: Int
    ) {
        this.publisher = publisher
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.url = url
        this.image = image
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.downloadsCount = downloadsCount
        this.lovesCount = lovesCount
        this.editorsChoice = editorsChoice
    }
}
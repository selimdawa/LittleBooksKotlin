package com.flatcode.littlebooks.Model

class Category {
    var id: String? = null
    var category: String? = null
    var image: String? = null
    var publisher: String? = null
    var timestamp: Long = 0

    constructor()

    constructor(
        id: String?, category: String?, image: String?, publisher: String?, timestamp: Long
    ) {
        this.id = id
        this.category = category
        this.publisher = publisher
        this.image = image
        this.timestamp = timestamp
    }
}
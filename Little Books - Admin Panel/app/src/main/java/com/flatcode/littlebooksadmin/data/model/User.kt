package com.flatcode.littlebooksadmin.data.model

data class User(
    var id: String? = null,
    var username: String? = null,
    var profileImage: String? = null,
    var email: String? = null,
    var timestamp: Long = 0,
    var version: Int = 0,
    var booksCount: Int = 0,
    var adLoad: Int = 0,
    var adClick: Int = 0
)

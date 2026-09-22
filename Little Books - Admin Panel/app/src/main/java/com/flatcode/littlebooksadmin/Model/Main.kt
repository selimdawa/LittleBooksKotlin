package com.flatcode.littlebooksadmin.model

import java.io.Serializable

data class Main(
    var image: Int = 0, var title: String? = null, var number: Int = 0, var c: Class<*>? = null
) : Serializable
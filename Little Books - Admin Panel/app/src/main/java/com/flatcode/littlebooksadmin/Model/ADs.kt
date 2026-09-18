package com.flatcode.littlebooksadmin.model

import java.io.Serializable

data class ADs(
    var name: String? = null,
    var adsLoadedCount: Int = 0,
    var adsClickedCount: Int = 0
) : Serializable

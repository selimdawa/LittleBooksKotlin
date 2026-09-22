package com.flatcode.littlebooksadmin.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ADs(
    var name: String? = null, var adsLoadedCount: Int = 0, var adsClickedCount: Int = 0
) : Parcelable
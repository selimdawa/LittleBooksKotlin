package com.flatcode.littlebooks.Model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ads")
data class ADs(
    @PrimaryKey
    var name: String = "",
    var adsLoadedCount: Int = 0,
    var adsClickedCount: Int = 0
) : Parcelable
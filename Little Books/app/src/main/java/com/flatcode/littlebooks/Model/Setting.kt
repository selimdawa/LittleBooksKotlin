package com.flatcode.littlebooks.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey var id: String = "",
    var name: String? = null,
    var image: Int = 0,
    var number: Int = 0,
    var c: @RawValue Class<*>? = null
) : Parcelable
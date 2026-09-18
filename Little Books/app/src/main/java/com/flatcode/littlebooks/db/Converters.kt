package com.flatcode.littlebooks.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromClassName(value: String?): Class<*>? {
        return value?.let {
            try {
                Class.forName(it)
            } catch (e: ClassNotFoundException) {
                null
            }
        }
    }

    @TypeConverter
    fun toClassName(clazz: Class<*>?): String? {
        return clazz?.name
    }
}



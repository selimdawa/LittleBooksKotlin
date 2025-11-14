package com.flatcode.littlebooks

import android.app.Application
import android.text.format.DateFormat
import java.util.*

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        fun formatTimestamp(timestamp: Long): String {
            val calendar = Calendar.getInstance(Locale.ENGLISH)
            calendar.timeInMillis = timestamp
            return DateFormat.format("dd/MM/yyyy", calendar).toString()
        }
    }
}
package com.flatcode.littlebooksadmin

import android.app.Application
import android.text.format.DateFormat
import java.util.Calendar
import java.util.Locale

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
package com.flatcode.littlebooks

import android.app.Application
import android.text.format.DateFormat
import io.selimdawa.multicolors.MultiColorManager
import java.util.Calendar
import java.util.Locale

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        MultiColorManager.init(this)
    }

    companion object {
        fun formatTimestamp(timestamp: Long): String {
            val calendar = Calendar.getInstance(Locale.ENGLISH)
            calendar.timeInMillis = timestamp
            return DateFormat.format("dd/MM/yyyy", calendar).toString()
        }
    }
}
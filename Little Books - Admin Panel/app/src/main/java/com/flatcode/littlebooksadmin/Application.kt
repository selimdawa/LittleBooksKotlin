package com.flatcode.littlebooksadmin

import android.app.Application
import io.selimdawa.multicolors.MultiColorManager

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
package com.flatcode.littlebooksadmin

import android.app.Application
import android.text.format.DateFormat
import com.cloudinary.android.MediaManager
import com.flatcode.littlebooksadmin.utils.DATA
import dagger.hilt.android.HiltAndroidApp
import io.selimdawa.multicolors.MultiColorManager
import timber.log.Timber

@HiltAndroidApp
class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        MultiColorManager.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val config = HashMap<String, String>()
        config["cloud_name"] = DATA.CLOUDINARY_CLOUD_NAME
        MediaManager.init(this, config)
    }

    companion object {
        fun formatTimestamp(timestamp: Long): String {
            return DateFormat.format("dd/MM/yyyy", timestamp).toString()
        }
    }
}
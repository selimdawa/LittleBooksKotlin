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

        val config = mapOf(
            "cloud_name" to DATA.CLOUDINARY_CLOUD_NAME,
            "upload_preset" to DATA.CLOUDINARY_UPLOAD_PRESET
        )
        MediaManager.init(this, config)
    }

    companion object {
        fun formatTimestamp(timestamp: Long): String {
            return DateFormat.format("dd/MM/yyyy", timestamp).toString()
        }
    }
}
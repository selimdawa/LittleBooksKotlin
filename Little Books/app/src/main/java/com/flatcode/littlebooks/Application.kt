package com.flatcode.littlebooks

import android.app.Application
import com.cloudinary.android.MediaManager
import com.flatcode.littlebooks.utils.DATA
import dagger.hilt.android.HiltAndroidApp
import io.selimdawa.multicolors.MultiColorManager
import timber.log.Timber
import com.flatcode.littlebooks.BuildConfig

@HiltAndroidApp
class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        MultiColorManager.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Cloudinary Initialization
        val config = mapOf(
            "cloud_name" to DATA.CLOUDINARY_CLOUD_NAME, "secure" to true
        )
        MediaManager.init(this, config)
    }
}
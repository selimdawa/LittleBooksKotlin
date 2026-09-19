package com.flatcode.littlebooks

import android.app.Application
import android.text.format.DateFormat
import com.flatcode.littlebooks.utils.DATA
import dagger.hilt.android.HiltAndroidApp
import io.selimdawa.multicolors.MultiColorManager
import java.util.Calendar
import java.util.Locale

@HiltAndroidApp
class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        MultiColorManager.init(this)
    }

    // Cloudinary Initialization
    val config = mapOf(
        "cloud_name" to DATA.CLOUDINARY_CLOUD_NAME, "secure" to true
    )
    MediaManager.init(this, config)
}
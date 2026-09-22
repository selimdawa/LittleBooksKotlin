package com.flatcode.littlebooks.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtils {
    val storagePermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun checkStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, storagePermission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
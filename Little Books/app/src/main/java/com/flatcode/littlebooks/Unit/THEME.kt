package com.flatcode.littlebooks.Unit

import android.content.Context
import androidx.preference.PreferenceManager
import com.flatcode.littlebooks.R

object THEME {
    fun setThemeOfApp(context: Context) {
        val sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context.applicationContext)
        if (sharedPreferences.getString("color_option", "BASIC") == "BASIC") {
            context.setTheme(R.style.Base_Theme_MainApp)
        //} else if (sharedPreferences.getString("color_option", "NIGHT_ONE") == "NIGHT_ONE") {
        //    context.setTheme(R.style.OneNightTheme)
        }
    }
}
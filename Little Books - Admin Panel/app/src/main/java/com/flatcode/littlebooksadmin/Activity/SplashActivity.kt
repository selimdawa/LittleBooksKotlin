package com.flatcode.littlebooksadmin.Activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooksadmin.Unit.CLASS
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ActivitySplashBinding
import com.flatcode.littlebooksadmin.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private var binding: ActivitySplashBinding? = null
    private val context: Context = this@SplashActivity
    
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        Handler(Looper.getMainLooper()).postDelayed({ 
            checkUser() 
        }, (TIME_PER_MILLIS * TIME_PER_SECOND).toLong())
    }

    private fun checkUser() {
        if (viewModel.isUserLoggedIn()) {
            VOID.IntentClear(context, CLASS.MAIN)
        } else {
            VOID.IntentClear(context, CLASS.LOGIN)
        }
        finish()
    }

    companion object {
        private const val TIME_PER_MILLIS = 1000
        private const val TIME_PER_SECOND = 2
    }
}

package com.flatcode.littlebooksadmin.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import com.flatcode.littlebooksadmin.utils.BaseActivity
import com.flatcode.littlebooksadmin.utils.*
import com.flatcode.littlebooksadmin.databinding.ActivitySplashBinding
import com.flatcode.littlebooksadmin.ui.auth.LoginActivity
import com.flatcode.littlebooksadmin.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val context: Context = this@SplashActivity
    
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({ 
            checkUser() 
        }, (TIME_PER_MILLIS * TIME_PER_SECOND).toLong())
    }

    private fun checkUser() {
        if (viewModel.isUserLoggedIn()) {
            context.openActivity<MainActivity>(clear = true)
        } else {
            context.openActivity<LoginActivity>(clear = true)
        }
        finish()
    }

    companion object {
        private const val TIME_PER_MILLIS = 1000
        private const val TIME_PER_SECOND = 2
    }
}

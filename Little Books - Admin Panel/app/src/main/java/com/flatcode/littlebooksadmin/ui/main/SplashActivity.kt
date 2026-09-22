package com.flatcode.littlebooksadmin.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.flatcode.littlebooksadmin.utils.*
import com.flatcode.littlebooksadmin.databinding.ActivitySplashBinding
import com.flatcode.littlebooksadmin.ui.auth.LoginActivity
import com.flatcode.littlebooksadmin.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val context: Context = this@SplashActivity
    
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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



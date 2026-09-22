package com.flatcode.littlebooks.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooks.databinding.ActivitySplashBinding
import com.flatcode.littlebooks.ui.auth.AuthActivity
import com.flatcode.littlebooks.ui.main.MainActivity
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private var binding: ActivitySplashBinding? = null
    var context: Context = this@SplashActivity
    private val viewModel: AuthViewModel by viewModels()

    var timeFinal = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        Handler(Looper.getMainLooper()).postDelayed({ checkUser() }, timeFinal.toLong())
    }

    private fun checkUser() {
        if (viewModel.getCurrentUser() == null) {
            context.openActivity<AuthActivity>()
        } else {
            context.openActivity<MainActivity>()
        }
        finish()
    }
}
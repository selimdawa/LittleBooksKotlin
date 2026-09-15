package com.flatcode.littlebooks.Activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooks.Auth.AuthActivity
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ActivitySplashBinding
import com.flatcode.littlebooks.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private var binding: ActivitySplashBinding? = null
    var context: Context = this@SplashActivity
    private val viewModel: AuthViewModel by viewModels()
    
    var time_per_second = 2
    var time_final = time_per_millis * time_per_second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        Handler(Looper.getMainLooper()).postDelayed({ checkUser() }, time_final.toLong())
    }

    private fun checkUser() {
        if (viewModel.getCurrentUser() == null) {
            VOID.Intent1(context, AuthActivity::class.java)
        } else {
            VOID.Intent1(context, MainActivity::class.java)
        }
        finish()
    }

    companion object {
        const val time_per_millis = 1000
    }
}
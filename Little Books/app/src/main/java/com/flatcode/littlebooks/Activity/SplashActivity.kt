package com.flatcode.littlebooks.Activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooks.Unit.CLASS
import com.flatcode.littlebooks.Unit.THEME
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private var binding: ActivitySplashBinding? = null
    var context: Context = this@SplashActivity
    var auth: FirebaseAuth? = null
    var time_per_second = 2
    var time_final = time_per_millis * time_per_second

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        Handler().postDelayed({ checkUser() }, time_final.toLong())
    }

    private fun checkUser() {
        //get current user, if logged in
        val firebaseUser = auth!!.currentUser
        if (firebaseUser == null) {
            VOID.Intent1(context, CLASS.AUTH)
        } else {
            VOID.Intent1(context, CLASS.MAIN)
        }
        finish()
    }

    companion object {
        const val time_per_millis = 1000
    }
}
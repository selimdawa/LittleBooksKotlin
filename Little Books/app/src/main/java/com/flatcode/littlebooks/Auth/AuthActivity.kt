package com.flatcode.littlebooks.Auth

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooks.Unit.CLASS
import com.flatcode.littlebooks.Unit.THEME
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private var binding: ActivityAuthBinding? = null
    var context: Context = this@AuthActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        binding!!.loginBtn.setOnClickListener { VOID.Intent1(context, CLASS.LOGIN) }
        binding!!.skipBtn.setOnClickListener { VOID.Intent1(context, CLASS.REGISTER) }
    }
}
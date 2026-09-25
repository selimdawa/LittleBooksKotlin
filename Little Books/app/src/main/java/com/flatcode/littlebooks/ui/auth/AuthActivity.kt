package com.flatcode.littlebooks.ui.auth

import android.content.Context
import android.os.Bundle
import com.flatcode.littlebooks.databinding.ActivityAuthBinding
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.openActivity

class AuthActivity : BaseActivity() {

    private var binding: ActivityAuthBinding? = null
    var context: Context = this@AuthActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        binding!!.loginBtn.setOnClickListener { context.openActivity<LoginActivity>() }
        binding!!.skipBtn.setOnClickListener { context.openActivity<RegisterActivity>() }
    }
}
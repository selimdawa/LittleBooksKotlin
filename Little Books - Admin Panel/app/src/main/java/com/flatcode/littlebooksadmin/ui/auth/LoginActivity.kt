package com.flatcode.littlebooksadmin.ui.auth

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import com.flatcode.littlebooksadmin.utils.BaseActivity
import androidx.appcompat.app.AlertDialog
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityLoginBinding
import com.flatcode.littlebooksadmin.ui.main.MainActivity
import com.flatcode.littlebooksadmin.utils.openActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val context: Context = this@LoginActivity
    private var dialog: AlertDialog? = null

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        binding.forget.setOnClickListener { context.openActivity<ForgetPasswordActivity>() }
        binding.loginBtn.setOnClickListener { validateData() }
    }

    private fun validateData() {
        val email = binding.emailEt.text.toString().trim()
        val password = binding.passwordEt.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, R.string.invalid_email, Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(context, R.string.enter_password, Toast.LENGTH_SHORT).show()
        } else {
            dialog = AlertDialog.Builder(context).apply {
                setMessage(getString(R.string.logging_in))
            }.show()
            viewModel.login(email, password) { success, message ->
                dialog?.dismiss()
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                if (success) {
                    context.openActivity<MainActivity>(clear = true)
                }
            }
        }
    }
}
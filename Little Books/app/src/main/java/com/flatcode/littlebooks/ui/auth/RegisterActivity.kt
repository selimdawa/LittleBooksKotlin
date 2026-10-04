package com.flatcode.littlebooks.ui.auth

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.databinding.ActivityRegisterBinding
import com.flatcode.littlebooks.ui.main.MainActivity
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.ProgressDialog
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : BaseActivity() {

    private var binding: ActivityRegisterBinding? = null
    var context: Context = this@RegisterActivity
    private var dialog: ProgressDialog? = null

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        dialog = ProgressDialog(this).apply {
            setTitle("Please wait...")
            setCanceledOnTouchOutside(false)
        }

        binding!!.login.setOnClickListener {
            context.openActivity<LoginActivity>()
            finish()
        }
        binding!!.forget.setOnClickListener { context.openActivity<ForgetPasswordActivity>() }
        binding!!.go.setOnClickListener { validateData() }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerStatus.collect { result ->
                    result?.let {
                        dialog?.dismiss()
                        if (it.isSuccess) {
                            Toast.makeText(context, "Account created...", Toast.LENGTH_SHORT).show()
                            context.openActivity<MainActivity>(true)
                            finish()
                        } else {
                            Toast.makeText(context, it.exceptionOrNull()?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.resetStatus()
                    }
                }
            }
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""
    private fun validateData() {
        name = binding!!.nameEt.text.toString().trim()
        email = binding!!.emailEt.text.toString().trim()
        password = binding!!.passwordEt.text.toString().trim()
        val cPassword = binding!!.cPasswordEt.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Enter you name...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Invalid email pattern...!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Enter password...!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(context, "Confirm Password...!", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            Toast.makeText(context, "Password doesn't match...!", Toast.LENGTH_SHORT).show()
        } else {
            dialog?.setMessage("Creating account...")
            dialog?.show()
            viewModel.register(name, email, password)
        }
    }
}

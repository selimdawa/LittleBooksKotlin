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
import com.flatcode.littlebooks.databinding.ActivityLoginBinding
import com.flatcode.littlebooks.ui.main.MainActivity
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.ProgressDialog
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private var binding: ActivityLoginBinding? = null
    var context: Context = this@LoginActivity
    private var dialog: ProgressDialog? = null

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        dialog = ProgressDialog(this).apply {
            setTitle("Please wait...")
            setCanceledOnTouchOutside(false)
        }

        binding!!.forget.setOnClickListener { context.openActivity<ForgetPasswordActivity>() }
        binding!!.noAccount.setOnClickListener { context.openActivity<RegisterActivity>() }
        binding!!.loginBtn.setOnClickListener { validateDate() }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginStatus.collect { result ->
                    result?.let {
                        dialog?.dismiss()
                        if (it.isSuccess) {
                            context.openActivity<MainActivity>(true)
                            finish()
                        } else {
                            Toast.makeText(context, it.exceptionOrNull()?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.resetStatus()
                    }
                }
            }
        }
    }

    private var email = ""
    private var password = ""
    private fun validateDate() {
        email = binding!!.emailEt.text.toString().trim()
        password = binding!!.passwordEt.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Invalid email pattern...!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Enter password...!", Toast.LENGTH_SHORT).show()
        } else {
            dialog?.setMessage("Logging In...")
            dialog?.show()
            viewModel.login(email, password)
        }
    }
}

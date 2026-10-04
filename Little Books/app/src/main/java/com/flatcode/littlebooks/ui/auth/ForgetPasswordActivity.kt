package com.flatcode.littlebooks.ui.auth

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.databinding.ActivityForgetPasswordBinding
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.ProgressDialog
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgetPasswordActivity : BaseActivity() {

    private var binding: ActivityForgetPasswordBinding? = null
    private val context: Context = this@ForgetPasswordActivity
    private var dialog: ProgressDialog? = null

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        dialog = ProgressDialog(this).apply {
            setTitle("Please wait...")
            setCanceledOnTouchOutside(false)
        }

        binding!!.noAccount.setOnClickListener {
            context.openActivity<RegisterActivity>()
            finish()
        }
        binding!!.login.setOnClickListener {
            context.openActivity<LoginActivity>()
            finish()
        }
        binding!!.go.setOnClickListener { validateDate() }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.forgetPasswordStatus.collect { result ->
                    result?.let {
                        dialog?.dismiss()
                        if (it.isSuccess) {
                            Toast.makeText(
                                context, "Instructions to reset password sent", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(context, it.exceptionOrNull()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.resetStatus()
                    }
                }
            }
        }
    }

    private var email = ""
    private fun validateDate() {
        email = binding!!.emailEt.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(context, "Enter email...!", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Invalid email format...!", Toast.LENGTH_SHORT).show()
        } else {
            dialog?.setMessage("Sending password recovery instructions...")
            dialog?.show()
            viewModel.forgetPassword(email)
        }
    }
}

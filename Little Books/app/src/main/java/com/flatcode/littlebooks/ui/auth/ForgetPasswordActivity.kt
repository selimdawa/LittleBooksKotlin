package com.flatcode.littlebooks.ui.auth

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.databinding.ActivityForgetPasswordBinding
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgetPasswordActivity : BaseActivity() {

    private var binding: ActivityForgetPasswordBinding? = null
    private val context: Context = this@ForgetPasswordActivity
    private var dialog: AlertDialog? = null

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        dialog = AlertDialog.Builder(this)
            .setTitle("Please wait...")
            .setCancelable(false)
            .create()

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
                viewModel.forgetPasswordStatus.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            dialog!!.dismiss()
                            Toast.makeText(
                                context, "Instructions to reset password sent", Toast.LENGTH_SHORT
                            ).show()
                        }

                        is Resource.Error -> {
                            dialog!!.dismiss()
                            Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        }

                        is Resource.Loading -> {
                            dialog!!.setMessage("Sending password recovery instructions...")
                            dialog!!.show()
                        }

                        null -> {}
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
            viewModel.forgetPassword(email)
        }
    }
}
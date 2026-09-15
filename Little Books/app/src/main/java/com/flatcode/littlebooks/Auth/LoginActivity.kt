package com.flatcode.littlebooks.Auth

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.Unit.CLASS
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ActivityLoginBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private var binding: ActivityLoginBinding? = null
    var context: Context = this@LoginActivity
    private var dialog: ProgressDialog? = null

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        dialog = ProgressDialog(this)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding!!.forget.setOnClickListener { VOID.Intent1(context, CLASS.FORGET_PASSWORD) }
        binding!!.noAccount.setOnClickListener { VOID.Intent1(context, CLASS.REGISTER) }
        binding!!.go.setOnClickListener { validateDate() }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginStatus.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            dialog!!.dismiss()
                            VOID.IntentClear(context, CLASS.MAIN)
                            finish()
                        }
                        is Resource.Error -> {
                            dialog!!.dismiss()
                            Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Loading -> {
                            dialog!!.setMessage("Logging In...")
                            dialog!!.show()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    private var email = ""
    private var password = ""
    private fun validateDate() {
        email = binding!!.emailEt.text.toString().trim { it <= ' ' }
        password = binding!!.passwordEt.text.toString().trim { it <= ' ' }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Invalid email pattern...!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Enter password...!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.login(email, password)
        }
    }
}
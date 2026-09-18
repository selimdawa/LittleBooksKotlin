package com.flatcode.littlebooks.ui.auth

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.utils.VOID
import com.flatcode.littlebooks.databinding.ActivityForgetPasswordBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgetPasswordActivity : AppCompatActivity() {

    private var binding: ActivityForgetPasswordBinding? = null
    private val context: Context = this@ForgetPasswordActivity
    private var dialog: ProgressDialog? = null

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.toolbarRl.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 20 // Original margin was 20sp
            }
            binding!!.go.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + 20 // Original margin was 20sp
            }
            insets
        }

        dialog = ProgressDialog(this)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding!!.noAccount.setOnClickListener {
            VOID.Intent1(context, RegisterActivity::class.java)
            finish()
        }
        binding!!.login.setOnClickListener {
            VOID.Intent1(context, LoginActivity::class.java)
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
        email = binding!!.emailEt.text.toString().trim { it <= ' ' }
        if (email.isEmpty()) {
            Toast.makeText(context, "Enter email...!", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Invalid email format...!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.forgetPassword(email)
        }
    }
}




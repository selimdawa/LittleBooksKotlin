package com.flatcode.littlebooks.ui.auth

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
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
import com.flatcode.littlebooks.ui.main.MainActivity
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.databinding.ActivityLoginBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private var binding: ActivityLoginBinding? = null
    var context: Context = this@LoginActivity
    private var dialog: AlertDialog? = null

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.toolbarRl.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 20 // Original margin was 20sp
            }
            binding!!.loginBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + 20 // Original margin was 20sp
            }
            insets
        }

        dialog = AlertDialog.Builder(this)
            .setTitle("Please wait...")
            .setCancelable(false)
            .create()

        binding!!.forget.setOnClickListener { context.openActivity<ForgetPasswordActivity>() }
        binding!!.noAccount.setOnClickListener { context.openActivity<RegisterActivity>() }
        binding!!.loginBtn.setOnClickListener { validateDate() }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginStatus.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            dialog!!.dismiss()
                            context.openActivity<MainActivity>(true)
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




package com.flatcode.littlebooksadmin.ui.auth

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityForgetPasswordBinding
import com.flatcode.littlebooksadmin.utils.createProgressDialog
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding
    private val context: Context = this@ForgetPasswordActivity
    private var auth: FirebaseAuth? = null
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        dialog = createProgressDialog(getString(R.string.please_wait))

        binding.go.setOnClickListener { validateDate() }
        binding.login.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private var email = ""
    private fun validateDate() {
        email = binding.emailEt.text.toString().trim { it <= ' ' }
        if (email.isEmpty()) {
            Toast.makeText(context, R.string.enter_email, Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, R.string.invalid_email, Toast.LENGTH_SHORT).show()
        } else {
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        dialog = createProgressDialog(
            getString(R.string.sending_password_recovery, email)
        )
        dialog!!.show()
        auth!!.sendPasswordResetEmail(email).addOnCompleteListener {
            dialog!!.dismiss()
            Toast.makeText(
                context, getString(R.string.instructions_sent, email), Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener { e: Exception ->
            dialog!!.dismiss()
            Toast.makeText(
                context, getString(R.string.failed_to_send, e.message), Toast.LENGTH_SHORT
            ).show()
        }
    }
}
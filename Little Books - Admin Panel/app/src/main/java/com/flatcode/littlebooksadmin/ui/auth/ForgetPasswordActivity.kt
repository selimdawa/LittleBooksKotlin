package com.flatcode.littlebooksadmin.ui.auth

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flatcode.littlebooksadmin.utils.BaseActivity
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityForgetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding
    private val context: Context = this@ForgetPasswordActivity
    private var auth: FirebaseAuth? = null
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

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
        dialog = AlertDialog.Builder(context).apply {
            setMessage(getString(R.string.sending_password_recovery, email))
        }.show()
        auth!!.sendPasswordResetEmail(email).addOnCompleteListener {
            dialog?.dismiss()
            Toast.makeText(
                context, getString(R.string.instructions_sent, email), Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener { e: Exception ->
            dialog?.dismiss()
            Toast.makeText(
                context, getString(R.string.failed_to_send, e.message), Toast.LENGTH_SHORT
            ).show()
        }
    }
}
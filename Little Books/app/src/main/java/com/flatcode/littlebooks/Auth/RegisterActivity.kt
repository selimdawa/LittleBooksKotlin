package com.flatcode.littlebooks.Auth

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooks.Unit.CLASS
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.THEME
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ActivityRegisterBinding
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private var binding: ActivityRegisterBinding? = null
    var context: Context = this@RegisterActivity
    private var auth: FirebaseAuth? = null
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        dialog = ProgressDialog(this)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding!!.login.setOnClickListener {
            VOID.Intent1(context, CLASS.LOGIN)
            finish()
        }
        binding!!.forget.setOnClickListener { VOID.Intent1(context, CLASS.FORGET_PASSWORD) }
        binding!!.go.setOnClickListener { validateData() }
    }

    private var name = ""
    private var email = ""
    private var password = ""
    private fun validateData() {

        //get data
        name = binding!!.nameEt.text.toString().trim { it <= ' ' }
        email = binding!!.emailEt.text.toString().trim { it <= ' ' }
        password = binding!!.passwordEt.text.toString().trim { it <= ' ' }
        val cPassword = binding!!.cPasswordEt.text.toString().trim { it <= ' ' }

        //validate data
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
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        //show progress
        dialog!!.setMessage("Creating account...")
        dialog!!.show()

        //create user in firebase auth
        auth!!.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { updateUserinfo() }
            .addOnFailureListener { e: Exception ->
                dialog!!.dismiss()
                Toast.makeText(context, DATA.EMPTY + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserinfo() {
        dialog!!.setMessage("Saving user info...")
        //get current user uid, since user is registered so we can get now
        val id = auth!!.uid

        //setup data to add in db
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.BOOKS_COUNT] = 0
        hashMap[DATA.AD_LOAD] = 0
        hashMap[DATA.AD_CLICK] = 0
        hashMap[DATA.EMAIL] = DATA.EMPTY + email
        hashMap[DATA.ID] = DATA.EMPTY + id
        hashMap[DATA.PROFILE_IMAGE] = DATA.EMPTY + DATA.BASIC
        hashMap[DATA.TIMESTAMP] = System.currentTimeMillis()
        hashMap[DATA.USER_NAME] = DATA.EMPTY + name
        hashMap[DATA.VERSION] = DATA.CURRENT_VERSION

        //set data to db
        val ref = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        assert(id != null)
        ref.child(id!!).setValue(hashMap).addOnSuccessListener {
            //data added to db
            dialog!!.dismiss()
            Toast.makeText(context, "Account created...", Toast.LENGTH_SHORT).show()
            VOID.IntentClear(context, CLASS.MAIN)
            finish()
        }.addOnFailureListener { e: Exception ->
            //data failed adding to db
            Toast.makeText(context, DATA.EMPTY + e.message, Toast.LENGTH_SHORT).show()
        }
    }
}
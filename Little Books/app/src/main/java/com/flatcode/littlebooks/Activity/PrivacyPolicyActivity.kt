package com.flatcode.littlebooks.Activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.THEME
import com.flatcode.littlebooks.databinding.ActivityPrivacyPolicyBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PrivacyPolicyActivity : AppCompatActivity() {

    private var binding: ActivityPrivacyPolicyBinding? = null
    var context: Context = this@PrivacyPolicyActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        binding!!.toolbar.nameSpace.setText(R.string.privacy_policy)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
    }

    private fun privacyPolicy() {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.TOOLS)
            .child(DATA.PRIVACY_POLICY)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.value.toString()
                binding!!.text.text = name
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onResume() {
        privacyPolicy()
        super.onResume()
    }

    override fun onRestart() {
        privacyPolicy()
        super.onRestart()
    }
}
package com.flatcode.littlebooksadmin.ui.settings

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.VOID
import com.flatcode.littlebooksadmin.databinding.ActivityPrivacyPolicyEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PrivacyPolicyEditActivity : AppCompatActivity() {

    private var binding: ActivityPrivacyPolicyEditBinding? = null
    var context: Context = this@PrivacyPolicyEditActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPrivacyPolicyEditBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding!!.toolbar.nameSpace.setText(R.string.privacy_policy)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.go.setOnClickListener { validateData() }

        privacyPolicy()
    }

    private var description = DATA.EMPTY
    private fun validateData() {
        description = binding!!.text.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(description)) {
            Toast.makeText(context, "Enter Privacy Policy...", Toast.LENGTH_SHORT).show()
        } else {
            update()
        }
    }

    private fun update() {
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.PRIVACY_POLICY] = DATA.EMPTY + description
        val ref = FirebaseDatabase.getInstance().getReference(DATA.TOOLS)
        ref.updateChildren(hashMap).addOnSuccessListener {
            Toast.makeText(context, "Privacy Policy updated...", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e: Exception ->
            Toast.makeText(context, DATA.EMPTY + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun privacyPolicy() {
        val reference =
            FirebaseDatabase.getInstance().reference.child(DATA.TOOLS).child(DATA.PRIVACY_POLICY)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.value.toString()
                binding!!.text.setText(name)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}


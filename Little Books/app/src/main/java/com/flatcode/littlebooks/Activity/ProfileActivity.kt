package com.flatcode.littlebooks.Activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooks.Model.Book
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.Unit.CLASS
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.THEME
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ActivityProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class ProfileActivity : AppCompatActivity() {

    private var binding: ActivityProfileBinding? = null
    var context: Context = this@ProfileActivity
    var profileId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val intent = intent
        profileId = intent.getStringExtra(DATA.PROFILE_ID)

        if (profileId == DATA.FirebaseUserUid) {
            binding!!.follow.visibility = View.GONE
            binding!!.editOrInfo.setImageResource(R.drawable.ic_edit_white)
            binding!!.editOrInfo.setOnClickListener { VOID.Intent1(context, CLASS.PROFILE_EDIT) }
        } else {
            binding!!.follow.visibility = View.VISIBLE
            binding!!.editOrInfo.setImageResource(R.drawable.ic_books)
            binding!!.editOrInfo.setOnClickListener {
                VOID.IntentExtra(context, CLASS.PROFILE_INFO, DATA.PROFILE_ID, profileId)
            }
        }
        isFollowing(binding!!.follow, profileId)
        binding!!.follow.setOnClickListener {
            if (binding!!.follow.tag == "add") {
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
                    .child(DATA.FirebaseUserUid)
                    .child(DATA.FOLLOWING).child(profileId!!).setValue(true)
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW).child(profileId!!)
                    .child(DATA.FOLLOWERS).child(DATA.FirebaseUserUid).setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
                    .child(DATA.FirebaseUserUid)
                    .child(DATA.FOLLOWING).child(profileId!!).removeValue()
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW).child(profileId!!)
                    .child(DATA.FOLLOWERS).child(DATA.FirebaseUserUid).removeValue()
            }
        }
        binding!!.back.setOnClickListener { onBackPressed() }
    }

    private fun init() {
        loadUserInfo()
        nrBooks
        nrItemFollow(DATA.FOLLOWERS, binding!!.numberFollowers)
        nrItemFollow(DATA.FOLLOWING, binding!!.numberFollowing)
        nrItemFavorites()
    }

    private fun loadUserInfo() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        reference.child(profileId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //String email = DATA.EMPTY + snapshot.child(DATA.EMAIL).getValue();
                val username = DATA.EMPTY + snapshot.child(DATA.USER_NAME).value
                val profileImage = DATA.EMPTY + snapshot.child(DATA.PROFILE_IMAGE).value
                //String timestamp = DATA.EMPTY + snapshot.child(DATA.TIMESTAMP).getValue();
                //String id = DATA.EMPTY + snapshot.child(DATA.ID).getValue();
                //String version = DATA.EMPTY + snapshot.child(DATA.VERSION).getValue();
                binding!!.username.text = username
                VOID.Glide_(true, context, profileImage, binding!!.profile)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private val nrBooks: Unit
        get() {
            val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (data in dataSnapshot.children) {
                        val item = data.getValue(Book::class.java)!!
                        if (item.publisher == profileId) i++
                    }
                    binding!!.numberBooks.text = MessageFormat.format("{0}{1}", DATA.EMPTY, i)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun nrItemFollow(type: String?, number: TextView) {
        val reference = FirebaseDatabase.getInstance().reference
            .child(DATA.FOLLOW).child(profileId!!).child(type!!)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                number.text = MessageFormat.format("{0}", dataSnapshot.childrenCount)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun nrItemFavorites() {
        val reference = FirebaseDatabase.getInstance().reference
            .child(DATA.FAVORITES).child(profileId!!)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                binding!!.numberFavorites.text =
                    MessageFormat.format("{0}", dataSnapshot.childrenCount)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun isFollowing(add: ImageView, userId: String?) {
        val reference = FirebaseDatabase.getInstance().reference
            .child(DATA.FOLLOW).child(DATA.FirebaseUserUid).child(DATA.FOLLOWING)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(userId!!).exists()) {
                    add.setImageResource(R.drawable.ic_heart_selected)
                    add.tag = "added"
                } else {
                    add.setImageResource(R.drawable.ic_heart_unselected)
                    add.tag = "add"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onResume() {
        init()
        super.onResume()
    }

    override fun onRestart() {
        init()
        super.onRestart()
    }
}
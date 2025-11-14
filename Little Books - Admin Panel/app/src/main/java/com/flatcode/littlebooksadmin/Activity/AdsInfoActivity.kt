package com.flatcode.littlebooksadmin.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooksadmin.Adapter.ADsInfoAdapter
import com.flatcode.littlebooksadmin.Modelimport.ADs
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ActivityAdsInfoBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class AdsInfoActivity : AppCompatActivity() {

    private var binding: ActivityAdsInfoBinding? = null
    var context: Context = this@AdsInfoActivity
    var list: ArrayList<ADs?>? = null
    var adapter: ADsInfoAdapter? = null
    private var profileId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityAdsInfoBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val intent: Intent = intent
        profileId = intent.getStringExtra(DATA.PROFILE_ID)

        binding!!.toolbar.nameSpace.setText(R.string.info_ads)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

        //binding.recyclerView.setHasFixedSize(true);
        list = ArrayList()
        adapter = ADsInfoAdapter(context, list!!, true)
        binding!!.recyclerView.adapter = adapter
    }

    private fun loadUserInfo() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        reference.child(profileId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = DATA.EMPTY + snapshot.child(DATA.USER_NAME).value
                val profileImage = DATA.EMPTY + snapshot.child(DATA.PROFILE_IMAGE).value
                binding!!.username.text = username
                VOID.Glide(true, context, profileImage, binding!!.profileImage)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadAds(orderBy: String?) {
        val ref: Query = FirebaseDatabase.getInstance().getReference(DATA.AD_S).child(profileId!!)
        ref.orderByChild(orderBy!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list!!.clear()
                for (data in dataSnapshot.children) {
                    val item = data.getValue(ADs::class.java)!!
                    list!!.add(item)
                }
                adapter!!.notifyDataSetChanged()
                binding!!.progress.visibility = View.GONE
                if (list!!.isNotEmpty()) {
                    binding!!.recyclerView.visibility = View.VISIBLE
                    binding!!.emptyText.visibility = View.GONE
                } else {
                    binding!!.recyclerView.visibility = View.GONE
                    binding!!.emptyText.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onResume() {
        loadUserInfo()
        loadAds(DATA.NAME)
        super.onResume()
    }

    override fun onRestart() {
        loadUserInfo()
        loadAds(DATA.NAME)
        super.onRestart()
    }
}
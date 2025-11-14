package com.flatcode.littlebooks.Activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooks.Adapter.PublisherAdapter
import com.flatcode.littlebooks.Model.User
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.THEME
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ActivityPageStaggeredBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class ExplorePublishersActivity : AppCompatActivity() {

    private var binding: ActivityPageStaggeredBinding? = null
    private val context: Context = this@ExplorePublishersActivity
    var list: ArrayList<User?>? = null
    var adapter: PublisherAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityPageStaggeredBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        binding!!.toolbar.nameSpace.setText(R.string.explore_publishers)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.toolbar.close.setOnClickListener { onBackPressed() }
        VOID.BannerAd(context, binding!!.adView, DATA.BANNER_SMART_EXPLORE_PUBLISHERS)

        binding!!.toolbar.search.setOnClickListener {
            binding!!.toolbar.toolbar.visibility = View.GONE
            binding!!.toolbar.toolbarSearch.visibility = View.VISIBLE
            DATA.searchStatus = true
        }
        binding!!.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    adapter!!.filter.filter(s)
                } catch (e: Exception) {
                    //None
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        //binding.recyclerView.setHasFixedSize(true);
        list = ArrayList()
        adapter = PublisherAdapter(context, list!!)
        binding!!.recyclerView.adapter = adapter
    }

    private val data: Unit
        get() {
            val reference: Query = FirebaseDatabase.getInstance().getReference(DATA.USERS)
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    list!!.clear()
                    var i = 0
                    for (snapshot in dataSnapshot.children) {
                        val item = snapshot.getValue(User::class.java)!!
                        if (item.id != DATA.FirebaseUserUid) if (item.booksCount >= 1) {
                            list!!.add(item)
                            i++
                        }
                    }
                    binding!!.toolbar.number.text = MessageFormat.format("( {0} )", i)
                    binding!!.progress.visibility = View.GONE
                    if (list!!.isNotEmpty()) {
                        binding!!.recyclerView.visibility = View.VISIBLE
                        binding!!.emptyText.visibility = View.GONE
                    } else {
                        binding!!.recyclerView.visibility = View.GONE
                        binding!!.emptyText.visibility = View.VISIBLE
                    }
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    override fun onBackPressed() {
        if (DATA.searchStatus) {
            binding!!.toolbar.toolbar.visibility = View.VISIBLE
            binding!!.toolbar.toolbarSearch.visibility = View.GONE
            DATA.searchStatus = false
            binding!!.toolbar.textSearch.setText(DATA.EMPTY)
        } else super.onBackPressed()
    }

    override fun onResume() {
        data
        super.onResume()
    }

    override fun onRestart() {
        data
        super.onRestart()
    }
}
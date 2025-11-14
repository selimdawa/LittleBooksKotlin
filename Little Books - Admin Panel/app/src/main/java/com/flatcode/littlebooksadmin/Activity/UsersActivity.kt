package com.flatcode.littlebooksadmin.Activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooksadmin.Adapter.PublisherAdapter
import com.flatcode.littlebooksadmin.Modelimport.User
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.databinding.ActivityUsersBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class UsersActivity : AppCompatActivity() {

    private var binding: ActivityUsersBinding? = null
    private val context: Context = this@UsersActivity
    var list: ArrayList<User?>? = null
    var adapter: PublisherAdapter? = null
    var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        binding!!.toolbar.nameSpace.setText(R.string.users)
        binding!!.toolbar.close.setOnClickListener { onBackPressed() }
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

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

        binding!!.all.setOnClickListener {
            type = DATA.TIMESTAMP
            getData(DATA.ALL)
        }
        binding!!.users.setOnClickListener {
            type = DATA.VIEWS_COUNT
            getData(DATA.USER)
        }
        binding!!.publishers.setOnClickListener {
            type = DATA.LOVES_COUNT
            getData(DATA.PUBLISHER)
        }
    }

    private fun getData(type: String?) {
        val ref: Query = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        ref.orderByChild(DATA.USER_NAME)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    list!!.clear()
                    var i = 0
                    for (data in dataSnapshot.children) {
                        val item = data.getValue(User::class.java)!!
                        when (type) {
                            "all" -> {
                                list!!.add(item)
                                i++
                            }

                            "user" -> if (item.booksCount <= 0) {
                                list!!.add(item)
                                i++
                            }

                            "publisher" -> if (item.booksCount >= 1) {
                                list!!.add(item)
                                i++
                            }
                        }
                    }
                    list!!.reverse()
                    binding!!.toolbar.number.text = MessageFormat.format("( {0} )", i)
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

    override fun onBackPressed() {
        if (DATA.searchStatus) {
            binding!!.toolbar.toolbar.visibility = View.VISIBLE
            binding!!.toolbar.toolbarSearch.visibility = View.GONE
            DATA.searchStatus = false
            binding!!.toolbar.textSearch.setText(DATA.EMPTY)
        } else super.onBackPressed()
    }

    override fun onResume() {
        getData(DATA.ALL)
        super.onResume()
    }

    override fun onRestart() {
        getData(DATA.ALL)
        super.onRestart()
    }
}
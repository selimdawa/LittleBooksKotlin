package com.flatcode.littlebooksadmin.Activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooksadmin.Adapter.LinearBookAdapter
import com.flatcode.littlebooksadmin.Modelimport.Book
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.databinding.ActivityPageLinearSwitchBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class MyBooksActivity : AppCompatActivity() {

    private var binding: ActivityPageLinearSwitchBinding? = null
    private val context: Context = this@MyBooksActivity
    var list: ArrayList<Book?>? = null
    var adapter: LinearBookAdapter? = null
    var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityPageLinearSwitchBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        binding!!.toolbar.nameSpace.setText(R.string.my_books)
        binding!!.toolbar.close.setOnClickListener { onBackPressed() }
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        type = DATA.TIMESTAMP

        binding!!.toolbar.search.setOnClickListener {
            binding!!.toolbar.toolbar.visibility = View.GONE
            binding!!.toolbar.toolbarSearch.visibility = View.VISIBLE
            DATA.searchStatus = true
        }
        binding!!.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapter!!.filter.filter(s)
                } catch (e: Exception) {
                    //None
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //binding.recyclerView.setHasFixedSize(true);
        list = ArrayList()
        adapter = LinearBookAdapter(context, list!!, true)
        binding!!.recyclerView.adapter = adapter

        binding!!.switchBar.all.setOnClickListener {
            type = DATA.TIMESTAMP
            getData(type)
        }
        binding!!.switchBar.name.setOnClickListener {
            type = DATA.TITLE
            getData(type)
        }
        binding!!.switchBar.mostViews.setOnClickListener {
            type = DATA.VIEWS_COUNT
            getData(type)
        }
        binding!!.switchBar.mostLoves.setOnClickListener {
            type = DATA.LOVES_COUNT
            getData(type)
        }
        binding!!.switchBar.mostDownloads.setOnClickListener {
            type = DATA.DOWNLOADS_COUNT
            getData(type)
        }
    }

    private fun getData(orderBy: String?) {
        val ref: Query = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        ref.orderByChild(orderBy!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list!!.clear()
                var i = 0
                for (data in dataSnapshot.children) {
                    val item = data.getValue(Book::class.java)!!
                    if (item.publisher == DATA.FirebaseUserUid) {
                        list!!.add(item)
                        i++
                    }
                }
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
        getData(type)
        super.onResume()
    }

    override fun onRestart() {
        getData(type)
        super.onRestart()
    }
}
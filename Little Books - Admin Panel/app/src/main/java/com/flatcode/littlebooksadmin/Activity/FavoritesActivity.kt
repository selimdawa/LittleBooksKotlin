package com.flatcode.littlebooksadmin.Activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooksadmin.Adapter.StaggeredBookAdapter
import com.flatcode.littlebooksadmin.Modelimport.Book
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.databinding.ActivityPageStaggeredSwitchBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat
import kotlin.collections.reverse as reverse1

class FavoritesActivity : AppCompatActivity() {

    private var binding: ActivityPageStaggeredSwitchBinding? = null
    private val context: Context = this@FavoritesActivity
    var item: MutableList<String?>? = null
    var list: ArrayList<Book?>? = null
    var adapter: StaggeredBookAdapter? = null
    var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityPageStaggeredSwitchBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        binding!!.toolbar.nameSpace.setText(R.string.favorites)
        binding!!.toolbar.close.setOnClickListener { onBackPressed() }
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        type = DATA.TIMESTAMP

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
        adapter = StaggeredBookAdapter(context, list!!)
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
        item = ArrayList()
        val reference =
            FirebaseDatabase.getInstance().getReference(DATA.FAVORITES).child(DATA.FirebaseUserUid)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (item as ArrayList<String?>).clear()
                for (snapshot in dataSnapshot.children) {
                    (item as ArrayList<String?>).add(snapshot.key)
                }
                getBooks(orderBy)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getBooks(orderBy: String?) {
        val ref: Query = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        ref.orderByChild(orderBy!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list!!.clear()
                var i = 0
                for (snapshot in dataSnapshot.children) {
                    val pdf = snapshot.getValue(Book::class.java)
                    for (id in item!!) {
                        assert(pdf != null)
                        if (pdf!!.id != null) if (pdf.id == id) {
                            list!!.add(pdf)
                            i++
                        }
                    }
                }
                binding!!.toolbar.number.text = MessageFormat.format("( {0} )", i)
                binding!!.progress.visibility = View.GONE
                if (list!!.isNotEmpty()) {
                    binding!!.recyclerView.visibility = View.VISIBLE
                    binding!!.emptyText.visibility = View.GONE
                    list!!.reverse1()
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
        getData(type)
        super.onResume()
    }

    override fun onRestart() {
        getData(type)
        super.onRestart()
    }
}
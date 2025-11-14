package com.flatcode.littlebooks.Activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.Adapter.LinearBookAdapter
import com.flatcode.littlebooks.Model.Book
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.THEME
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ActivityPageLinearBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class MoreBooksActivity : AppCompatActivity() {

    private var binding: ActivityPageLinearBinding? = null
    private val context: Context = this@MoreBooksActivity
    var list: ArrayList<Book?>? = null
    var adapter: LinearBookAdapter? = null
    var type: String? = null
    var name: String? = null
    var isReverse: String? = null
    var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityPageLinearBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val intent = intent
        type = intent.getStringExtra(DATA.SHOW_MORE_TYPE)
        name = intent.getStringExtra(DATA.SHOW_MORE_NAME)
        isReverse = intent.getStringExtra(DATA.SHOW_MORE_BOOLEAN)

        binding!!.toolbar.nameSpace.text = name
        binding!!.toolbar.close.setOnClickListener { onBackPressed() }
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        VOID.BannerAd(context, binding!!.adView, DATA.BANNER_SMART_MORE_BOOKS)

        if (isReverse == "true") {
            recyclerView = binding!!.recyclerViewReverse
        } else if (isReverse == "false") {
            recyclerView = binding!!.recyclerView
        }
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

        //recyclerView.setHasFixedSize(true);
        list = ArrayList()
        adapter = LinearBookAdapter(context, list!!, false)
        recyclerView!!.adapter = adapter
    }

    private fun getData(orderBy: String?) {
        val ref: Query = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        ref.orderByChild(orderBy!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list!!.clear()
                var i = 0
                for (data in dataSnapshot.children) {
                    val item = data.getValue(Book::class.java)!!
                    if (orderBy == DATA.EDITORS_CHOICE) {
                        if (item.editorsChoice > 0) list!!.add(item)
                    } else list!!.add(item)
                    i++
                }
                binding!!.toolbar.number.text = MessageFormat.format("( {0} )", i)
                adapter!!.notifyDataSetChanged()
                binding!!.progress.visibility = View.GONE
                if (list!!.isNotEmpty()) {
                    recyclerView!!.visibility = View.VISIBLE
                    binding!!.emptyText.visibility = View.GONE
                } else {
                    recyclerView!!.visibility = View.GONE
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
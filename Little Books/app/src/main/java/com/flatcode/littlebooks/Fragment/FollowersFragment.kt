package com.flatcode.littlebooks.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flatcode.littlebooks.Adapter.LinearBookAdapter
import com.flatcode.littlebooks.Model.Book
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.FragmentFollowersBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class FollowersFragment : Fragment() {

    private var binding: FragmentFollowersBinding? = null
    var check: MutableList<String?>? = null
    var list: ArrayList<Book?>? = null
    var adapter: LinearBookAdapter? = null
    private var type: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFollowersBinding.inflate(LayoutInflater.from(context), container, false)

        type = DATA.TIMESTAMP
        VOID.BannerAd(context, binding!!.adView, DATA.BANNER_SMART_FOLLOWERS_BOOKS)

        //binding!!.recyclerView.setHasFixedSize(true)
        list = ArrayList()
        adapter = LinearBookAdapter(context, list!!, false)
        binding!!.recyclerView.adapter = adapter

        binding!!.switchBar.all.setOnClickListener {
            type = DATA.TIMESTAMP
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
        return binding!!.root
    }

    private fun getData(orderBy: String?) {
        check = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference(DATA.FOLLOW)
            .child(DATA.FirebaseUserUid).child(DATA.FOLLOWING)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (check as ArrayList<String?>).clear()
                for (snapshot in dataSnapshot.children) {
                    (check as ArrayList<String?>).add(snapshot.key)
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
                for (data in dataSnapshot.children) {
                    val item = data.getValue(Book::class.java)
                    for (id in check!!) {
                        assert(item != null)
                        if (item!!.publisher == id) list!!.add(item)
                    }
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
        getData(DATA.TIMESTAMP)
        super.onResume()
    }
}
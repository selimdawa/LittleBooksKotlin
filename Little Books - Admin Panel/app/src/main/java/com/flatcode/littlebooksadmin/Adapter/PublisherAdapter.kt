package com.flatcode.littlebooksadmin.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.Filter.PublisherFilter
import com.flatcode.littlebooksadmin.Modelimport.User
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.CLASS
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ItemPublisherBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class PublisherAdapter(private val context: Context, var list: ArrayList<User?>) :
    RecyclerView.Adapter<PublisherAdapter.ViewHolder>(), Filterable {

    private var binding: ItemPublisherBinding? = null
    var filterList: ArrayList<User?>
    private var filter: PublisherFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemPublisherBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = DATA.EMPTY + item!!.id
        val image = DATA.EMPTY + item.profileImage

        VOID.Glide(true, context, image, holder.image)

        if (item.username == DATA.EMPTY) {
            holder.username.visibility = View.GONE
        } else {
            holder.username.visibility = View.VISIBLE
            holder.username.text = item.username
        }
        if (item.id == DATA.FirebaseUserUid) {

            holder.add.visibility = View.GONE
        } else {
            holder.add.visibility = View.VISIBLE
        }

        NrFollowers(holder.numberFollowers, id)
        NrBooks(holder.numberBooks, id)
        isFollowing(holder.add, id)

        holder.add.setOnClickListener {
            if (holder.add.tag == "add") {
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
                    .child(DATA.FirebaseUserUid)
                    .child(DATA.FOLLOWING).child(id).setValue(true)
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW).child(id)
                    .child(DATA.FOLLOWERS).child(DATA.FirebaseUserUid).setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
                    .child(DATA.FirebaseUserUid)
                    .child(DATA.FOLLOWING).child(id).removeValue()
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW).child(id)
                    .child(DATA.FOLLOWERS).child(DATA.FirebaseUserUid).removeValue()
            }
        }

        holder.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.PROFILE, DATA.PROFILE_ID, item.id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = PublisherFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var image: ImageView
        var add: ImageView
        var username: TextView
        var numberBooks: TextView
        var numberFollowers: TextView
        var item: LinearLayout

        init {
            image = binding!!.imageProfile
            username = binding!!.username
            add = binding!!.add
            numberBooks = binding!!.numberBooks
            numberFollowers = binding!!.numberFollowers
            item = binding!!.item
        }
    }

    private fun isFollowing(add: ImageView, userId: String) {
        val reference = FirebaseDatabase.getInstance().reference
            .child(DATA.FOLLOW).child(DATA.FirebaseUserUid).child(DATA.FOLLOWING)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(userId).exists()) {
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

    private fun NrFollowers(numberConnected: TextView, userId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
            .child(userId).child(DATA.FOLLOWERS)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                numberConnected.text = MessageFormat.format("{0}", dataSnapshot.childrenCount)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun NrBooks(numberConnected: TextView, userId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.USERS).child(userId)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val number = DATA.EMPTY + dataSnapshot.child(DATA.BOOKS_COUNT).value
                numberConnected.text = number
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    init {
        filterList = list
    }
}
package com.flatcode.littlebooksadmin.ui.user

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
import com.flatcode.littlebooksadmin.filter.PublisherFilter
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.ui.profile.ProfileActivity
import com.flatcode.littlebooksadmin.utils.*
import com.flatcode.littlebooksadmin.databinding.ItemPublisherBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class PublisherAdapter(private val context: Context, var list: ArrayList<User?>) :
    RecyclerView.Adapter<PublisherAdapter.ViewHolder>(), Filterable {

    var filterList: ArrayList<User?>
    private var filter: PublisherFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPublisherBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = DATA.EMPTY + item!!.id
        val image = DATA.EMPTY + item.profileImage

        holder.binding.imageProfile.loadWithGlide(true, image)

        if (item.username == DATA.EMPTY) {
            holder.binding.username.visibility = View.GONE
        } else {
            holder.binding.username.visibility = View.VISIBLE
            holder.binding.username.text = item.username
        }
        if (item.id == DATA.FirebaseUserUid) {

            holder.binding.add.visibility = View.GONE
        } else {
            holder.binding.add.visibility = View.VISIBLE
        }

        NrFollowers(holder.binding.numberFollowers, id)
        NrBooks(holder.binding.numberBooks, id)
        isFollowing(holder.binding.add, id)

        holder.binding.add.setOnClickListener {
            if (holder.binding.add.tag == "add") {
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

        holder.binding.item.setOnClickListener {
            context.openActivity<ProfileActivity>(extras = arrayOf(DATA.PROFILE_ID to item.id))
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

    inner class ViewHolder(val binding: ItemPublisherBinding) : RecyclerView.ViewHolder(binding.root)

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


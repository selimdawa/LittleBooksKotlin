package com.flatcode.littlebooksadmin.ui.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ItemPublisherBinding
import com.flatcode.littlebooksadmin.filter.PublisherFilter
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.ui.profile.ProfileActivity
import com.flatcode.littlebooksadmin.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class PublisherAdapter : ListAdapter<User, PublisherAdapter.ViewHolder>(UserDiffCallback()), Filterable {

    var unfilteredList: List<User> = emptyList()
    private var filter: PublisherFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPublisherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val id = item.id
        val image = item.profileImage ?: ""

        holder.binding.imageProfile.loadWithGlide(true, image)

        if (item.username.isNullOrEmpty()) {
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

        nrFollowers(holder.binding.numberFollowers, id)
        nrBooks(holder.binding.numberBooks, id)
        isFollowing(holder.binding.add, id)

        holder.binding.add.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
            if (holder.binding.add.tag == "add") {
                ref.child(DATA.FirebaseUserUid).child(DATA.FOLLOWING).child(id).setValue(true)
                ref.child(id).child(DATA.FOLLOWERS).child(DATA.FirebaseUserUid).setValue(true)
            } else {
                ref.child(DATA.FirebaseUserUid).child(DATA.FOLLOWING).child(id).removeValue()
                ref.child(id).child(DATA.FOLLOWERS).child(DATA.FirebaseUserUid).removeValue()
            }
        }

        holder.binding.item.setOnClickListener {
            holder.itemView.context.openActivity<ProfileActivity>(extras = arrayOf(DATA.PROFILE_ID to item.id))
        }
    }

    fun submitUnfilteredList(list: List<User>?) {
        unfilteredList = list ?: emptyList()
        submitList(unfilteredList)
    }

    fun submitFilteredList(list: List<User>?) {
        submitList(list)
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = PublisherFilter(this)
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

    private fun nrFollowers(numberConnected: TextView, userId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
            .child(userId).child(DATA.FOLLOWERS)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                numberConnected.text = MessageFormat.format("{0}", dataSnapshot.childrenCount)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun nrBooks(numberConnected: TextView, userId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.USERS).child(userId)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val number = dataSnapshot.child(DATA.BOOKS_COUNT).value?.toString() ?: "0"
                numberConnected.text = number
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}

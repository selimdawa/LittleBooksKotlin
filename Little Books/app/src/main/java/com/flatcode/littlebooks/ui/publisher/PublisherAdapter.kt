package com.flatcode.littlebooks.ui.publisher

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
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.ItemPublisherBinding
import com.flatcode.littlebooks.filter.PublisherFilter
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.ui.profile.ProfileActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.glide
import com.flatcode.littlebooks.utils.intentExtra
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class PublisherAdapter : ListAdapter<User, PublisherAdapter.ViewHolder>(DiffCallback), Filterable {

    var originalList: List<User> = emptyList()
    private var filter: PublisherFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPublisherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = PublisherFilter(ArrayList(originalList), this)
        }
        return filter!!
    }

    fun submitFullList(list: List<User>?) {
        originalList = list ?: emptyList()
        submitList(originalList)
    }

    fun setFilteredList(list: List<User?>?) {
        submitList(list?.filterNotNull() ?: emptyList())
    }

    inner class ViewHolder(private val binding: ItemPublisherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: User) {
            val context = itemView.context
            val username = DATA.EMPTY + item.username
            val profileImage = DATA.EMPTY + item.profileImage

            binding.imageProfile.glide(true, profileImage)

            binding.username.visibility = if (username.isEmpty()) View.GONE else View.VISIBLE
            binding.username.text = username

            binding.add.visibility = if (item.id == DATA.FirebaseUserUid) View.GONE else View.VISIBLE

            nrFollowers(binding.numberFollowers, item.id)
            nrBooks(binding.numberBooks, item.id)
            isFollowing(binding.add, item.id)

            binding.add.setOnClickListener {
                val ref = FirebaseDatabase.getInstance().reference
                if (binding.add.tag == "add") {
                    ref.child(DATA.FOLLOW).child(DATA.FirebaseUserUid).child(DATA.FOLLOWING)
                        .child(item.id).setValue(true)
                    ref.child(DATA.FOLLOW).child(item.id).child(DATA.FOLLOWERS)
                        .child(DATA.FirebaseUserUid).setValue(true)
                } else {
                    ref.child(DATA.FOLLOW).child(DATA.FirebaseUserUid).child(DATA.FOLLOWING)
                        .child(item.id).removeValue()
                    ref.child(DATA.FOLLOW).child(item.id).child(DATA.FOLLOWERS)
                        .child(DATA.FirebaseUserUid).removeValue()
                }
            }

            itemView.setOnClickListener {
                context.intentExtra(ProfileActivity::class.java, DATA.PROFILE_ID, item.id)
            }
        }

        private fun isFollowing(add: ImageView, userId: String) {
            val reference = FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
                .child(DATA.FirebaseUserUid).child(DATA.FOLLOWING)
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
                    val number = DATA.EMPTY + dataSnapshot.child(DATA.BOOKS_COUNT).value
                    numberConnected.text = number
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem == newItem
        }
    }
}

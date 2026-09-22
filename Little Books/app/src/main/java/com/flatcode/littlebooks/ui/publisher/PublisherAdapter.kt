package com.flatcode.littlebooks.ui.publisher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.base.BaseListAdapter
import com.flatcode.littlebooks.databinding.ItemPublisherBinding
import com.flatcode.littlebooks.filter.PublisherFilter
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.loadImage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class PublisherAdapter(
    private val onItemClick: (User) -> Unit,
    private val onFollowClick: (User, Boolean) -> Unit
) : BaseListAdapter<User, ItemPublisherBinding>(DiffCallback), Filterable {

    var originalList: List<User> = emptyList()
    private var filter: PublisherFilter? = null

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemPublisherBinding {
        return ItemPublisherBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemPublisherBinding, item: User, position: Int) {
        val username = item.username ?: ""
        val profileImage = item.profileImage ?: ""

        binding.imageProfile.loadImage(true, profileImage)

        binding.username.visibility = if (username.isEmpty()) View.GONE else View.VISIBLE
        binding.username.text = username

        binding.add.visibility = if (item.id == DATA.FirebaseUserUid) View.GONE else View.VISIBLE

        nrFollowers(binding.numberFollowers, item.id)
        nrBooks(binding.numberBooks, item.id)
        isFollowing(binding.add, item.id)

        binding.add.setOnClickListener {
            onFollowClick(item, binding.add.tag == "added")
        }

        binding.root.setOnClickListener {
            onItemClick(item)
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

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem == newItem
        }
    }
}

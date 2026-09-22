package com.flatcode.littlebooks.ui.book

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.flatcode.littlebooks.base.BaseListAdapter
import com.flatcode.littlebooks.databinding.ItemCommentBinding
import com.flatcode.littlebooks.model.Comment
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.formatTimestamp
import com.flatcode.littlebooks.utils.loadImage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentAdapter(
    private val onItemClick: (Comment) -> Unit
) : BaseListAdapter<Comment, ItemCommentBinding>(DiffCallback) {

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemCommentBinding {
        return ItemCommentBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemCommentBinding, item: Comment, position: Int) {
        binding.date.text = item.timestamp.formatTimestamp()
        binding.comment.text = item.comment

        item.publisher?.let { loadUserDetails(it, binding) }

        binding.root.setOnClickListener {
            onItemClick(item)
        }
    }

    private fun loadUserDetails(publisher: String, binding: ItemCommentBinding) {
        FirebaseDatabase.getInstance().getReference(DATA.USERS).child(publisher)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        binding.profile.loadImage(true, it.profileImage)
                        binding.name.text = it.username
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem == newItem
        }
    }
}

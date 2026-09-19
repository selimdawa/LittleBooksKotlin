package com.flatcode.littlebooks.ui.book

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.databinding.ItemCommentBinding
import com.flatcode.littlebooks.model.Comment
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.formatTimestamp
import com.flatcode.littlebooks.utils.glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentAdapter : ListAdapter<Comment, CommentAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    inner class ViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Comment) {
            val context = itemView.context
            val bookId = DATA.EMPTY + item.bookId
            val comment = DATA.EMPTY + item.comment
            val publisher = DATA.EMPTY + item.publisher
            val timestamp = item.timestamp

            binding.date.text = timestamp.formatTimestamp()
            binding.comment.text = comment

            loadUserDetails(publisher, binding)

            itemView.setOnClickListener {
                if (publisher == DATA.FirebaseUserUid) deleteComment(item.id, bookId)
            }
        }

        private fun loadUserDetails(publisher: String?, binding: ItemCommentBinding) {
            val ref = FirebaseDatabase.getInstance().getReference(DATA.USERS)
            ref.child(publisher!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        binding.profile.glide(true, user.profileImage)
                        binding.name.text = user.username
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun deleteComment(commentId: String?, bookId: String?) {
            val context = itemView.context
            AlertDialog.Builder(context).setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("DELETE") { _, _ ->
                    val ref = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
                    ref.child(bookId!!).child(DATA.COMMENTS).child(commentId!!).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Deleted...", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { e: Exception ->
                            Toast.makeText(
                                context, "Failed to delete duo to " + e.message, Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss() }
                .show()
        }
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

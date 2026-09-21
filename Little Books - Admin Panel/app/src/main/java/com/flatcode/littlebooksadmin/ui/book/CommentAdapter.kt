package com.flatcode.littlebooksadmin.ui.book

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.Application
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ItemCommentBinding
import com.flatcode.littlebooksadmin.model.Comment
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.loadWithGlide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentAdapter : ListAdapter<Comment, CommentAdapter.ViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val commentId = item.id
        val bookId = item.bookId ?: ""
        val comment = item.comment ?: ""
        val publisher = item.publisher ?: ""
        val timestamp = item.timestamp

        val date = Application.formatTimestamp(timestamp)
        holder.binding.date.text = date
        holder.binding.comment.text = comment

        loadUserDetails(publisher, holder)

        holder.itemView.setOnClickListener {
            if (publisher == DATA.FirebaseUserUid) deleteComment(
                holder.itemView.context, commentId, bookId
            )
        }
    }

    private fun deleteComment(context: Context, commentId: String?, bookId: String?) {
        AlertDialog.Builder(context).setTitle(R.string.delete_comment)
            .setMessage(R.string.are_you_sure_delete_comment)
            .setPositiveButton(R.string.delete) { dialog, _ ->
                val ref = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
                ref.child(bookId!!).child(DATA.COMMENTS).child(commentId!!).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e: Exception ->
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_message, e.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                dialog.dismiss()
            }.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }.show()
    }

    class ViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    private fun loadUserDetails(publisher: String?, holder: ViewHolder) {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(publisher!!).exists()) {
                    val item = snapshot.child(publisher).getValue(User::class.java)!!
                    val username = item.username
                    val profileImage = item.profileImage
                    holder.binding.profile.loadWithGlide(true, profileImage!!)
                    holder.binding.name.text = username
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
}
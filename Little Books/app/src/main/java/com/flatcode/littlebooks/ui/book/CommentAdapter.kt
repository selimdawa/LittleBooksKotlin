package com.flatcode.littlebooks.ui.book

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flatcode.littlebooks.model.Comment
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.Application
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.glide
import com.flatcode.littlebooks.databinding.ItemCommentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentAdapter(private val context: Context, var list: ArrayList<Comment?>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = item!!.id
        val bookId = DATA.EMPTY + item.bookId
        val comment = DATA.EMPTY + item.comment
        val publisher = DATA.EMPTY + item.publisher
        val timestamp = DATA.EMPTY + item.timestamp

        val date: String = Application.formatTimestamp(timestamp.toLong())
        holder.binding.date.text = date
        holder.binding.comment.text = comment

        loadUserDetails(publisher, holder)

        holder.itemView.setOnClickListener {
            if (publisher == DATA.FirebaseUserUid) deleteComment(id, bookId)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder((binding as ViewBinding).root)

    private fun loadUserDetails(publisher: String?, holder: ViewHolder) {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(publisher!!).exists()) {
                    val item = snapshot.child(publisher).getValue(User::class.java)!!
                    val username = item.username
                    val profileImage = item.profileImage
                    holder.binding.profile.glide(true, profileImage)
                    holder.binding.name.text = username
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun deleteComment(commentId: String?, bookId: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("DELETE") { dialog: DialogInterface?, which: Int ->
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
            .setNegativeButton("CANCEL") { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            .show()
    }
}

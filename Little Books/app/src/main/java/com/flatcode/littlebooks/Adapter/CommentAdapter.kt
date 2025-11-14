package com.flatcode.littlebooks.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.Model.Comment
import com.flatcode.littlebooks.Model.User
import com.flatcode.littlebooks.MyApplication
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ItemCommentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentAdapter(private val context: Context, var list: ArrayList<Comment?>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = item!!.id
        val bookId = DATA.EMPTY + item.bookId
        val comment = DATA.EMPTY + item.comment
        val publisher = DATA.EMPTY + item.publisher
        val timestamp = DATA.EMPTY + item.timestamp

        val date: String = MyApplication.formatTimestamp(timestamp.toLong())
        holder.date.text = date
        holder.comment.text = comment

        loadUserDetails(publisher, holder.name)

        holder.itemView.setOnClickListener {
            if (publisher == DATA.FirebaseUserUid) deleteComment(id, bookId)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var profile: ImageView
        var name: TextView
        var comment: TextView
        var date: TextView
        var item: LinearLayout

        init {
            profile = binding!!.profile
            name = binding!!.name
            comment = binding!!.comment
            date = binding!!.date
            item = binding!!.item
        }
    }

    private fun loadUserDetails(publisher: String?, name: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(publisher!!).exists()) {
                    val item = snapshot.child(publisher).getValue(User::class.java)!!
                    val username = item.username
                    val profileImage = item.profileImage
                    VOID.Glide_(true, context, profileImage, binding!!.profile)
                    name.text = username
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

    companion object {
        private var binding: ItemCommentBinding? = null
    }
}
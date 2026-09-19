package com.flatcode.littlebooksadmin.ui.book

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.databinding.ItemBookEditorsChoiceBinding
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.EditorsChoice
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.dialogOptionDelete
import com.flatcode.littlebooksadmin.utils.loadWithGlide
import com.flatcode.littlebooksadmin.utils.openActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class EditorsChoiceAdapter :
    ListAdapter<EditorsChoice, EditorsChoiceAdapter.ViewHolder>(EditorsChoiceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemBookEditorsChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val id = position + 1
        val editorsChoiceId = DATA.EMPTY + id

        loadBookDetails(id, editorsChoiceId, holder)

        holder.binding.numberEditorsChoice.text = MessageFormat.format("{0}{1}", DATA.EMPTY, id)
        holder.binding.add.setOnClickListener {
            context.openActivity<EditorsChoiceAddActivity>(
                extras = arrayOf(
                    DATA.EDITORS_CHOICE_ID to editorsChoiceId, DATA.OLD_BOOK_ID to null
                )
            )
        }
    }

    class ViewHolder(val binding: ItemBookEditorsChoiceBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun loadBookDetails(i: Int, position: String, holder: ViewHolder) {
        val context = holder.itemView.context
        val ref = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val item = snapshot.getValue(Book::class.java)!!
                    if (item.editorsChoice == i) {
                        val id = DATA.EMPTY + item.id

                        loadBook(id, holder)
                        holder.binding.addCard.visibility = View.GONE
                        holder.binding.detailsCard.visibility = View.VISIBLE
                        holder.binding.remove.visibility = View.VISIBLE
                        holder.binding.change.visibility = View.VISIBLE
                        holder.binding.detailsCard.setOnClickListener {
                            context.openActivity<BookDetailsActivity>(extras = arrayOf(DATA.BOOK_ID to id))
                        }
                        holder.binding.remove.setOnClickListener {
                            context.dialogOptionDelete(
                                null, id, null, null, false, true, null, null
                            )
                        }
                        holder.binding.change.setOnClickListener {
                            context.openActivity<EditorsChoiceAddActivity>(
                                extras = arrayOf(
                                    DATA.EDITORS_CHOICE_ID to position, DATA.OLD_BOOK_ID to id
                                )
                            )
                        }
                    } else {
                        holder.binding.addCard.visibility = View.VISIBLE
                        holder.binding.detailsCard.visibility = View.GONE
                        holder.binding.remove.visibility = View.GONE
                        holder.binding.change.visibility = View.GONE
                    }
                }
            }

            private fun loadBook(text: String, holder: ViewHolder) {
                val ref = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
                ref.child(text).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val item = dataSnapshot.getValue(Book::class.java)!!
                        holder.binding.title.text = item.title
                        holder.binding.description.text = item.description
                        holder.binding.numberViews.text = item.viewsCount.toString()
                        holder.binding.numberLoves.text = item.lovesCount.toString()
                        holder.binding.numberDownloads.text = item.downloadsCount.toString()
                        holder.binding.image.loadWithGlide(false, item.image ?: "")
                        holder.binding.addCard.visibility = View.GONE
                        holder.binding.detailsCard.visibility = View.VISIBLE
                        holder.binding.remove.visibility = View.VISIBLE
                        holder.binding.change.visibility = View.VISIBLE
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

class EditorsChoiceDiffCallback : DiffUtil.ItemCallback<EditorsChoice>() {
    override fun areItemsTheSame(oldItem: EditorsChoice, newItem: EditorsChoice): Boolean {
        return oldItem === newItem // They are all placeholders, but for now strict identity
    }

    override fun areContentsTheSame(oldItem: EditorsChoice, newItem: EditorsChoice): Boolean {
        return true // Contents are identical for placeholders
    }
}
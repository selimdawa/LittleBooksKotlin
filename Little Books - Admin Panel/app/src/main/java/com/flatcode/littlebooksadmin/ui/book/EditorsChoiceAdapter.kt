package com.flatcode.littlebooksadmin.ui.book

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.EditorsChoice
import com.flatcode.littlebooksadmin.ui.book.BookDetailsActivity
import com.flatcode.littlebooksadmin.ui.book.EditorsChoiceAddActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.*
import com.flatcode.littlebooksadmin.databinding.ItemBookEditorsChoiceBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class EditorsChoiceAdapter(private val context: Context, var list: List<EditorsChoice>) :
    RecyclerView.Adapter<EditorsChoiceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookEditorsChoiceBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val id = position + 1
        val editorsChoiceId = DATA.EMPTY + id

        loadBookDetails(
            id, editorsChoiceId, holder
        )

        holder.binding.numberEditorsChoice.text = MessageFormat.format("{0}{1}", DATA.EMPTY, id)
        holder.binding.add.setOnClickListener {
            context.openActivity<EditorsChoiceAddActivity>(
                extras = arrayOf(DATA.EDITORS_CHOICE_ID to editorsChoiceId, DATA.OLD_BOOK_ID to null)
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val binding: ItemBookEditorsChoiceBinding) : RecyclerView.ViewHolder(binding.root)

    private fun loadBookDetails(
        i: Int, position: String, holder: ViewHolder
    ) {
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
                                null, id, null, null,
                                false, true, null, null
                            )
                        }
                        holder.binding.change.setOnClickListener {
                            context.openActivity<EditorsChoiceAddActivity>(
                                extras = arrayOf(DATA.EDITORS_CHOICE_ID to position, DATA.OLD_BOOK_ID to id)
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
                        //get data
                        val item = dataSnapshot.getValue(Book::class.java)!!
                        val Title = DATA.EMPTY + item.title
                        val Description = DATA.EMPTY + item.description
                        val ViewsCount = DATA.EMPTY + item.viewsCount
                        val LovesCount = DATA.EMPTY + item.lovesCount
                        val DownloadsCount = DATA.EMPTY + item.downloadsCount
                        val BookImage = DATA.EMPTY + item.image

                        holder.binding.title.text = Title
                        holder.binding.description.text = Description
                        holder.binding.numberViews.text = ViewsCount
                        holder.binding.numberLoves.text = LovesCount
                        holder.binding.numberDownloads.text = DownloadsCount
                        holder.binding.image.loadWithGlide(false, BookImage)
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


package com.flatcode.littlebooksadmin.Adapter

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
import com.flatcode.littlebooksadmin.Modelimport.Book
import com.flatcode.littlebooksadmin.Modelimport.EditorsChoice
import com.flatcode.littlebooksadmin.Unit.CLASS
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ItemBookEditorsChoiceBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class EditorsChoiceAdapter(private val context: Context, var list: List<EditorsChoice>) :
    RecyclerView.Adapter<EditorsChoiceAdapter.ViewHolder>() {

    private var binding: ItemBookEditorsChoiceBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemBookEditorsChoiceBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val id = position + 1
        val editorsChoiceId = DATA.EMPTY + id

        loadBookDetails(
            id, editorsChoiceId, holder.title, holder.description, holder.numberViews,
            holder.numberLoves, holder.numberDownloads, holder.image, holder.remove, holder.change,
            holder.addCard, holder.detailsCard
        )

        holder.numberEditorsChoice.text = MessageFormat.format("{0}{1}", DATA.EMPTY, id)
        holder.add.setOnClickListener {
            VOID.IntentExtra2(
                context, CLASS.EDITORS_CHOICE_ADD,
                DATA.EDITORS_CHOICE_ID, editorsChoiceId, DATA.OLD_BOOK_ID, null
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(
        view!!
    ) {
        var image: ImageView
        var add: ImageView
        var remove: ImageView
        var change: ImageView
        var more: ImageButton
        var title: TextView
        var description: TextView
        var numberViews: TextView
        var numberLoves: TextView
        var numberDownloads: TextView
        var numberEditorsChoice: TextView
        var item: LinearLayout
        var item2: LinearLayout
        var addCard: CardView
        var detailsCard: CardView

        init {
            image = binding!!.image
            title = binding!!.title
            more = binding!!.more
            description = binding!!.description
            numberViews = binding!!.numberViews
            numberLoves = binding!!.numberLoves
            numberDownloads = binding!!.numberDownloads
            item = binding!!.item
            item2 = binding!!.item2
            add = binding!!.add
            numberEditorsChoice = binding!!.numberEditorsChoice
            addCard = binding!!.addCard
            detailsCard = binding!!.detailsCard
            remove = binding!!.remove
            change = binding!!.change
        }
    }

    private fun loadBookDetails(
        i: Int, position: String, title: TextView, description: TextView, viewsCount: TextView,
        lovesCount: TextView, downloadsCount: TextView, image: ImageView, remove: ImageView,
        change: ImageView, addCard: CardView, detailsCard: CardView
    ) {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val item = snapshot.getValue(Book::class.java)!!
                    if (item.editorsChoice == i) {
                        val id = DATA.EMPTY + item.id

                        loadBook(id)
                        addCard.visibility = View.GONE
                        detailsCard.visibility = View.VISIBLE
                        remove.visibility = View.VISIBLE
                        change.visibility = View.VISIBLE
                        detailsCard.setOnClickListener {
                            VOID.IntentExtra(context, CLASS.BOOK_DETAIL, DATA.BOOK_ID, id)
                        }
                        remove.setOnClickListener {
                            VOID.dialogOptionDelete(
                                context, null, id, null, null,
                                false, true, null, null
                            )
                        }
                        change.setOnClickListener {
                            VOID.IntentExtra2(
                                context, CLASS.EDITORS_CHOICE_ADD,
                                DATA.EDITORS_CHOICE_ID, position, DATA.OLD_BOOK_ID, id
                            )
                        }
                    } else {
                        addCard.visibility = View.VISIBLE
                        detailsCard.visibility = View.GONE
                        remove.visibility = View.GONE
                        change.visibility = View.GONE
                    }
                }
            }

            private fun loadBook(text: String) {
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

                        title.text = Title
                        description.text = Description
                        viewsCount.text = ViewsCount
                        lovesCount.text = LovesCount
                        downloadsCount.text = DownloadsCount
                        VOID.Glide(false, context, BookImage, image)
                        addCard.visibility = View.GONE
                        detailsCard.visibility = View.VISIBLE
                        remove.visibility = View.VISIBLE
                        change.visibility = View.VISIBLE
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
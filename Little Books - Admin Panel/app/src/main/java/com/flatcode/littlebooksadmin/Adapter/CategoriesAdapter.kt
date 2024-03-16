package com.flatcode.littlebooksadmin.Adapterimport

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.Filter.CategoriesFilter
import com.flatcode.littlebooksadmin.Modelimport.Book
import com.flatcode.littlebooksadmin.Modelimport.Category
import com.flatcode.littlebooksadmin.Unit.CLASS
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ItemCategoriesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class CategoriesAdapter(private val context: Context, var list: ArrayList<Category?>) :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>(), Filterable {

    private var binding: ItemCategoriesBinding? = null
    var filterList: ArrayList<Category?>
    private var filter: CategoriesFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, VT: Int): CategoriesAdapter.ViewHolder {
        binding = ItemCategoriesBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: CategoriesAdapter.ViewHolder, position: Int) {
        val item = list[position]
        val categoryId = DATA.EMPTY + item!!.id
        val name = DATA.EMPTY + item.category
        val image = DATA.EMPTY + item.image

        VOID.Glide(false, context, image, holder.image)

        if (item.category == DATA.EMPTY) {
            holder.name.visibility = View.GONE
        } else {
            holder.name.visibility = View.VISIBLE
            holder.name.text = name
        }

        nrBooks(holder.numberBooks, categoryId)

        holder.more.setOnClickListener { VOID.moreCategories(context, item) }
        holder.item.setOnClickListener {
            VOID.IntentExtra2(
                context, CLASS.CATEGORY_BOOKS,
                DATA.CATEGORY_ID, categoryId, DATA.CATEGORY_NAME, name
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = CategoriesFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(
        view!!
    ) {
        var image: ImageView
        var more: ImageButton
        var name: TextView
        var numberBooks: TextView
        var item: LinearLayout

        init {
            image = binding!!.image
            name = binding!!.name
            more = binding!!.more
            numberBooks = binding!!.numberBooks
            item = binding!!.item
        }
    }

    private fun nrBooks(number: TextView, categoryId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.BOOKS)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var i = 0
                for (snapshot in dataSnapshot.children) {
                    val item = snapshot.getValue(Book::class.java)!!
                    if (item.categoryId == categoryId) i++
                }
                number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, i)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    init {
        filterList = list
    }
}
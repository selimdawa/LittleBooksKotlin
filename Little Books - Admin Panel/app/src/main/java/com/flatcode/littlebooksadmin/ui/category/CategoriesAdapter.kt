package com.flatcode.littlebooksadmin.ui.category

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
import com.flatcode.littlebooksadmin.filter.CategoriesFilter
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.ui.category.BooksCategoryActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.*
import com.flatcode.littlebooksadmin.databinding.ItemCategoriesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class CategoriesAdapter(private val context: Context, var list: ArrayList<Category?>) :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>(), Filterable {

    var filterList: ArrayList<Category?>
    private var filter: CategoriesFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, VT: Int): ViewHolder {
        val binding = ItemCategoriesBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriesAdapter.ViewHolder, position: Int) {
        val item = list[position]
        val categoryId = DATA.EMPTY + item!!.id
        val name = DATA.EMPTY + item.category
        val image = DATA.EMPTY + item.image

        holder.binding.image.loadWithGlide(false, image)

        if (item.category == DATA.EMPTY) {
            holder.binding.name.visibility = View.GONE
        } else {
            holder.binding.name.visibility = View.VISIBLE
            holder.binding.name.text = name
        }

        nrBooks(holder.binding.numberBooks, categoryId)

        holder.binding.more.setOnClickListener { context.moreCategories(item) }
        holder.binding.item.setOnClickListener {
            context.openActivity<BooksCategoryActivity>(
                extras = arrayOf(DATA.CATEGORY_ID to categoryId, DATA.CATEGORY_NAME to name)
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

    inner class ViewHolder(val binding: ItemCategoriesBinding) : RecyclerView.ViewHolder(binding.root)

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


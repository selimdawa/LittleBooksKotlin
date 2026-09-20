package com.flatcode.littlebooksadmin.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.databinding.ItemCategoriesBinding
import com.flatcode.littlebooksadmin.filter.CategoriesFilter
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.loadWithGlide
import com.flatcode.littlebooksadmin.utils.moreCategories
import com.flatcode.littlebooksadmin.utils.openActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoriesAdapter : ListAdapter<Category, CategoriesAdapter.ViewHolder>(CategoryDiffCallback()), Filterable {

    var unfilteredList: List<Category> = emptyList()
        private set

    private var filter: CategoriesFilter? = null

    fun submitUnfilteredList(list: List<Category>?) {
        unfilteredList = list ?: emptyList()
        super.submitList(unfilteredList)
    }

    fun submitFilteredList(list: List<Category>?) {
        super.submitList(list ?: emptyList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        val categoryId = item.id
        val name = item.category.orEmpty()
        val image = item.image.orEmpty()

        holder.binding.image.loadWithGlide(false, image)

        holder.binding.name.visibility = if (item.category.isNullOrEmpty()) View.GONE else View.VISIBLE
        if (!item.category.isNullOrEmpty()) {
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

    override fun getFilter(): Filter {
        return filter ?: CategoriesFilter(this).also { filter = it }
    }

    class ViewHolder(val binding: ItemCategoriesBinding) : RecyclerView.ViewHolder(binding.root)

    private fun nrBooks(number: TextView, categoryId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.BOOKS)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val i = dataSnapshot.children.count {
                    it.getValue(Book::class.java)?.categoryId == categoryId
                }
                number.text = i.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
}
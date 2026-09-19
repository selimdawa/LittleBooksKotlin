package com.flatcode.littlebooks.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.databinding.ItemCategoryBinding
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.ui.book.BooksCategoryActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.glide
import com.flatcode.littlebooks.utils.intentExtra2

class CategoryAdapter : ListAdapter<Category, CategoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Category) {
            val context = itemView.context
            binding.image.glide(false, item.image)

            itemView.setOnClickListener {
                context.intentExtra2(
                    BooksCategoryActivity::class.java, DATA.CATEGORY_ID, item.id,
                    DATA.CATEGORY_NAME, item.category
                )
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean =
                oldItem == newItem
        }
    }
}

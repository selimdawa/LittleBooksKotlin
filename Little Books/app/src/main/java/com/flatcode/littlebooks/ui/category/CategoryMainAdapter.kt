package com.flatcode.littlebooks.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.databinding.ItemCategoryMainBinding
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.ui.book.BooksCategoryActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.glide
import com.flatcode.littlebooks.utils.glideBlur
import com.flatcode.littlebooks.utils.openActivity

class CategoryMainAdapter : ListAdapter<Category, CategoryMainAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCategoryMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    class ViewHolder(private val binding: ItemCategoryMainBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Category) {
            val context = itemView.context
            val name = DATA.EMPTY + item.category
            val image = DATA.EMPTY + item.image

            binding.image.glide(false, image)
            binding.imageBlur.glideBlur(false, image, 50)

            binding.name.visibility = if (name.isEmpty()) View.GONE else View.VISIBLE
            binding.name.text = name

            binding.card.setOnClickListener {
                context.openActivity<BooksCategoryActivity>(
                    false, DATA.CATEGORY_ID to item.id, DATA.CATEGORY_NAME to name
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
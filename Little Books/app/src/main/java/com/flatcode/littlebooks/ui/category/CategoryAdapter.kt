package com.flatcode.littlebooks.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.flatcode.littlebooks.base.BaseListAdapter
import com.flatcode.littlebooks.databinding.ItemCategoryBinding
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.utils.glide

class CategoryAdapter(
    private val onItemClick: (Category) -> Unit
) : BaseListAdapter<Category, ItemCategoryBinding>(DiffCallback) {

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemCategoryBinding {
        return ItemCategoryBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemCategoryBinding, item: Category, position: Int) {
        binding.image.glide(false, item.image)
        binding.root.setOnClickListener { onItemClick(item) }
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

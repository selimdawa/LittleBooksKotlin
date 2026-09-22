package com.flatcode.littlebooks.ui.book

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.flatcode.littlebooks.ui.BaseListAdapter
import com.flatcode.littlebooks.databinding.ItemBookLinearBinding
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.checkFavorite
import com.flatcode.littlebooks.utils.checkLove
import com.flatcode.littlebooks.utils.loadImage
import com.flatcode.littlebooks.utils.isFavorite
import com.flatcode.littlebooks.utils.isLoves
import com.flatcode.littlebooks.utils.moreOptionDialog

class LinearBookAdapter(
    private val isUser: Boolean,
    private val onItemClick: (Book) -> Unit
) : BaseListAdapter<Book, ItemBookLinearBinding>(DiffCallback) {

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemBookLinearBinding {
        return ItemBookLinearBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemBookLinearBinding, item: Book, position: Int) {
        val context = binding.root.context
        val bookId = DATA.EMPTY + item.id

        binding.more.visibility = if (isUser) View.VISIBLE else View.GONE
        binding.image.loadImage(false, item.image)

        binding.title.visibility = if (item.title.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.title.text = item.title

        binding.description.visibility = if (item.description.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.description.text = item.description

        binding.numberViews.text = item.viewsCount.toString()
        binding.numberLoves.text = item.lovesCount.toString()
        binding.numberDownloads.text = item.downloadsCount.toString()

        binding.favorites.isFavorite(item.id, DATA.FirebaseUserUid)
        binding.loves.isLoves(item.id)

        binding.favorites.setOnClickListener { binding.favorites.checkFavorite(bookId) }
        binding.loves.setOnClickListener { binding.loves.checkLove(bookId) }
        binding.more.setOnClickListener { context.moreOptionDialog(item) }
        binding.root.setOnClickListener { onItemClick(item) }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean =
                oldItem == newItem
        }
    }
}

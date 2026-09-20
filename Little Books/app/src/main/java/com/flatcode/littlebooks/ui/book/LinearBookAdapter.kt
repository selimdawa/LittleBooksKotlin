package com.flatcode.littlebooks.ui.book

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import com.flatcode.littlebooks.base.BaseListAdapter
import com.flatcode.littlebooks.databinding.ItemBookLinearBinding
import com.flatcode.littlebooks.filter.MoreBooksFilter
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.checkFavorite
import com.flatcode.littlebooks.utils.checkLove
import com.flatcode.littlebooks.utils.glide
import com.flatcode.littlebooks.utils.isFavorite
import com.flatcode.littlebooks.utils.isLoves
import com.flatcode.littlebooks.utils.moreOptionDialog

class LinearBookAdapter(
    private val isUser: Boolean,
    private val onItemClick: (Book) -> Unit
) : BaseListAdapter<Book, ItemBookLinearBinding>(DiffCallback), Filterable {

    var originalList: List<Book> = emptyList()
    private var filter: MoreBooksFilter? = null

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemBookLinearBinding {
        return ItemBookLinearBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemBookLinearBinding, item: Book, position: Int) {
        val context = binding.root.context
        val bookId = DATA.EMPTY + item.id

        binding.more.visibility = if (isUser) View.VISIBLE else View.GONE
        binding.image.glide(false, item.image)

        binding.title.visibility = if (item.title.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.title.text = item.title

        binding.description.visibility = if (item.description.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.description.text = item.description

        binding.numberViews.text = DATA.EMPTY + item.viewsCount
        binding.numberLoves.text = DATA.EMPTY + item.lovesCount
        binding.numberDownloads.text = DATA.EMPTY + item.downloadsCount

        binding.favorites.isFavorite(item.id, DATA.FirebaseUserUid)
        binding.loves.isLoves(item.id)

        binding.favorites.setOnClickListener { binding.favorites.checkFavorite(bookId) }
        binding.loves.setOnClickListener { binding.loves.checkLove(bookId) }
        binding.more.setOnClickListener { context.moreOptionDialog(item) }
        binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = MoreBooksFilter(ArrayList(originalList), this)
        }
        return filter!!
    }

    fun submitFullList(list: List<Book>?) {
        originalList = list ?: emptyList()
        submitList(originalList)
    }

    fun setFilteredList(list: List<Book?>?) {
        submitList(list?.filterNotNull() ?: emptyList())
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

package com.flatcode.littlebooks.ui.book

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
import com.flatcode.littlebooks.utils.openActivity

class LinearBookAdapter(
    private val isUser: Boolean
) : ListAdapter<Book, LinearBookAdapter.ViewHolder>(DiffCallback), Filterable {

    var originalList: List<Book> = emptyList()
    private var filter: MoreBooksFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
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

    inner class ViewHolder(private val binding: ItemBookLinearBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Book) {
            val context = itemView.context
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
            binding.item.setOnClickListener {
                context.openActivity<BookDetailsActivity>(false, DATA.BOOK_ID to item.id)
            }
        }
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

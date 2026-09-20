package com.flatcode.littlebooks.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.databinding.ItemBookMainBinding
import com.flatcode.littlebooks.filter.PDFMainFilter
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.ui.book.BookDetailsActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.checkFavorite
import com.flatcode.littlebooks.utils.glide
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.utils.isFavorite

class MainBookAdapter(
    private val isDownloads: Boolean,
    private val isViews: Boolean,
    private val isLoves: Boolean
) : ListAdapter<Book, MainBookAdapter.ViewHolder>(DiffCallback), Filterable {

    var originalList: List<Book> = emptyList()
    private var filter: PDFMainFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
            filter = PDFMainFilter(ArrayList(originalList), this)
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

    inner class ViewHolder(private val binding: ItemBookMainBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Book) {
            val context = itemView.context
            val bookId = DATA.EMPTY + item.id

            binding.linearDownloads.visibility = if (isDownloads) View.VISIBLE else View.GONE
            binding.linearLoves.visibility = if (isLoves) View.VISIBLE else View.GONE
            binding.linearViews.visibility = if (isViews) View.VISIBLE else View.GONE
            binding.line.visibility = if (isViews || isLoves || isDownloads) View.VISIBLE else View.GONE

            binding.image.glide(false, item.image)
            binding.views.text = DATA.EMPTY + item.viewsCount
            binding.downloads.text = DATA.EMPTY + item.downloadsCount
            binding.loves.text = DATA.EMPTY + item.lovesCount
            binding.name.text = item.title

            binding.favorites.isFavorite(item.id, DATA.FirebaseUserUid)

            binding.favorites.setOnClickListener {
                binding.favorites.checkFavorite(bookId)
            }

            itemView.setOnClickListener {
                context.openActivity<BookDetailsActivity>(false, DATA.BOOK_ID to bookId)
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

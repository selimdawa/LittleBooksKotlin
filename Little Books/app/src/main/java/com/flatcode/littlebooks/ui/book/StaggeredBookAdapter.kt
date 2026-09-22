package com.flatcode.littlebooks.ui.book

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.databinding.ItemBookStaggeredBinding
import com.flatcode.littlebooks.filter.StaggerdFilter
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.checkFavorite
import com.flatcode.littlebooks.utils.checkLove
import com.flatcode.littlebooks.utils.loadImage
import com.flatcode.littlebooks.utils.isFavorite
import com.flatcode.littlebooks.utils.isLoves
import com.flatcode.littlebooks.utils.openActivity

class StaggeredBookAdapter : ListAdapter<Book, StaggeredBookAdapter.ViewHolder>(DiffCallback),
    Filterable {

    var originalList: List<Book> = emptyList()
    private var filter: StaggerdFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookStaggeredBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
            filter = StaggerdFilter(ArrayList(originalList), this)
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

    inner class ViewHolder(private val binding: ItemBookStaggeredBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Book) {
            val context = itemView.context
            val bookId = DATA.EMPTY + item.id

            binding.image.loadImage(false, item.image)

            binding.title.visibility = if (item.title.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.title.text = item.title

            binding.numberLoves.text = DATA.EMPTY + item.lovesCount
            binding.numberDownloads.text = DATA.EMPTY + item.downloadsCount

            binding.favorites.isFavorite(bookId, DATA.FirebaseUserUid)
            binding.loves.isLoves(bookId)

            binding.favorites.setOnClickListener { binding.favorites.checkFavorite(bookId) }
            binding.loves.setOnClickListener { binding.loves.checkLove(bookId) }

            binding.item.setOnClickListener {
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

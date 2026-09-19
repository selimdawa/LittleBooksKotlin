package com.flatcode.littlebooksadmin.ui.book

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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.filter.StaggeredFilter
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.*
import com.flatcode.littlebooksadmin.databinding.ItemBookStaggeredBinding

class StaggeredBookAdapter : ListAdapter<Book, StaggeredBookAdapter.ViewHolder>(BookDiffCallback()), Filterable {

    var unfilteredList: List<Book> = emptyList()
        private set

    private var filter: StaggeredFilter? = null

    fun submitUnfilteredList(list: List<Book>?) {
        unfilteredList = list ?: emptyList()
        super.submitList(unfilteredList)
    }

    fun submitFilteredList(list: List<Book>?) {
        super.submitList(list ?: emptyList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookStaggeredBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        val bookId = item.id
        val title = item.title.orEmpty()
        val image = item.image.orEmpty()
        val nrLoves = item.lovesCount.toString()
        val nrDownloads = item.downloadsCount.toString()

        holder.binding.image.loadWithGlide(false, image)

        if (item.title.isNullOrEmpty()) {
            holder.binding.title.visibility = View.GONE
        } else {
            holder.binding.title.visibility = View.VISIBLE
            holder.binding.title.text = title
        }

        holder.binding.numberLoves.text = nrLoves
        holder.binding.numberDownloads.text = nrDownloads

        holder.binding.favorites.isFavorite(item.id, DATA.FirebaseUserUid)
        holder.binding.loves.isLoves(item.id)

        holder.binding.favorites.setOnClickListener { holder.binding.favorites.checkFavorite(bookId) }
        holder.binding.loves.setOnClickListener { holder.binding.loves.checkLove(bookId) }
        holder.binding.more.setOnClickListener { context.moreOptionDialog(item) }

        holder.item.setOnClickListener {
            context.openActivity<BookDetailsActivity>(extras = arrayOf(DATA.BOOK_ID to bookId))
        }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = StaggeredFilter(this)
        }
        return filter!!
    }

    inner class ViewHolder(val binding: ItemBookStaggeredBinding) : RecyclerView.ViewHolder(binding.root) {
        val item = binding.item
    }
}


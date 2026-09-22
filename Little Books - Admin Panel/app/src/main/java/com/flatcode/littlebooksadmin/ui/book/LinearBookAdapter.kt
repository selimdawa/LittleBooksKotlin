package com.flatcode.littlebooksadmin.ui.book

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.*
import com.flatcode.littlebooksadmin.databinding.ItemBookLinearBinding

class LinearBookAdapter(private val isUser: Boolean) :
    ListAdapter<Book, LinearBookAdapter.ViewHolder>(BookDiffCallback()), Filterable {

    var unfilteredList: List<Book> = emptyList()
        private set

    private var filter: Filter? = null

    fun submitUnfilteredList(list: List<Book>?) {
        unfilteredList = list ?: emptyList()
        super.submitList(unfilteredList)
    }

    fun submitFilteredList(list: List<Book>?) {
        super.submitList(list ?: emptyList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        val bookId = item.id
        val title = item.title.orEmpty()
        val description = item.description.orEmpty()
        val image = item.image.orEmpty()
        val nrViews = item.viewsCount.toString()
        val nrLoves = item.lovesCount.toString()
        val nrDownloads = item.downloadsCount.toString()

        holder.binding.more.visibility = if (isUser) View.VISIBLE else View.GONE

        holder.binding.image.loadImage(isUser = false, url = image)

        holder.binding.title.visibility = if (item.title.isNullOrEmpty()) View.GONE else View.VISIBLE
        if (!item.title.isNullOrEmpty()) {
            holder.binding.title.text = title
        }

        holder.binding.description.visibility = if (item.description.isNullOrEmpty()) View.GONE else View.VISIBLE
        if (!item.description.isNullOrEmpty()) {
            holder.binding.description.text = description
        }

        holder.binding.numberViews.text = nrViews
        holder.binding.numberLoves.text = nrLoves
        holder.binding.numberDownloads.text = nrDownloads

        holder.binding.favorites.isFavorite(item.id, DATA.FirebaseUserUid)
        holder.binding.loves.isLoves(item.id)
        holder.binding.favorites.setOnClickListener {
            holder.binding.favorites.checkFavorite(bookId)
        }
        holder.binding.loves.setOnClickListener { holder.binding.loves.checkLove(bookId) }
        holder.binding.more.setOnClickListener {
            Dialogs.moreOptionDialog(context, item)
        }
        holder.binding.item.setOnClickListener {
            context.openActivity<BookDetailsActivity>(extras = arrayOf(DATA.BOOK_ID to item.id))
        }
    }

    override fun getFilter(): Filter {
        return filter ?: object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                var query = constraint
                val results = FilterResults()
                val filterList = unfilteredList
                if (!query.isNullOrEmpty()) {
                    query = query.toString().uppercase()
                    val filteredModels = ArrayList<Book>()
                    for (item in filterList) {
                        if (item.title?.uppercase()?.contains(query) == true) {
                            filteredModels.add(item)
                        }
                    }
                    results.count = filteredModels.size
                    results.values = filteredModels
                } else {
                    results.count = filterList.size
                    results.values = filterList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                val list = results.values as? List<*>
                submitFilteredList(list?.filterIsInstance<Book>())
            }
        }.also { filter = it }
    }

    class ViewHolder(val binding: ItemBookLinearBinding) : RecyclerView.ViewHolder(binding.root)
}




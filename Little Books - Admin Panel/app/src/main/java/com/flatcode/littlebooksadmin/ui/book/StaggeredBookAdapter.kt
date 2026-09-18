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
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.filter.StaggeredFilter
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.ui.book.BookDetailsActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.VOID
import com.flatcode.littlebooksadmin.databinding.ItemBookStaggeredBinding

class StaggeredBookAdapter(private val context: Context, var list: ArrayList<Book?>) :
    RecyclerView.Adapter<StaggeredBookAdapter.ViewHolder>(), Filterable {

    var filterList: ArrayList<Book?>
    private var filter: StaggeredFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookStaggeredBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val bookId = DATA.EMPTY + item!!.id
        val title = DATA.EMPTY + item.title
        val image = DATA.EMPTY + item.image
        val nrLoves = DATA.EMPTY + item.lovesCount
        val nrDownloads = DATA.EMPTY + item.downloadsCount

        VOID.Glide(false, context, image, holder.binding.image)

        if (item.title == DATA.EMPTY) {
            holder.binding.title.visibility = View.GONE
        } else {
            holder.binding.title.visibility = View.VISIBLE
            holder.binding.title.text = title
        }

        holder.binding.numberLoves.text = nrLoves
        holder.binding.numberDownloads.text = nrDownloads

        VOID.isFavorite(holder.binding.favorites, item.id, DATA.FirebaseUserUid)
        VOID.isLoves(holder.binding.loves, item.id)

        holder.binding.favorites.setOnClickListener { VOID.checkFavorite(holder.binding.favorites, bookId) }
        holder.binding.loves.setOnClickListener { VOID.checkLove(holder.binding.loves, bookId) }
        holder.binding.more.setOnClickListener { VOID.moreOptionDialog(context, item) }

        holder.item.setOnClickListener {
            VOID.IntentExtra(context, BookDetailsActivity::class.java, DATA.BOOK_ID, bookId)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = StaggeredFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(val binding: ItemBookStaggeredBinding) : RecyclerView.ViewHolder(binding.root) {
        val item = binding.item
    }

    init {
        filterList = list
    }
}


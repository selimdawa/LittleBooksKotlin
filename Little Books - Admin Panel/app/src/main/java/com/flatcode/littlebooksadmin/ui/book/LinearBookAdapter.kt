package com.flatcode.littlebooksadmin.ui.book

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.filter.MoreBooksFilter
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.ui.book.BookDetailsActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.*
import com.flatcode.littlebooksadmin.databinding.ItemBookLinearBinding

class LinearBookAdapter(private val context: Context, var list: ArrayList<Book?>, isUser: Boolean) :
    RecyclerView.Adapter<LinearBookAdapter.ViewHolder>(), Filterable {

    var filterList: ArrayList<Book?>
    private var filter: MoreBooksFilter? = null
    var isUser: Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookLinearBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val bookId = DATA.EMPTY + item!!.id
        val title = DATA.EMPTY + item.title
        val description = DATA.EMPTY + item.description
        val image = DATA.EMPTY + item.image
        val nrViews = DATA.EMPTY + item.viewsCount
        val nrLoves = DATA.EMPTY + item.lovesCount
        val nrDownloads = DATA.EMPTY + item.downloadsCount

        if (isUser) {
            holder.binding.more.visibility = View.VISIBLE
        } else {
            holder.binding.more.visibility = View.GONE
        }

        holder.binding.image.loadWithGlide(false, image)

        if (item.title == DATA.EMPTY) {
            holder.binding.title.visibility = View.GONE
        } else {
            holder.binding.title.visibility = View.VISIBLE
            holder.binding.title.text = title
        }

        if (item.description == DATA.EMPTY) {
            holder.binding.description.visibility = View.GONE
        } else {
            holder.binding.description.visibility = View.VISIBLE
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
            context.moreOptionDialog(item)
        }
        holder.binding.item.setOnClickListener {
            context.intentExtra(BookDetailsActivity::class.java, DATA.BOOK_ID, item.id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = MoreBooksFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(val binding: ItemBookLinearBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        filterList = list
        this.isUser = isUser
    }
}


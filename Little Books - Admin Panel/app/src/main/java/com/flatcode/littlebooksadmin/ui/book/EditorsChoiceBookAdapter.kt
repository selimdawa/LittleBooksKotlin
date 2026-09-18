package com.flatcode.littlebooksadmin.ui.book

import android.app.Activity
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
import com.flatcode.littlebooksadmin.filter.EditorsChoiceBookFilter
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.ui.book.BookDetailsActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.*
import com.flatcode.littlebooksadmin.databinding.ItemEditorsChoiceBinding

class EditorsChoiceBookAdapter(
    private val context: Context, private val activity: Activity?,
    var oldBookId: String?, var list: ArrayList<Book?>, number: Int
) : RecyclerView.Adapter<EditorsChoiceBookAdapter.ViewHolder>(), Filterable {

    var filterList: ArrayList<Book?>
    private var filter: EditorsChoiceBookFilter? = null
    var number: Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditorsChoiceBinding.inflate(LayoutInflater.from(context), parent, false)
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

        holder.binding.add.setOnClickListener {
            if (oldBookId != null) {
                context.addToEditorsChoice(activity, bookId, number)
                context.addToEditorsChoice(activity, oldBookId, 0)
            } else {
                context.addToEditorsChoice(activity, bookId, number)
            }
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
            filter = EditorsChoiceBookFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(val binding: ItemEditorsChoiceBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        filterList = list
        this.number = number
    }
}


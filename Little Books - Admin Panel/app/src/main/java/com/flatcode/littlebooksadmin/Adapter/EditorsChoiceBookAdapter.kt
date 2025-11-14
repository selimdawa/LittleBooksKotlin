package com.flatcode.littlebooksadmin.Adapter

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
import com.flatcode.littlebooksadmin.Filterimport.EditorsChoiceBookFilter
import com.flatcode.littlebooksadmin.Modelimport.Book
import com.flatcode.littlebooksadmin.Unit.CLASS
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ItemEditorsChoiceBinding

class EditorsChoiceBookAdapter(
    private val context: Context, private val activity: Activity?,
    var oldBookId: String?, var list: ArrayList<Book?>, number: Int
) : RecyclerView.Adapter<EditorsChoiceBookAdapter.ViewHolder>(), Filterable {
    private var binding: ItemEditorsChoiceBinding? = null

    var filterList: ArrayList<Book?>
    private var filter: EditorsChoiceBookFilter? = null
    var number: Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemEditorsChoiceBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
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

        VOID.Glide(false, context, image, holder.image)

        if (item.title == DATA.EMPTY) {
            holder.title.visibility = View.GONE
        } else {
            holder.title.visibility = View.VISIBLE
            holder.title.text = title
        }

        if (item.description == DATA.EMPTY) {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = description
        }

        holder.numberViews.text = nrViews
        holder.numberLoves.text = nrLoves
        holder.numberDownloads.text = nrDownloads

        holder.add.setOnClickListener {
            if (oldBookId != null) {
                VOID.addToEditorsChoice(context, activity, bookId, number)
                VOID.addToEditorsChoice(context, activity, oldBookId, 0)
            } else {
                VOID.addToEditorsChoice(context, activity, bookId, number)
            }
        }

        holder.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.BOOK_DETAIL, DATA.BOOK_ID, item.id)
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

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(
        view!!
    ) {
        var image: ImageView
        var add: ImageView
        var title: TextView
        var description: TextView
        var numberViews: TextView
        var numberLoves: TextView
        var numberDownloads: TextView
        var item: LinearLayout

        init {
            image = binding!!.image
            title = binding!!.title
            description = binding!!.description
            add = binding!!.add
            numberViews = binding!!.numberViews
            numberLoves = binding!!.numberLoves
            numberDownloads = binding!!.numberDownloads
            item = binding!!.item
        }
    }

    init {
        filterList = list
        this.number = number
    }
}
package com.flatcode.littlebooks.Adapter

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
import com.flatcode.littlebooks.Filter.MoreBooksFilter
import com.flatcode.littlebooks.Model.Book
import com.flatcode.littlebooks.Unit.CLASS
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ItemBookLinearBinding

class LinearBookAdapter(
    private val context: Context?,
    var list: ArrayList<Book?>,
    isUser: Boolean
) : RecyclerView.Adapter<LinearBookAdapter.ViewHolder>(), Filterable {

    private var binding: ItemBookLinearBinding? = null
    var filterList: ArrayList<Book?>
    private var filter: MoreBooksFilter? = null
    var isUser: Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemBookLinearBinding.inflate(LayoutInflater.from(context), parent, false)
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

        if (isUser) {
            holder.more.visibility = View.VISIBLE
        } else {
            holder.more.visibility = View.GONE
        }

        VOID.Glide_(false, context, image, holder.image)

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

        /*if (item.getPublisher().equals(DATA.FirebaseUserUid)) {
            holder.favorites.setVisibility(View.GONE);
        } else {
            holder.favorites.setVisibility(View.VISIBLE);
        }*/

        VOID.isFavorite(holder.favorites, item.id, DATA.FirebaseUserUid)
        VOID.isLoves(holder.loves, item.id)

        holder.favorites.setOnClickListener { VOID.checkFavorite(holder.favorites, bookId) }
        holder.loves.setOnClickListener { VOID.checkLove(holder.loves, bookId) }
        holder.more.setOnClickListener { VOID.moreOptionDialog(context, item) }
        holder.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.BOOK_DETAIL, DATA.BOOK_ID, item.id)
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

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(
        view!!
    ) {
        var image: ImageView
        var favorites: ImageView
        var loves: ImageView
        var more: ImageView
        var title: TextView
        var description: TextView
        var numberViews: TextView
        var numberLoves: TextView
        var numberDownloads: TextView
        var item: LinearLayout

        init {
            image = binding!!.image
            title = binding!!.title
            more = binding!!.more
            description = binding!!.description
            favorites = binding!!.favorites
            loves = binding!!.loves
            numberViews = binding!!.numberViews
            numberLoves = binding!!.numberLoves
            numberDownloads = binding!!.numberDownloads
            item = binding!!.item
        }
    }

    init {
        filterList = list
        this.isUser = isUser
    }
}
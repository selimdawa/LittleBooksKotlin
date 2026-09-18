package com.flatcode.littlebooks.ui.book

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
import com.flatcode.littlebooks.filter.MoreBooksFilter
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.checkFavorite
import com.flatcode.littlebooks.utils.checkLove
import com.flatcode.littlebooks.utils.glide
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.utils.isFavorite
import com.flatcode.littlebooks.utils.isLoves
import com.flatcode.littlebooks.utils.moreOptionDialog
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

        holder.image.glide(false, image)

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

        holder.favorites.isFavorite(item.id, DATA.FirebaseUserUid)
        holder.loves.isLoves(item.id)

        holder.favorites.setOnClickListener { holder.favorites.checkFavorite(bookId) }
        holder.loves.setOnClickListener { holder.loves.checkLove(bookId) }
        holder.more.setOnClickListener { context?.moreOptionDialog(item) }
        holder.item.setOnClickListener {
            context?.openActivity<BookDetailsActivity>(false, DATA.BOOK_ID to item.id)
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




package com.flatcode.littlebooksadmin.Adapter

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
import com.flatcode.littlebooksadmin.Filterimport.StaggeredFilter
import com.flatcode.littlebooksadmin.Modelimport.Book
import com.flatcode.littlebooksadmin.Unit.CLASS
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ItemBookStaggeredBinding

class StaggeredBookAdapter(private val context: Context, var list: ArrayList<Book?>) :
    RecyclerView.Adapter<StaggeredBookAdapter.ViewHolder>(), Filterable {

    private var binding: ItemBookStaggeredBinding? = null
    var filterList: ArrayList<Book?>
    private var filter: StaggeredFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemBookStaggeredBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val bookId = DATA.EMPTY + item!!.id
        val title = DATA.EMPTY + item.title
        val image = DATA.EMPTY + item.image
        val nrLoves = DATA.EMPTY + item.lovesCount
        val nrDownloads = DATA.EMPTY + item.downloadsCount

        VOID.Glide(false, context, image, holder.image)

        if (item.title == DATA.EMPTY) {
            holder.title.visibility = View.GONE
        } else {
            holder.title.visibility = View.VISIBLE
            holder.title.text = title
        }

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
            VOID.IntentExtra(context, CLASS.BOOK_DETAIL, DATA.BOOK_ID, bookId)
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

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var image: ImageView
        var favorites: ImageView
        var loves: ImageView
        var more: ImageButton
        var title: TextView
        var numberLoves: TextView
        var numberDownloads: TextView
        var item: LinearLayout

        init {
            image = binding!!.image
            title = binding!!.title
            more = binding!!.more
            favorites = binding!!.favorites
            loves = binding!!.loves
            numberLoves = binding!!.numberLoves
            numberDownloads = binding!!.numberDownloads
            item = binding!!.item
        }
    }

    init {
        filterList = list
    }
}
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
import com.flatcode.littlebooks.filter.StaggerdFilter
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.checkFavorite
import com.flatcode.littlebooks.utils.checkLove
import com.flatcode.littlebooks.utils.glide
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.utils.isFavorite
import com.flatcode.littlebooks.utils.isLoves
import com.flatcode.littlebooks.databinding.ItemBookStaggeredBinding

class StaggeredBookAdapter(private val context: Context, var list: ArrayList<Book?>) :
    RecyclerView.Adapter<StaggeredBookAdapter.ViewHolder>(), Filterable {

    private var binding: ItemBookStaggeredBinding? = null

    var filterList: ArrayList<Book?>
    private var filter: StaggerdFilter? = null
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

        holder.image.glide(false, image)

        if (title == DATA.EMPTY) {
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

        holder.favorites.isFavorite(bookId, DATA.FirebaseUserUid)
        holder.loves.isLoves(bookId)

        holder.favorites.setOnClickListener { holder.favorites.checkFavorite(bookId) }
        holder.loves.setOnClickListener { holder.loves.checkLove(bookId) }

        holder.item.setOnClickListener {
            context.openActivity<BookDetailsActivity>(false, DATA.BOOK_ID to bookId)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = StaggerdFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(
        view!!
    ) {
        var image: ImageView
        var favorites: ImageView
        var loves: ImageView
        var title: TextView
        var numberLoves: TextView
        var numberDownloads: TextView
        var item: LinearLayout

        init {
            image = binding!!.image
            title = binding!!.title
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




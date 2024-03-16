package com.flatcode.littlebooksadmin.Adapterimport

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
import com.flatcode.littlebooksadmin.Filterimport.TopPublisherFilter
import com.flatcode.littlebooksadmin.Modelimport.User
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.CLASS
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ItemTopPublisherBinding
import java.text.MessageFormat

class TopPublisherAdapter(
    private val context: Context, var list: ArrayList<User?>, isUser: Boolean
) : RecyclerView.Adapter<TopPublisherAdapter.ViewHolder>(), Filterable {

    private var binding: ItemTopPublisherBinding? = null
    var filterList: ArrayList<User?>
    private var filter: TopPublisherFilter? = null
    var isUser: Boolean

    override fun onCreateViewHolder(parent: ViewGroup, VT: Int): TopPublisherAdapter.ViewHolder {
        binding = ItemTopPublisherBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: TopPublisherAdapter.ViewHolder, position: Int) {
        val all = list.size
        val loop = position / 2
        val round = loop * 2
        val round2 = round + 1

        run {
            var x = 0
            while (x < all && round == position) {
                holder.item.setBackgroundResource(R.drawable.solid1)
                x++
            }
        }

        var x = 0
        while (x < all && round2 == position) {
            holder.item.setBackgroundResource(R.drawable.solid2)
            x++
        }

        val item = list[position]
        val userId = DATA.EMPTY + item!!.id
        val username = DATA.EMPTY + item.username
        val profileImage = DATA.EMPTY + item.profileImage
        val numberBooks = DATA.EMPTY + item.booksCount

        VOID.Glide(false, context, profileImage, holder.profileImage)

        if (username == DATA.EMPTY) {
            holder.username.visibility = View.GONE
        } else {
            holder.username.visibility = View.VISIBLE
            holder.username.text = username
        }

        val First = holder.position
        val Final = list.size - First
        holder.rank.text = MessageFormat.format("{0}", Final)
        holder.numberBooks.text = numberBooks

        /*if (item.getPublisher().equals(DATA.FirebaseUserUid)) {
            holder.favorites.setVisibility(View.GONE);
        } else {
            holder.favorites.setVisibility(View.VISIBLE);
        }*/

        holder.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.PROFILE, DATA.PROFILE_ID, userId)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = TopPublisherFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(
        view!!
    ) {
        var profileImage: ImageView
        var username: TextView
        var numberBooks: TextView
        var rank: TextView
        var item: LinearLayout

        init {
            profileImage = binding!!.imageProfile
            username = binding!!.username
            rank = binding!!.rank
            numberBooks = binding!!.numberBooks
            item = binding!!.item
        }
    }

    init {
        filterList = list
        this.isUser = isUser
    }
}
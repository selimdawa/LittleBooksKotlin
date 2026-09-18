package com.flatcode.littlebooksadmin.ui.user

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
import com.flatcode.littlebooksadmin.filter.TopPublisherFilter
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.ui.profile.ProfileActivity
import com.flatcode.littlebooksadmin.utils.*
import com.flatcode.littlebooksadmin.databinding.ItemTopPublisherBinding
import java.text.MessageFormat

class TopPublisherAdapter(
    private val context: Context, var list: ArrayList<User?>, isUser: Boolean
) : RecyclerView.Adapter<TopPublisherAdapter.ViewHolder>(), Filterable {

    var filterList: ArrayList<User?>
    private var filter: TopPublisherFilter? = null
    var isUser: Boolean

    override fun onCreateViewHolder(parent: ViewGroup, VT: Int): TopPublisherAdapter.ViewHolder {
        val binding = ItemTopPublisherBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopPublisherAdapter.ViewHolder, position: Int) {
        val all = list.size
        val loop = position / 2
        val round = loop * 2
        val round2 = round + 1

        run {
            var x = 0
            while (x < all && round == position) {
                holder.binding.item.setBackgroundResource(R.drawable.solid1)
                x++
            }
        }

        var x = 0
        while (x < all && round2 == position) {
            holder.binding.item.setBackgroundResource(R.drawable.solid2)
            x++
        }

        val item = list[position]
        val userId = DATA.EMPTY + item!!.id
        val username = DATA.EMPTY + item.username
        val profileImage = DATA.EMPTY + item.profileImage
        val numberBooks = DATA.EMPTY + item.booksCount

        holder.binding.imageProfile.loadWithGlide(false, profileImage)

        if (username == DATA.EMPTY) {
            holder.binding.username.visibility = View.GONE
        } else {
            holder.binding.username.visibility = View.VISIBLE
            holder.binding.username.text = username
        }

        val First = holder.position
        val Final = list.size - First
        holder.binding.rank.text = MessageFormat.format("{0}", Final)
        holder.binding.numberBooks.text = numberBooks

        holder.binding.item.setOnClickListener {
            context.openActivity<ProfileActivity>(extras = arrayOf(DATA.PROFILE_ID to userId))
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

    inner class ViewHolder(val binding: ItemTopPublisherBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        filterList = list
        this.isUser = isUser
    }
}


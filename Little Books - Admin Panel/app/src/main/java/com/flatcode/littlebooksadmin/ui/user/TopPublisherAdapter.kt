package com.flatcode.littlebooksadmin.ui.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ItemTopPublisherBinding
import com.flatcode.littlebooksadmin.filter.TopPublisherFilter
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.ui.profile.ProfileActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.loadWithGlide
import com.flatcode.littlebooksadmin.utils.openActivity
import java.text.MessageFormat

class TopPublisherAdapter(val isUser: Boolean) :
    ListAdapter<User, TopPublisherAdapter.ViewHolder>(TopPublisherDiffCallback()), Filterable {

    var unfilteredList: List<User?> = emptyList()
        private set

    private var filter: TopPublisherFilter? = null

    fun submitUnfilteredList(list: List<User?>?) {
        unfilteredList = list ?: emptyList()
        super.submitList(unfilteredList.filterNotNull())
    }

    fun submitFilteredList(list: List<User?>?) {
        super.submitList(list?.filterNotNull() ?: emptyList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, VT: Int): ViewHolder {
        val binding = ItemTopPublisherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val all = itemCount
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

        val item = getItem(position)
        val context = holder.itemView.context
        val userId = DATA.EMPTY + item.id
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

        val first = position
        val finalCount = itemCount - first
        holder.binding.rank.text = MessageFormat.format("{0}", finalCount)
        holder.binding.numberBooks.text = numberBooks

        holder.binding.item.setOnClickListener {
            context.openActivity<ProfileActivity>(extras = arrayOf(DATA.PROFILE_ID to userId))
        }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = TopPublisherFilter(this)
        }
        return filter!!
    }

    class ViewHolder(val binding: ItemTopPublisherBinding) : RecyclerView.ViewHolder(binding.root)
}

class TopPublisherDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}

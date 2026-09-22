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
import com.flatcode.littlebooksadmin.utils.loadImage
import com.flatcode.littlebooksadmin.utils.openActivity

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
        val backgroundRes = if (position % 2 == 0) R.drawable.solid1 else R.drawable.solid2
        holder.binding.item.setBackgroundResource(backgroundRes)

        val item = getItem(position)
        val context = holder.itemView.context
        val userId = item.id ?: ""
        val username = item.username.orEmpty()
        val profileImage = item.profileImage.orEmpty()
        val numberBooks = item.booksCount.toString()

        holder.binding.imageProfile.loadImage(isUser = false, url = profileImage)

        holder.binding.username.visibility = if (username.isEmpty()) View.GONE else View.VISIBLE
        if (username.isNotEmpty()) {
            holder.binding.username.text = username
        }

        holder.binding.rank.text = (itemCount - position).toString()
        holder.binding.numberBooks.text = numberBooks

        holder.binding.item.setOnClickListener {
            context.openActivity<ProfileActivity>(extras = arrayOf(DATA.PROFILE_ID to userId))
        }
    }

    override fun getFilter(): Filter {
        return filter ?: TopPublisherFilter(this).also { filter = it }
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



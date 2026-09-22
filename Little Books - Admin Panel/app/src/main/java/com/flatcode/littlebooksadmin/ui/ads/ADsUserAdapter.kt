package com.flatcode.littlebooksadmin.ui.ads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.Application
import com.flatcode.littlebooksadmin.databinding.ItemAdsUserBinding
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.loadImage
import com.flatcode.littlebooksadmin.utils.openActivity

class ADsUserAdapter(val isUser: Boolean) :
    ListAdapter<User, ADsUserAdapter.ViewHolder>(UserDiffCallback()), Filterable {

    var unfilteredList: List<User?> = emptyList()
        private set

    private var filter: Filter? = null

    fun submitUnfilteredList(list: List<User?>?) {
        unfilteredList = list ?: emptyList()
        super.submitList(unfilteredList.filterNotNull())
    }

    fun submitFilteredList(list: List<User?>?) {
        super.submitList(list?.filterNotNull() ?: emptyList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdsUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        val userId = DATA.EMPTY + item.id
        val username = DATA.EMPTY + item.username
        val profileImage = DATA.EMPTY + item.profileImage
        val timestamp = DATA.EMPTY + item.timestamp
        val adLoaded = DATA.EMPTY + item.adLoad
        val adClicked = DATA.EMPTY + item.adClick
        val formattedDate: String = Application.formatTimestamp(timestamp.toLong())

        holder.binding.profileImage.loadImage(isUser = true, url = profileImage)

        if (username == DATA.EMPTY) {
            holder.binding.username.visibility = View.GONE
        } else {
            holder.binding.username.visibility = View.VISIBLE
            holder.binding.username.text = username
        }

        holder.binding.time.text = formattedDate
        holder.binding.rank.text = (itemCount - position).toString()
        holder.binding.numberADsLoad.text = adLoaded
        holder.binding.numberADsClick.text = adClicked

        holder.binding.item.setOnClickListener {
            context.openActivity<AdsInfoActivity>(extras = arrayOf(DATA.PROFILE_ID to userId))
        }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    var query = constraint
                    val results = FilterResults()
                    val filterList = unfilteredList
                    if (!query.isNullOrEmpty()) {
                        query = query.toString().uppercase()
                        val filteredModels = ArrayList<User?>()
                        for (item in filterList) {
                            if (item?.username?.uppercase()?.contains(query) == true) {
                                filteredModels.add(item)
                            }
                        }
                        results.count = filteredModels.size
                        results.values = filteredModels
                    } else {
                        results.count = filterList.size
                        results.values = filterList
                    }
                    return results
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                    val list = results.values as? List<*>
                    submitFilteredList(list?.filterIsInstance<User>())
                }
            }
        }
        return filter!!
    }

    class ViewHolder(val binding: ItemAdsUserBinding) : RecyclerView.ViewHolder(binding.root)
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}
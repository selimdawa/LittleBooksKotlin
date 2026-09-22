package com.flatcode.littlebooksadmin.ui.ads

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.databinding.ItemInfoAdsBinding
import com.flatcode.littlebooksadmin.model.ADs

class ADsInfoAdapter(private val isUser: Boolean) :
    ListAdapter<ADs, ADsInfoAdapter.ViewHolder>(ADsDiffCallback()), Filterable {

    var unfilteredList: List<ADs> = emptyList()
        private set

    private var filter: Filter? = null

    fun submitUnfilteredList(list: List<ADs>?) {
        unfilteredList = list ?: emptyList()
        super.submitList(unfilteredList)
    }

    fun submitFilteredList(list: List<ADs>?) {
        super.submitList(list ?: emptyList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInfoAdsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val name = item.name
        val adsLoadedCount = item.adsLoadedCount
        val adsClickedCount = item.adsClickedCount

        if (name != null) {
            holder.binding.name.text = name
        }

        holder.binding.numberADsLoad.text = adsLoadedCount.toString()
        holder.binding.numberADsClick.text = adsClickedCount.toString()
    }

    override fun getFilter(): Filter {
        return filter ?: object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                var query = constraint
                val results = FilterResults()
                val filterList = unfilteredList
                if (query != null && query.isNotEmpty()) {
                    query = query.toString().uppercase()
                    val filteredModels = ArrayList<ADs>()
                    for (i in filterList.indices) {
                        if (filterList[i].name?.uppercase()?.contains(query) == true) {
                            filteredModels.add(filterList[i])
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
                submitFilteredList(results.values as? List<ADs>)
            }
        }.also { filter = it }
    }

    class ViewHolder(val binding: ItemInfoAdsBinding) : RecyclerView.ViewHolder(binding.root)
}

class ADsDiffCallback : DiffUtil.ItemCallback<ADs>() {
    override fun areItemsTheSame(oldItem: ADs, newItem: ADs): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: ADs, newItem: ADs): Boolean {
        return oldItem == newItem
    }
}
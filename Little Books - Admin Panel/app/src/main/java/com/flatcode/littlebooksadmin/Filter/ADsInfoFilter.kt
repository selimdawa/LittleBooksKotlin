package com.flatcode.littlebooksadmin.filter

import android.widget.Filter
import com.flatcode.littlebooksadmin.model.ADs
import com.flatcode.littlebooksadmin.ui.ads.ADsInfoAdapter

class ADsInfoFilter(
    private var filterList: List<ADs?>,
    private var adapter: ADsInfoAdapter
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<ADs?>()
            for (i in filterList.indices) {
                if (filterList[i]!!.name!!.uppercase().contains(constraint)) {
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
        adapter.list = results.values as ArrayList<ADs?>
        adapter.notifyDataSetChanged()
    }
}
